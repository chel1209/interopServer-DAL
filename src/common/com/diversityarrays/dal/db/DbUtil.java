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
package com.diversityarrays.dal.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import com.diversityarrays.dal.entity.EntityColumn;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;

public class DbUtil {
	
	// No instances allowed
	private DbUtil() {
	}
	
	static public Connection createConnection(JdbcConnectionParameters params) throws SQLException {
		return createConnection(params.connectionUrl, params.userName, params.password, params.properties);
	}
	
	static public Connection createConnection(String url, String userName, String password, Properties properties) 
	throws SQLException 
	{
		Connection conn;
        if (properties != null) {
            try {
                conn = DriverManager.getConnection(url, properties);
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
                try {
                    conn = DriverManager.getConnection(url, userName, new String(password));
                    conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                } catch (Exception ex) {
                    conn = DriverManager.getConnection(url);
                    conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                }
            }
        } else if (userName != null && password != null) {
        	// No properties, see if username/password are both supplied

        	try {
        		conn = DriverManager.getConnection(url, userName, password==null ? null : new String(password));
        	} catch (Exception e) {
        		//                    LOG.log(Level.WARNING, "Connection with properties failed. " +
        		//                                    "Trying to connect without.", e);
        		//try to connect without using the userName and password
        		conn = DriverManager.getConnection(url);
        	}
        }
        else {
        	// Ok - just do it with the url 
        	conn = DriverManager.getConnection(url);
        }
        
        return conn;
	}
	
	static public Integer getIntegerParameter(int pIndex, List<String> parameters) {
		Integer result = null;
		String tmp = parameters.get(pIndex);
		if (tmp!=null) {
			try {
				result = new Integer(tmp);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	static public String getStringParameter(int pIndex, Map<String,String> parameters) {
		String result = null;
		String tmp = parameters.get(pIndex);
		if (tmp!=null && ! tmp.isEmpty()) {
			result = tmp;
		}
		return result;
	}

	/**
	 * Extract the individual Strings from a parenthesized and comma-separated
	 * list of fragments that are individually quotes with single quotes.
	 * @param params
	 * @return
	 */
	public static Set<String> getSetValues(String params) {
		Pattern parensPattern = Pattern.compile("^\\(.*\\)$");
		if (! parensPattern.matcher(params).matches()) {
			throw new IllegalArgumentException("Set values are not in correct format:"+params);
		}

		String values = params.substring(1, params.length()-1);
		String[] vparts = values.split(",");

		Pattern qPattern = Pattern.compile("^'[^']*'$");

		Set<String> result = new HashSet<String>();
		for (String v : vparts) {
			v = v.trim();
			if (! qPattern.matcher(v).matches()) {
				throw new IllegalArgumentException("Invalid value in set:"+v+" SET="+params);
			}
			result.add(v.substring(1, v.length()-1));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String join(String delim, Object ... parts) {
		StringBuilder sb = new StringBuilder();
		if (parts!=null) {
			// Handle the call to the wrong overload!
			if (parts.length==1 && parts[0]!=null && (Iterable.class.isAssignableFrom(parts[0].getClass()))) {
				return join(delim, (Iterable) parts[0]);
			}
			String d = "";
			for (Object p : parts) {
				sb.append(d);
				if (p!=null)
					sb.append(p);
				d = delim;
			}
		}
		return sb.toString();
	}

	public static String join(String delim, Iterable<Object> parts) {
		StringBuilder sb = new StringBuilder();
		if (parts != null) {
			String d = "";
			for (Object p : parts) {
				sb.append(d);
				if (p!=null) {
					sb.append(p);
				}
				d = delim;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convert &amp;, &quot;, &lt; and &gt; to their respective HTML escape sequences. 
	 * @param input
	 * @return a String
	 */
	static public String htmlEscape(String input) {
		if (input==null)
			return "";
		String res = input;
		res = res.replaceAll("&", "&amp;");
		//		res = res.replaceAll("'", "&apos;");
		res = res.replaceAll("\"", "&quot;");
		res = res.replaceAll("<", "&lt;");
		res = res.replaceAll(">", "&gt;");
		return res;
	}
	
	/**
	 * Returns the input string with all occurrences of "'" replaced by two occurrences of the character.
	 * This is intended for use when assembling SQL queries.
	 * @param input
	 * @return a String
	 */
	public static String doubleUpSingleQuote(String input) {
		String result = input;
		if (result.indexOf('\'')>=0) {
			result = result.replaceAll("'", "''");
		}
		return result;
	}
	
	/**
	 * Return the substring before the first occurrence of SUB else INPUT (if substring does not occur).
	 * @param input
	 * @param sub
	 * @return a String
	 */
	static public String substringBefore(String input, String sub) {
		int pos = input.indexOf(sub);
		if (pos<0) {
			return input;
		}
		return input.substring(0, pos);
	}

	/**
	 * Return the substring after the first occurrence of SUB else "" (if substring does not occur).
	 * @param input
	 * @param sub
	 * @return a String - possibly ""
	 */
	static public String substringAfter(String input, String sub) {
		int pos = input.indexOf(sub);
		if (pos<0) {
			return "";
		}
		return input.substring(pos+sub.length());
	}

	// TODO Find a better place for this

	static public String getDatatypeForListField(EntityColumn c) {
		String result = c.getSqlDataType().name().toLowerCase();
		if ("smallint".equals(result)) {
			result = "tinyint";
		}
		return result;
	}

}
