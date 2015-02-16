/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.dal.db.bms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ClosureUtils;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;
import com.diversityarrays.dal.sqldb.ResultSetVisitor;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.util.Continue;

class BmsConnectionInfo {

	
	static private final String SPECIES_NAME_CONSTRAINT = " ftable='ATRIBUTS' AND ftype='PASSPORT' AND fcode = 'SPP_CODE' AND fname='SPECIES CODE'";

	// for CIMMYT test database: fcode IN ('TAXON','TAXNO');
	static private final String TAXONOMY_CONSTRAINT = " ftable='ATRIBUTS' AND ftype='PASSPORT' AND fcode IN ('TAXON','TAXNO') AND fname='Taxonomy'";
//	static private final String TAXONOMY_CONSTRAINT = " ftable='ATRIBUTS' AND ftype='PASSPORT' AND fcode = 'TAXON' AND fname='Taxonomy'";
	
	static private final String GET_TAXONOMY_FLDNO = "SELECT fldno FROM udflds WHERE " + TAXONOMY_CONSTRAINT ; 
	
	static private final String GET_SPECIES_FLDNO =  "SELECT fldno FROM udflds WHERE " + SPECIES_NAME_CONSTRAINT;

	
	private Connection localConnection;
	Connection centralConnection;
	
	// default is to use the Taxonomy one
	private final boolean genusFromSpecies = Boolean.getBoolean("BMS_GENUS_FROM_SPPCODE");
	public Integer fldNoForGenus;
	
	public GenusStore genusStore;
	
	public Map<Integer,UdfldsRecord> userTypesByFldno = new HashMap<Integer,UdfldsRecord>();
	
	private final JdbcConnectionParameters localParams;
	private final JdbcConnectionParameters centralParams;
	
	
	@SuppressWarnings("unchecked")
	BmsConnectionInfo(JdbcConnectionParameters local, JdbcConnectionParameters central, Closure<String> progress) throws DalDbException {
		
		if (progress==null) {
			progress = ClosureUtils.nopClosure();
		}
		
		this.localParams = local;
		this.centralParams = central;
		
		List<SQLException> errors = new ArrayList<SQLException>();
		try {
			if (localParams != null) {
				progress.execute("Connecting to " + localParams.connectionUrl);
				localConnection = createLocalConnection();
			}
			
			progress.execute("Connecting to " + centralParams.connectionUrl);
			centralConnection = createCentralConnection();

			int totalGids = 0;
			for (Connection c : Arrays.asList(centralConnection , localConnection) ) {
				if (c != null) {
					Integer count = SqlUtil.getSingleInteger(c, "SELECT COUNT(*) FROM GERMPLSM");
					if (count != null) {
						totalGids += count;
					}
				}
			}
			progress.execute("Total of " + totalGids + " GERMPLSM records in database");
			
			collectUserTypes(progress);
			
			collectFldnoForGenus(progress);
			
			if (fldNoForGenus==null) {
				StringBuilder sb = new StringBuilder("Missing FLDNO values for:");
				sb.append("\n").append(genusFromSpecies? SPECIES_NAME_CONSTRAINT : TAXONOMY_CONSTRAINT);
				throw new DalDbException(sb.toString());
			}
			
			genusStore = new GenusStore(centralConnection, fldNoForGenus, progress);
			// TODO Determine if need to populate from local database as well

		} catch (SQLException e) {
			errors.add(e);
		} finally {
			if (! errors.isEmpty()) {
				closeConnections();
			}
		}
		
		if (! errors.isEmpty()) {
			// Just the first one!
			throw new DalDbException(errors.get(0));
		}
	}

	public Connection createLocalConnection() throws SQLException {
		return DbUtil.createConnection(localParams);
	}
	
	public Connection createCentralConnection() throws SQLException {
		return DbUtil.createConnection(centralParams);
	}
	
	public Connection[] getConnections() {
		if (localConnection==null) {
			return new Connection[] { centralConnection };
		}
		return new Connection[] { centralConnection, localConnection };
	}
	
	/**
	 * Return the value for ATRIBUTS.atype
	 * @return int
	 */
	int getFldnoForGenus() {
		return fldNoForGenus;
	}

	private void collectUserTypes(Closure<String> progress) throws DalDbException {
		String sql = "SELECT fldno, fcode, fname FROM udflds WHERE ftable='USERS' AND ftype='UTYPE'";
		ResultSetVisitor visitor = new ResultSetVisitor() {
			
			@Override
			public Continue visit(ResultSet rs) {
				try {
					Integer fldno = rs.getInt(1);
					String fcode = rs.getString(2);
					String fname = rs.getString(3);
					
					UdfldsRecord record = new UdfldsRecord(fldno, fcode, fname);
					userTypesByFldno.put(fldno, record);
					
					return Continue.CONTINUE;
				} catch (SQLException e) {
					return Continue.error(e);
				}
			}
		};
		
		Continue cont = SqlUtil.performQuery(centralConnection, sql, visitor);
		if (cont.isError()) {
			throw new DalDbException(cont.throwable);
		}
	}

	protected void collectFldnoForGenus(Closure<String> progress) {
		if (genusFromSpecies) {
			progress.execute("Checking for Species Code FLDNO");
			fldNoForGenus = SqlUtil.getSingleInteger(centralConnection, GET_SPECIES_FLDNO);
			progress.execute("\tfound " + fldNoForGenus);
		}
		else {
			progress.execute("Checking for Taxonomy FLDNO");
			fldNoForGenus = SqlUtil.getSingleInteger(centralConnection, GET_TAXONOMY_FLDNO);
			progress.execute("\tfound " + fldNoForGenus);
		}
	}
	
	private void closeOne(Connection c) {
		if (c != null) {
			try { c.close(); }
			catch (SQLException ignore) {}
		}
	}
	
	public void closeConnections() {
		closeOne(localConnection);
		localConnection = null;
		
		closeOne(centralConnection);
		centralConnection = null;
	}

	public Connection getConnectionFor(String id) {
		return id.startsWith("-") ? localConnection : centralConnection;
	}
}