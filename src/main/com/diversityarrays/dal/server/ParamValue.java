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

import java.util.HashMap;
import java.util.Map;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.sqldb.SqlDataType;

public class ParamValue {
	public static final String DT_VARCHAR = "VARCHAR";
	public static final String DT_SMALLINT = "SMALLINT";
	public static final String DT_CHAR = "CHAR";
	
	public final SqlDataType sqlDataType;
	public final Object value;
	
	public ParamValue(SqlDataType sdt, Object v) {
		this.sqlDataType = sdt;
		this.value = v;
	}
	
	@Override
	public String toString() {
		return sqlDataType.name()+"("+value+")";
	}

	public static ParamValue construct(SqlDataType sdt, String value) throws DalDbException {
		Object pvalue = sdt.convertValue(value);
		return new ParamValue(sdt, pvalue);
	}
	
	static private Map<String,Class<?>> DATATYPENAME_TO_CLASS = new HashMap<String,Class<?>>();
	static private Map<String,Integer> DATATYPENAME_TO_SQLTYPE = new HashMap<String,Integer>();
	static private Map<Class<?>,Integer> CLASS_TO_SQLTYPE = new HashMap<Class<?>,Integer>();
	static void x(Class<?> clazz, String s, int javaSqlType) {
		DATATYPENAME_TO_CLASS.put(s, clazz);
		DATATYPENAME_TO_SQLTYPE.put(s, javaSqlType);
		CLASS_TO_SQLTYPE.put(clazz, javaSqlType);
	}
	static {
		x(Integer.class, "INTEGER", java.sql.Types.INTEGER);
		x(Short.class, DT_SMALLINT, java.sql.Types.SMALLINT);
		x(Float.class, "FLOAT", java.sql.Types.FLOAT);
		x(Double.class, "DOUBLE", java.sql.Types.DOUBLE);
		x(String.class, DT_CHAR, java.sql.Types.CHAR);
		x(String.class, DT_VARCHAR, java.sql.Types.VARCHAR);
		x(java.sql.Date.class, "DATE", java.sql.Types.DATE);
		x(java.sql.Time.class, "TIME", java.sql.Types.TIME);
		x(java.sql.Timestamp.class, "TIMESTAMP", java.sql.Types.TIMESTAMP);
		x(Boolean.class, "BOOLEAN", java.sql.Types.BOOLEAN);


	}
	
	/*
	BIT
	TINYINT
	SMALLINT
	INTEGER
	BIGINT

	FLOAT
	REAL
	DOUBLE

	NUMERIC
	DECIMAL

	CHAR
	VARCHAR
	LONGVARCHAR

	DATE
	TIME
	TIMESTAMP

	BINARY
	VARBINARY
	LONGVARBINARY

	NULL
		// OBJECT
	OTHER
		// JAVA_OBJECT
	JAVA_OBJECT

	DISTINCT
	STRUCT

	ARRAY

	BLOB
	CLOB

	REF

	DATALINK

	BOOLEAN

	ROWID

	NCHAR
	NVARCHAR
	LONGNVARCHAR
	NCLOB

	SQLXML
	*/

}