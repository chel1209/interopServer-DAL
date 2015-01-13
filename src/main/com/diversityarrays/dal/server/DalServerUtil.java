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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.db.SqlDalDatabase;
import com.diversityarrays.dal.db.impl.JsonResponseBuilder;
import com.diversityarrays.dal.db.impl.XmlResponseBuilder;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.ops.WordNode;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.dalclient.DALClient;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;

public class DalServerUtil {
	
	static public final int DEFAULT_MAX_INACTIVE_MINUTES = 20;

	static public final int DEFAULT_DAL_SERVER_PORT = 40112; // D=04 A=01 L=12
	
	//
	public static final Response.IStatus ERROR_STATUS = DalResponseBuilder.ERROR_STATUS;

	// Use this for "authorisation" errors
	private static final Response.IStatus AUTH_ERROR_STATUS = Response.Status.UNAUTHORIZED;

	static public Response buildOkResponse(boolean wantJson, String message) {
		return buildResponse(Response.Status.OK, wantJson, message);
	}

	public static Response buildInternalErrorResponse(boolean wantJson, Throwable error) {
		error.printStackTrace();
		return buildInternalErrorResponse(wantJson, error.getMessage());
	}

	static public Response buildInternalErrorResponse(boolean wantJson, String message) {
		return buildResponse(Response.Status.INTERNAL_ERROR, wantJson, message);
	}


	static public Response buildAuthErrorResponse(boolean wantJson, String message) {
		return buildResponse(AUTH_ERROR_STATUS, wantJson, message);
	}

	static public Response buildErrorResponse(boolean wantJson, String message) {
		return buildResponse(ERROR_STATUS, wantJson, message);
	}

	static public Response buildNotFoundResponse(boolean wantJson, String message) {
		return buildResponse(Response.Status.NOT_FOUND, wantJson, message);
	}

	static private Response buildResponse(Response.IStatus status, boolean wantJson, String message) {
		DalResponseBuilder builder = createBuilder(wantJson);

		Response result = builder.startTag(DALClient.TAG_ERROR)
				.attribute(DALClient.ATTR_MESSAGE, message)
				.endTag()
				.build(status);

		return result;
	}

	static public void appendResultSetRowsAsTable(String messageIfEmpty, ResultSet rs, StringBuilder sb) throws SQLException {
		int nColumns = -1;
		boolean[] isdouble = null;
		DecimalFormat dformat = new DecimalFormat("0.000");
		while (rs.next()) {
			if (nColumns<0) {
				ResultSetMetaData rsmd = rs.getMetaData();
				nColumns = rsmd.getColumnCount();
				isdouble = new boolean[nColumns];
				sb.append("<table border='1'><thead><tr>");
				for (int i = 0; i < nColumns; ++i) {
					sb.append("<th>").append(DbUtil.htmlEscape(rsmd.getColumnLabel(i+1))).append("</th>");
					int sqlType = rsmd.getColumnType(i+1);
					isdouble[i] = java.sql.Types.DOUBLE==sqlType; // || java.sql.Types.DECIMAL==sqlType;
				}
				sb.append("</tr></thead><tbody>");
			}

			sb.append("<tr>");
			for (int i = 0; i < nColumns; ++i) {
				String s = rs.getString(i+1);
				if (s!=null && isdouble[i]) {
					try {
						double d = Double.parseDouble(s);
						s = dformat.format(d);
					}
					catch (NumberFormatException ignore) { }
				}
				sb.append("<td>").append(s==null?"":DbUtil.htmlEscape(s)).append("</td>");
			}
			sb.append("</tr>");

		}


		if (nColumns==-1) {
			if (messageIfEmpty!=null) {
				sb.append(messageIfEmpty);
			}
		}
		else {
			sb.append("</tbody></table>");
		}
	}


