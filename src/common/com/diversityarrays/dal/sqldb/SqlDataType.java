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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.diversityarrays.dal.db.DalDbException;

public enum SqlDataType {
	INTEGER(Integer.class, java.sql.Types.INTEGER, true),
	SMALLINT(Short.class, java.sql.Types.SMALLINT, true),
	TINYINT(Short.class, java.sql.Types. TINYINT, true, "SMALLINT"),
	FLOAT(Float.class, java.sql.Types.FLOAT, true),
	DOUBLE(Double.class, java.sql.Types.DOUBLE, true),
	DECIMAL(Double.class, java.sql.Types.DECIMAL, true),
	CHAR(String.class, java.sql.Types.CHAR),
	VARCHAR(String.class, java.sql.Types.VARCHAR),
	DATE(java.sql.Date.class, java.sql.Types.DATE),
	TIME(java.sql.Time.class, java.sql.Types.TIME),
	TIMESTAMP(java.sql.Timestamp.class, java.sql.Types.TIMESTAMP),
	BOOLEAN(Boolean.class, java.sql.Types.BOOLEAN);
	
	public static SqlDataType lookup(int sqlType) {
		for (SqlDataType sdt : values()) {
			if (sdt.javaSqlType==sqlType) {
				return sdt;
			}
		}
		return null;
	}
	

	public final Class<?> valueClass;
	public final int javaSqlType;
	public final boolean numeric;
	private final String derbyName;
	
	SqlDataType(Class<?> clazz, int javaSqlType) {
		this(clazz, javaSqlType, false, null);
	}
	
	SqlDataType(Class<?> clazz, int javaSqlType, boolean numeric) {
		this(clazz, javaSqlType, numeric, null);
	}
	
	SqlDataType(Class<?> clazz, int javaSqlType, boolean numeric, String derbyName) {
	    this.valueClass = clazz;
	    this.javaSqlType = javaSqlType;
	    this.numeric = numeric;
	    this.derbyName = derbyName;
	}
	
	public boolean getValueNeedsQuotes() {
		return this==CHAR || this==DATE || this==TIME || this==TIMESTAMP || this==VARCHAR;
	}
	
	public String getDerbyName() {
		return derbyName==null ? name() : derbyName;
	}

	public boolean isFloatOrDouble() {
		return this==FLOAT || this==DOUBLE || this==DECIMAL;
	}
	
	public boolean isNumeric() {
		return numeric;
	}
	
	public Object convertValue(String s) throws DalDbException {
		try {
			return convertValueImpl(s);
		}
		catch (NumberFormatException e) {
			throw new DalDbException(e);
		}
		catch (ParseException e) {
			throw new DalDbException(e);
		}
	}
	
	private Object convertValueImpl(String s) throws ParseException {
		Object result = s;
		if (s!=null) {
			if ("NULL".equals(s)) {
				result = null;
			}
			else if (CHAR==this || VARCHAR==this) {
				// already done
			}
			else if (INTEGER==this) {
				result = s.isEmpty() ? null : new Integer(s);
			}
			else if (SMALLINT==this) {
				result = s.isEmpty() ? null : new Short(s);
			}
			else if (FLOAT==this) {
				result = s.isEmpty() ? null : new Float(s);
			}
			else if (DOUBLE==this || DECIMAL==this) {
				result = s.isEmpty() ? null : new Double(s);
			}
			else if (DATE==this) {
				if (s.isEmpty()) {
					result = null;
				}
				else {
					java.util.Date dt = null;
					for (DateFormat df : getDateFormat(this.valueClass)) {
						try {
							dt = df.parse(s);
							result = new java.sql.Date(dt.getTime());
						} catch (ParseException e) {
//							System.out.println(e.getMessage());
						}
					}
					if (dt==null) {
						throw new ParseException("Unsupported format for Date: "+s, 0);
					}
				}
			}
			else if (TIME==this) {
				if (s.isEmpty()) {
					result = null;
				}
				else {
					java.util.Date dt = null;
					for (DateFormat df : getDateFormat(this.valueClass)) {
						try {
							dt = df.parse(s);
							result = new java.sql.Time(dt.getTime());
						} catch (ParseException e) {
						}
					}
					if (dt==null) {
						throw new ParseException("Unsupported format for Time: "+s, 0);
					}
				}
			}
			else if (TIMESTAMP==this) {
				if (s.isEmpty()) {
					result = null;
				}
				else if  ("NOW()".equals(s)) {
					result = new java.sql.Timestamp(new java.util.Date().getTime());
				}
				else {
					java.util.Date dt = null;
					for (DateFormat df : getDateFormat(this.valueClass)) {
						try {
							dt = df.parse(s);
							result = new java.sql.Timestamp(dt.getTime());
						} catch (ParseException e) {
						}
					}
					if (dt==null) {
						throw new ParseException("Unsupported format for Timestamp: "+s, 0);
					}
				}
			}
			else if (BOOLEAN==this) {
				if ("0".equals(s)) {
					result = Boolean.FALSE;
				}
				else if ("1".equals(s)) {
					result = Boolean.TRUE;
				}
				else {
					result = new Boolean(s);
				}
			}
			else {
				throw new RuntimeException("Unsupported "+SqlDataType.class.getName()+"("+this+")");
			}
		}
		return result;
	}
	
	static ThreadLocal<DateFormat[]> J_S_DATE_FORMAT = new ThreadLocal<DateFormat[]>() {
		@Override
		protected DateFormat[] initialValue() {
			return new DateFormat[] { new SimpleDateFormat("yyyy-MM-dd") };
		}
	};
	static ThreadLocal<DateFormat[]> J_S_TIME_FORMAT = new ThreadLocal<DateFormat[]>() {
		@Override
		protected DateFormat[] initialValue() {
			return new DateFormat[] { new SimpleDateFormat("HH:mm:ss") };
		}
	};
	static ThreadLocal<DateFormat[]> J_S_TIMESTAMP_FORMAT = new ThreadLocal<DateFormat[]>() {
		@Override
		protected DateFormat[] initialValue() {
			return new DateFormat[] {
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
					new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss"),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
					
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
					new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss"),
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"),

					new SimpleDateFormat("yyyy-MM-dd"), // Used when loading DemoData
					};
		}
	};
	
	static DateFormat[] getDateFormat(Class<?> clazz) {
		ThreadLocal<DateFormat[]> threadLocal = null;
		if (java.sql.Date.class==clazz) {
			threadLocal = J_S_DATE_FORMAT;
		}
		else if (java.sql.Time.class==clazz) {
			threadLocal = J_S_TIME_FORMAT;
		}
		else if (java.sql.Timestamp.class==clazz) {
			threadLocal = J_S_TIMESTAMP_FORMAT;
		}
		if (threadLocal==null) {
			throw new RuntimeException("No DateFormat available for "+clazz.getName());
		}
		return threadLocal.get();
	}

	static private Map<String,Class<?>> DATATYPENAME_TO_CLASS = new HashMap<String,Class<?>>();
	static {
		for (SqlDataType sdt : values()) {
			DATATYPENAME_TO_CLASS.put(sdt.name().toUpperCase(), sdt.valueClass);
		}
	}


}