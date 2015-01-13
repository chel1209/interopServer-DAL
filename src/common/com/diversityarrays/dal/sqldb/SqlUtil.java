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
package com.diversityarrays.dal.sqldb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.util.Continue;

public class SqlUtil {

	
	static public Logger logger;
	
	/**
	 * Perform a close() on each of the arguments if they are not null.
	 * This is purely a method to reduce copy/paste of its functions.
	 * @param stmt
	 * @param rs
	 */
	static public void closeSandRS(Statement stmt, ResultSet rs) {
		if (rs!=null) {
			try { rs.close(); } catch (SQLException ignore) {}
		}
		if (stmt!=null) {
			try { stmt.close(); } catch (SQLException ignore) {}
		}
	}
	
	static int logIdCount = 0;
	/**
	 * Run the query on the given Connection and visit each ResultSet which is returned.
	 * @param conn
	 * @param sql
	 * @param visitor
	 * @return false if the ResultSetVisitor stopped processing before all ResultSets were visited
	 * @throws SQLException
	 */
	static public Continue performQuery(Connection conn, String sql, ResultSetVisitor visitor) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = createQueryStatement(conn);
			
			int logId = 0;
			if (logger != null) {
				logId = ++logIdCount;
				logger.log(Level.INFO, "SqlUtil.performQuery#" + logId + ": " + sql);
			}

			long startNanos = System.nanoTime();
			rs = stmt.executeQuery(sql);
			long elapsed = System.nanoTime() - startNanos;

			if (logger != null) {
				logger.log(Level.INFO, "SqlUtil.performQuery#" + logId + ": time=" + (elapsed / 1_000_000.0) + " ms");
			}
			
			return visitResults(rs, visitor);
		}
		catch (SQLException e) {
			return Continue.error(e);
		}
		finally {
			closeSandRS(stmt, rs);
		}
	}
	
	/**
	 * Return the status code from Statement.executeUpdate(sql) after calling that method. 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	static public int executeUpdate(Connection conn, String sql) throws SQLException {
		int res = Integer.MIN_VALUE;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			
			int logId = 0;
			if (logger != null) {
				logId = ++logIdCount;
				logger.log(Level.INFO, "SqlUtil.executeUpdate#" + logId + ": " + sql);
			}
			
			long startNanos = System.nanoTime();
			res = stmt.executeUpdate(sql);
			long elapsed = System.nanoTime() - startNanos;

			if (logger != null) {
				logger.log(Level.INFO, "SqlUtil.executeUpdate#" + logId + ": time=" + (elapsed / 1_000_000.0) + " ms");
			}
		} finally {
			closeSandRS(stmt, null);
		}
		return res;
	}
	
	public static int executeUpdate(PreparedStatement stmt) throws DalDbException, SQLException {
		int result = -1;
		
		long startNanos = System.nanoTime();
		boolean status = stmt.execute();
		long elapsed = System.nanoTime() - startNanos;

		if (logger != null) {
			int logId = ++logIdCount;
			logger.log(Level.INFO, "SqlUtil.executeUpdate#" + logId + ": time=" + (elapsed / 1_000_000.0) + " ms");
		}
		
		if (status) {
			throw new DalDbException("PreparedStatement.execute() returned true: "+stmt.toString());
		}
		else {
			result = stmt.getUpdateCount();
		}
		return result;
	}
	
	static public Continue visitResults(ResultSet rs, ResultSetVisitor visitor) {
		try {
			while (rs.next()) {
				Continue c = visitor.visit(rs);
				if (! c.shouldContinue) {
					return c;
				}
			}
		} catch (SQLException e) {
			return Continue.error(e);
		}
		return Continue.CONTINUE;
	}
	


	public static Integer getSingleInteger(Connection conn, String sql) {

		final Integer[] result = new Integer[1];
		
		performQuery(conn, sql, new ResultSetVisitor() {
			@Override
			public Continue visit(ResultSet rs) {
				try {
					result[0] = rs.getInt(1);
				} catch (SQLException e) {
					return Continue.error(e);
				}
				return Continue.STOP;
			}
		});
		
		return result[0];
	}
	
	// For debugging
	/**
	 * 
	 * @param rs
	 * @param headings
	 * @param rows
	 * @return array of maxlengths
	 * @throws SQLException
	 */
	static public int[] collectHeadingsAndRows(ResultSet rs, List<String> headings, List<String[]> rows) throws SQLException {
		int nColumns = -1;

		headings.clear();
		rows.clear();
		
		int maxLength[] = null;
		while (rs.next()) {
			if (nColumns<0) {
				ResultSetMetaData rsmd = rs.getMetaData();
				nColumns = rsmd.getColumnCount();

				maxLength = new int[nColumns];


				for (int i = 1; i <= nColumns; ++i) {
					String hdg = rsmd.getColumnLabel(i);
					headings.add(hdg);
					maxLength[i-1] = hdg.length();
				}
			}
			
			String[] values = new String[nColumns];
			rows.add(values);
			for (int i = 1; i <= nColumns; ++i) {
				String s = rs.getString(i);
				values[i-1] = s;
				if (s!=null) {
					maxLength[i-1] = Math.max(maxLength[i-1], s.length());
				}
			}
		}
		
		return maxLength;
	}
	
	// For debugging
	static public void printWithBars(int[] maxLength, String[] headings, List<String[]> rows) {
		
		int nColumns = headings.length;
		if (nColumns<=0) {
			System.out.println("No rows");
		}
		else {
			System.out.println(rows.size()+" rows returned");
			
			StringBuilder fb = new StringBuilder();
			StringBuilder bar = new StringBuilder();
			String gutter = "";
			String barsep = "";
			for (int max : maxLength)  {
				fb.append(gutter).append("%").append(max).append("s");
				gutter = " |";
				
				bar.append(barsep);
				for (int q = max; --q>=0; ) {
					bar.append('-');
				}
				barsep = "-+";
			}
		
			String format = fb.toString();
			String barline = bar.toString();
			
			System.out.format(format, (Object[]) headings);
			System.out.println();
			System.out.println(barline);
			for (String[] row : rows) {
				System.out.format(format, (Object[]) row);
				System.out.println();
			}
			System.out.println(barline);
		}
		
	}
	
	// No instances allowed
	private SqlUtil() {
	}

	public static Statement createQueryStatement(Connection c) throws SQLException {
		return c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	
}
