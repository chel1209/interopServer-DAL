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
package com.diversityarrays.dal.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pearcan.db.DbException;

import org.apache.commons.collections15.Transformer;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.db.SqlDalDatabase;
import com.diversityarrays.dal.sqldb.ResultSetVisitor;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.util.Continue;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;

public class SqlWorker {
	
	static private final Pattern INSERT_OR_DELETE = Pattern.compile("^(INSERT +INTO|DELETE +FROM) +(\\w*)\\W", Pattern.CASE_INSENSITIVE);

	
	private SqlDalDatabase db;
	
	private Connection connection;

	private boolean testOnly;
	
	private boolean verbose;
	
	public SqlWorker(SqlDalDatabase db) {
		this.db = db;
	}
	
	public void setVerbose(boolean b) {
		verbose = b;
	}
	
	public void setTestOnly(boolean b) {
		this.testOnly = b;
	}
	
	public int getRecordCount(String tableName, String whereClause) throws DalDbException
	{
		StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM ");
		sb.append(tableName);
		if (whereClause!=null) {
			sb.append(" WHERE ").append(whereClause);
		}
		String sql = sb.toString();
		Integer value = SqlUtil.getSingleInteger(getConnection(), sql);
		return value == null ? 0 : value.intValue();
	}

	public Connection getConnection() throws DalDbException {
		if (connection==null) {
			connection = db.getConnection(false);
		}
		return connection;
	}
	
	public void close() {
		if (connection!=null) {
			Connection conn = connection;
			connection = null;
			try { conn.close(); } catch (SQLException ignore) {}
		}
	}

	
	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		}
		finally {
			super.finalize();
		}
	}
	
	public Response doDalDatabaseQuery(boolean wantJson, String sql, ResultSetVisitor visitor) {
		Response result = null;
		
		Throwable error = null;
		try {
			Connection conn = getConnection();
			Continue sc = SqlUtil.performQuery(conn, sql, visitor);
			if (sc.isError()) {
				error = sc.throwable;
			}
		} catch (DalDbException e) {
			error = e;
		}

		if (error!=null) {
			error.printStackTrace();
			result = DalServerUtil.buildInternalErrorResponse(wantJson, error);
		}
		return result;
	}
	
	/**
	 * Return non-null Response if an error occurred else null.
	 * @param wantJson
	 * @param update
	 * @param parameterSetter
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Response doDalDatabaseUpdate(boolean wantJson, String update, ParameterSetter parameterSetter) {
		Response result = null;
		
		Throwable error = null;
		try {
//			int nUpdates = 
			executeUpdate(update, parameterSetter);
		} catch (DalDbException e) {
			error = e;
		}
		
		if (error!=null) {
			error.printStackTrace();
			result = DalServerUtil.buildInternalErrorResponse(wantJson, error);
		}
		return result;
	}
	
	/**
	 * 
	 * @param wantJson
	 * @param upd
	 * @return null if ok else error Response
	 * @throws SQLException
	 * @throws DbException
	 */
	public Response executeUpdate(boolean wantJson, String upd) {
		Response result = null;
		boolean success = false;
		Throwable error = null;
		try {
			if (! testOnly) {
				Connection conn = getConnection();
				SqlUtil.executeUpdate(conn, upd);
				success = true;
			}

		} catch (DalDbException e) {
			error = e;
		} catch (SQLException e) {
			error = e;
		} finally {
			if (success) {
				updateRecordCountCacheForINSERTorDELETE(upd);
			}
		}
		
		if (error!=null) {
			error.printStackTrace();
			result = DalServerUtil.buildInternalErrorResponse(wantJson, error);
		}
		return result;
	}

	public void updateRecordCountCacheForINSERTorDELETE(String upd) {
		Matcher m = INSERT_OR_DELETE.matcher(upd);
		if (m.matches()) {
			String tableName = m.group(2);
			db.getRecordCountCache().tableReceivedUpdate(tableName);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int executeUpdate(String upd, ParameterSetter parameterSetter) throws DalDbException {
		boolean success = false;
		PreparedStatement stmt = null;
		try {
			Connection conn = getConnection();
			stmt = conn.prepareStatement(upd);
			parameterSetter.setParameters(stmt);
			
			int result = -1;
			if (! testOnly) {
				result = SqlUtil.executeUpdate(stmt);
			}
			success = true;
			return result;
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		finally {
			SqlUtil.closeSandRS(stmt, null);
			if (success) {
				updateRecordCountCacheForINSERTorDELETE(upd);
			}
		}
	}

	public Response createResponse(boolean wantJson, String sql, String metaTagName, Transformer<Boolean,DalResponseBuilder> builderFactory) {
		return createResponse(wantJson ? SqlResponseType.JSON : SqlResponseType.XML, sql, metaTagName, builderFactory);
	}
	
	public Response createResponse(SqlResponseType rtype, String sql, String metaTagName, 
			Transformer<Boolean,DalResponseBuilder> builderFactory) 
	{
		
		if (metaTagName==null && ! rtype.isText()) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			new IllegalArgumentException("No metaTagName supplied for rtype="+rtype).printStackTrace(pw);
			pw.close();
			return new Response(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, sw.toString());
		}
		
		Response result;
		
		Statement stmt = null;
		ResultSet rs = null;
		try {

			Connection conn = getConnection();
			
			stmt = conn.createStatement();
			
			if (rtype.isText()) {
				StringBuilder sb = new StringBuilder("<html><body>");
				
				sb.append("<code>").append(DbUtil.htmlEscape(sql)).append("</code><hr/>");

				boolean hasResultSet = stmt.execute(sql);
					
				if (hasResultSet) {
					rs = stmt.getResultSet();
					DalServerUtil.appendResultSetRowsAsTable("No data rows returned", rs, sb);
					sb.append("</body></html>");
				}
				else {
					int n = stmt.getUpdateCount();
					sb.append("Update count=").append(n);
				}
				
				result = new Response(Response.Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
			}
			else {
				if (verbose) {
					System.err.println("sql: "+sql);
				}

				DalResponseBuilder builder = builderFactory==null ? DalServerUtil.createBuilder(rtype.isJson()) : builderFactory.transform(rtype.isJson());
				boolean hasResultSet = stmt.execute(sql);
				
				if (hasResultSet) {
					rs = stmt.getResultSet();
					DalServerUtil.appendResultSetRows(rs, builder, metaTagName);
					result = builder.build(Response.Status.OK);	
				}
				else {
					int n = stmt.getUpdateCount();
					builder.startTag(DALClient.TAG_INFO)
						.attribute(DALClient.ATTR_MESSAGE, "Update Count="+n)
						.endTag();
					result = builder.build(Response.Status.OK);
				}
			}
		} catch (SQLException e) {
			// Once for the log
			e.printStackTrace();
			
			if (SqlResponseType.TEXT==rtype) {
				// Browser request gets it all as text
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				pw.close();
				result = new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, sw.toString());
			}
			else {
				// DAL client gets just the message part
				result = DalServerUtil.buildErrorResponse(SqlResponseType.JSON==rtype, e.getMessage());
			}
		} catch (DalDbException e) {
			result = DalServerUtil.buildErrorResponse(SqlResponseType.JSON==rtype, e.getMessage());
		} finally {
			SqlUtil.closeSandRS(stmt, rs);
		}
		
		return result;
	}


	public int performUpdate(String update) throws DalDbException {
		int count = -1;
		Connection conn = getConnection();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(update);
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		finally {
			SqlUtil.closeSandRS(stmt, rs);
		}
		
		return count;
	}
}