	static public void collectSqlResults(Connection conn, String sql, String metaTagName, DalResponseBuilder builder) 
			throws DalDbException, SQLException 
	{
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			boolean hasResultSet = stmt.execute(sql);

			if (hasResultSet) {
				rs = stmt.getResultSet();
				DalServerUtil.appendResultSetRows(rs, builder, metaTagName);
			}
			else {
				int n = stmt.getUpdateCount();
				builder.startTag(DALClient.TAG_INFO)
				.attribute(DALClient.ATTR_MESSAGE, "Update Count="+n)
				.endTag();
			}
		} finally {
			SqlUtil.closeSandRS(stmt, rs);
		}
	}

	static public void appendResultSetRows(ResultSet rs, DalResponseBuilder builder, String metaTagName) 
			throws SQLException {

		if (metaTagName!=null) {
			builder.addResponseMeta(metaTagName);
		}

		DecimalFormat dformat = new DecimalFormat("0.000");
		int nColumns = -1;
		String[] columnNames = null;
		boolean[] isdouble = null;
		while (rs.next()) {
			if (nColumns==-1) {
				ResultSetMetaData rsmd = rs.getMetaData();
				nColumns = rsmd.getColumnCount();
				isdouble = new boolean[nColumns];
				columnNames = new String[nColumns];
				for (int i = 0; i < nColumns; ++i) {
					columnNames[i] = rsmd.getColumnLabel(i+1);

					int sqlType = rsmd.getColumnType(i+1);
					isdouble[i] = java.sql.Types.DOUBLE==sqlType; // || java.sql.Types.DECIMAL==sqlType;
				}
			}

			builder.startTag(metaTagName);
			for (int i = 0; i < nColumns; ++i) {
				String s = rs.getString(i+1);
				if (s!=null && isdouble[i]) {
					try {
						double d = Double.parseDouble(s);
						s = dformat.format(d);
					}
					catch (NumberFormatException ignore) { }
				}
				builder.attribute(columnNames[i], s);
			}
			builder.endTag();
		}

	}

	static public DalResponseBuilder createBuilder(boolean wantJson) {
		return wantJson ? new JsonResponseBuilder() : new XmlResponseBuilder();
	}

	static public boolean doInitSql(Connection conn, String sql, boolean asHtml, List<String> messages) {
		boolean result = true;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			messages.add("Problem while creating Statement");
			if (asHtml) {
				messages.add(DbUtil.htmlEscape(e.getMessage()));
			}
			else {
				messages.add(e.getMessage());
			}
			return false;
		}

		try {
			boolean hasResultSet = stmt.execute(sql);
			if (hasResultSet) {
				rs = stmt.getResultSet();

				int nRows = 0;
				StringBuilder tmp = new StringBuilder();
				while (rs.next()) {
					if (nRows==0) {
						ResultSetMetaData rsmd = rs.getMetaData();
						int nColumns = rsmd.getColumnCount();
						for (int i = 0; i < nColumns; ++i) {
							if (i>0) tmp.append(",");
							if (asHtml) {
								tmp.append(DbUtil.htmlEscape(rsmd.getColumnLabel(i+1)));
							}
							else {
								tmp.append(rsmd.getColumnLabel(i+1));
							}
						}
					}
					++nRows;
				}

				messages.add(nRows+" rows of: "+tmp);
			}
			else {
				int n = stmt.getUpdateCount();
				if (n>0) {
					messages.add("update count="+n);
				}
			}
		} catch (SQLException e) {
			System.err.println("SQL: "+sql);
			e.printStackTrace();
			if (asHtml) {
				messages.add("<code>"+DbUtil.htmlEscape(sql)+"</code>");
				messages.add(DbUtil.htmlEscape(e.getMessage()));
			}
			else {
				messages.add(sql);
				messages.add(e.getMessage());
			}
			result = false;
		}
		finally {
			SqlUtil.closeSandRS(stmt, rs);
		}
		return result;
	}

	static private final String[] NON_FIELD_PARAM_NAMES = {
		"NanoHttpd.QUERY_STRING",

		"ctype",
		"xyzzy",

		"url",
		"rand_num",
		"param_order",
		"signature"
	};

	static private final Set<String> NON_FIELD_PARAM_NAME_SET = new HashSet<String>(Arrays.asList(NON_FIELD_PARAM_NAMES)); 


	public static Set<String> createUnusedPostParamKeys(Map<String, String> postParams) {
		Set<String> result = new HashSet<String>(postParams.keySet());
		result.removeAll(NON_FIELD_PARAM_NAME_SET);
		return result;
	}

	static public void buildWordTree(Collection<DalOperation> operations, final WordNode root) {

		for (DalOperation op : operations) {
			String[] parts = op.getCommandTemplate().split("/");

			WordNode node = root;
			for (String p : parts) {
				if (p.startsWith("_")) {
					node.addParameter(p);
				}
				else {
					node = node.getSubNode(p);
				}
			}
			node.setOperation(op);
		}

	}

	static public List<String> collectHostnamesForChoice(boolean includeLocalhost) {
		Set<String> hostnameSet = new TreeSet<String>();
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				Enumeration<InetAddress> ie = e.nextElement().getInetAddresses();
				while (ie.hasMoreElements()) {
					InetAddress ia = ie.nextElement();
					if (! ia.isLoopbackAddress() && ! ia.isMulticastAddress() && ia instanceof Inet4Address) {
						hostnameSet.add(ia.getHostAddress());
					}
				}
			}
		} catch (SocketException ignore) {
		}
		
		List<String> hostnames = new ArrayList<String>();
		if (includeLocalhost) {
			hostnames.add("localhost");
		}
		hostnames.addAll(hostnameSet);
		return hostnames;
	}

	public static Response buildNotSqlDalDatabaseTextResponse(DalDatabase dalDatabase) {
		String message = "Database "+dalDatabase.getDatabaseName()+" is not a " + SqlDalDatabase.class.getSimpleName();
		return new Response(ERROR_STATUS, NanoHTTPD.MIME_PLAINTEXT, message);
	}

}
