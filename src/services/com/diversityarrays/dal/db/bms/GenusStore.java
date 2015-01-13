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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.pearcan.util.StringTemplate;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.bag.HashBag;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.entity.Genus;
import com.diversityarrays.dal.sqldb.ResultSetVisitor;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.util.Continue;

public class GenusStore {

	private Genus UNKNOWN_GENUS;

	static private final String CREATE_GENUS_TABLE = "CREATE TEMPORARY TABLE genus (GenusId int, GenusName VARCHAR(255)"
			+ ", INDEX tx_genus_genusId USING HASH (GenusId)"
			+ ", INDEX tx_genus_genusName USING HASH (GenusName)"
			+ ")";

	static private final String INSERT_GENUS_RECORDS_TEMPLATE = "INSERT INTO genus"
			+ " SELECT MIN(gid) AS GenusId, "
			+ " CASE WHEN INSTR(GenusSpecies,' ')>0 THEN LTRIM(LEFT(GenusSpecies,INSTR(GenusSpecies,' ')))"
			+ " ELSE LTRIM(GenusSpecies)"
			+ " END AS GenusName"
			+ " FROM "
			+ "(SELECT g.gid, UPPER(a.aval) AS GenusSpecies"
			+ " FROM germplsm AS g JOIN atributs AS a ON a.gid=g.gid AND a.atype=${fldno}) AS X"
			+ " GROUP BY GenusName";

	private void createGenusTable(Connection conn, Closure<String> progress)
			throws SQLException {

		progress.execute("Creating Genus TEMPORARY table...");

		SqlUtil.executeUpdate(conn, CREATE_GENUS_TABLE);

		String insertGenusRecords = StringTemplate
				.buildString(INSERT_GENUS_RECORDS_TEMPLATE)
				.replace("fldno", fldNoForGenus).build();

		progress.execute("Populating Genus TEMPORARY table...");
		long startNanos = System.nanoTime();
		int statusCode = SqlUtil.executeUpdate(conn, insertGenusRecords);
		long elapsedNanos = System.nanoTime() - startNanos;
		progress.execute("Population of Genus table took "
				+ (elapsedNanos / 1_000_000.0) + " millis, statusCode="
				+ statusCode);
	}

	// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
	// One way to find the matching Genus record is to use the GENUS_NAME and
	// join on that. Another is to have a GENUS_GENOTYPE table with IDs from
	// both sides of the relation and to join on the IDs.
	// But it takes 24 seconds to populate this G_G table on my old Macbook Pro
	// to I am disabling the creation of the table and doing just using
	// a join on the GenusName.
	// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =

	private static final boolean WANT_GENUS_GENOTYPE_TABLE = false;

	static private final String CREATE_GENUS_GENOTYPE_TABLE = "CREATE TEMPORARY TABLE genus_genotype (GenusId int, GenotypeId int"
			+ ", INDEX tx_gg_genus USING HASH (GenusId), INDEX tx_gg_genotype USING HASH (GenotypeId))";

	static private final String INSERT_GENUS_GENOTYPE_RECORDS_TEMPLATE = "INSERT INTO genus_genotype"
			+ " SELECT genus.GenusId, GenotypeId"
			+ " FROM"
			+ " (SELECT g.gid AS GenotypeId"
			+ ", CONCAT('Germplasm GID ',CAST(g.gid AS CHAR)) AS GenotypeName"
			+ ", CASE WHEN INSTR(a.aval, ' ')>0 THEN UPPER(LTRIM(LEFT(a.aval,INSTR(a.aval,' '))))  ELSE UPPER(LTRIM(a.aval))  END AS GenusName"
			+ ", CASE WHEN INSTR(a.aval, ' ')>0 THEN UPPER(LTRIM(SUBSTR(a.aval,INSTR(a.aval,' ')+1)))  ELSE null  END AS SpeciesName"
			+ " FROM germplsm AS g "
			+ " JOIN atributs AS a ON a.gid=g.gid AND a.atype=${fldno}) AS X"
			+ " JOIN genus ON X.GenusName=genus.GenusName ";

	private void createGenusGenotypeTable(Connection conn,
			Closure<String> progress) throws SQLException {

		progress.execute("Creating Genus_Genotype TEMPORARY table...");

		SqlUtil.executeUpdate(conn, CREATE_GENUS_GENOTYPE_TABLE);

		String insertGGrecords = StringTemplate
				.buildSql(INSERT_GENUS_GENOTYPE_RECORDS_TEMPLATE)
				.replace("fldno", fldNoForGenus).build();
		progress.execute("Populating Genus_Genotype TEMPORARY table...");
		long startNanos = System.nanoTime();
		int statusCode = SqlUtil.executeUpdate(conn, insertGGrecords);
		long elapsedNanos = System.nanoTime() - startNanos;
		progress.execute("Population of Genus_Genotype table took "
				+ (elapsedNanos / 1_000_000.0) + " millis, statusCode="
				+ statusCode);

	}

	// ====================================================

	private final Map<Integer, Genus> genusById = new HashMap<Integer, Genus>();

	private final Bag<Genus> counts = new HashBag<Genus>();

	private final int fldNoForGenus;

	public GenusStore(Connection connection, int fldNoForGenus,
			Closure<String> progress) throws DalDbException, SQLException {
		this.fldNoForGenus = fldNoForGenus;

		createGenusTable(connection, progress);
		if (WANT_GENUS_GENOTYPE_TABLE) {
			createGenusGenotypeTable(connection, progress);
		}

		String sql = "SELECT GenusId, GenusName FROM genus";
		Continue cont = SqlUtil.performQuery(connection, sql,
				new ResultSetVisitor() {
					@Override
					public Continue visit(ResultSet rs) {
						Genus g = new Genus();
						try {
							g.setGenusId(rs.getInt(1));
							g.setGenusName(rs.getString(2));
							genusById.put(g.getGenusId(), g);

							if ("UNKNOWN".equalsIgnoreCase(g.getGenusName())) {
								if (UNKNOWN_GENUS != null) {
									return Continue.error(new Exception(
											"Multiple 'UNKNOWN' Genus: id="
													+ UNKNOWN_GENUS
															.getGenusId()
													+ " and " + g.getGenusId()));
								}
								UNKNOWN_GENUS = g;
							}
						} catch (SQLException e) {
							return Continue.error(e);
						}
						return Continue.CONTINUE;
					}
				});

		if (cont.isError()) {
			throw new DalDbException(cont.throwable);
		}

		if (UNKNOWN_GENUS == null) {
			UNKNOWN_GENUS = new Genus();
			UNKNOWN_GENUS.setGenusId(0);
			UNKNOWN_GENUS.setGenusName("UNKNOWN");
			// TODO insert this into the table too!
		}
	}

	public int getFldnoForGenus() {
		return fldNoForGenus;
	}

	protected void report(Closure<String> progress) {
		progress.execute("Found " + genusById.size() + " Genus records");
		int total = 0;
		for (Genus g : counts.uniqueSet()) {
			int n = counts.getCount(g);
			total += n;
			progress.execute("\t" + n + ": " + g.getGenusName());
		}
		progress.execute("Found total of " + total + " GIDs with Genus");
	}

	public Collection<Genus> getGenusValues() {
		return genusById.values();
	}

	public Genus getGenusById(String idString) {
		Genus result = null;
		try {
			result = genusById.get(new Integer(idString));
		} catch (NumberFormatException e) {
		}
		return result;
	}

	public int getGenusCount() {
		return genusById.size();
	}

}
