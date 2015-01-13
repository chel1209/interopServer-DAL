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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ParameterSetterImpl implements ParameterSetter<PreparedStatement> {
	
	private Integer newRecordId;
	public final List<ParamValue> columnValues;
	
	public ParameterSetterImpl() {
		this(null);
	}
	
	public ParameterSetterImpl(List<ParamValue> paramValues) {
		columnValues = paramValues==null ? new ArrayList<ParamValue>() : paramValues;
	}
	
	@Override
	public void setNewRecordId(int id) {
		newRecordId = id;
	}

	@Override
	public Integer getNewRecordId() {
		return newRecordId;
	}


	@Override
	public PreparedStatement setParameters(PreparedStatement stmt) throws SQLException {
		int parameterIndex = 0;
		
		if (newRecordId!=null) {
			stmt.setInt(++parameterIndex, newRecordId);
		}

		for (ParamValue pvalue : columnValues) {
			++parameterIndex;
		
			Class<?> valueClass = pvalue.sqlDataType.valueClass;
			Object x = pvalue.value;
		
			if (x==null) {
				stmt.setNull(parameterIndex, pvalue.sqlDataType.javaSqlType);
			}
			else {
				if (valueClass==String.class) {
					stmt.setString(parameterIndex, (String) x);
				}
				else if (valueClass==Integer.class) {
					stmt.setInt(parameterIndex, ((Integer) x).intValue());
				}
				else if (valueClass==Boolean.class) {
					stmt.setBoolean(parameterIndex, ((Boolean) x).booleanValue());
				}
				else if (valueClass==Short.class) {
					stmt.setInt(parameterIndex, ((Short) x).shortValue());
				}
				else if (valueClass==Byte.class) {
					stmt.setInt(parameterIndex, ((Byte) x).byteValue());
				}
				else if (valueClass==java.sql.Date.class) {
					stmt.setDate(parameterIndex, (java.sql.Date) x);
				}
				else if (valueClass==Double.class) {
					stmt.setDouble(parameterIndex, ((Double) x).doubleValue());
				}
				else if (valueClass==Float.class) {
					stmt.setFloat(parameterIndex, ((Float) x).floatValue());
				}
				else if (valueClass==java.sql.Time.class) {
					stmt.setTime(parameterIndex, (java.sql.Time) x);
				}
				else if (valueClass==Timestamp.class) {
					stmt.setTimestamp(parameterIndex, (java.sql.Timestamp) x);
				}
				else {
					stmt.setObject(parameterIndex, x);
				}
			}
		}
		
		return stmt;
	}

}