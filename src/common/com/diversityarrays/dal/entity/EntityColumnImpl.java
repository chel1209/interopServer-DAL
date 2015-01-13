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
package com.diversityarrays.dal.entity;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Id;

import com.diversityarrays.dal.sqldb.SqlDataType;

/**
 * A simple implementation of EntityColumn.
 * @author brian
 *
 */
public class EntityColumnImpl implements EntityColumn {

	private final Class<? extends DalEntity> entityClass;
	private final Field field;
	private final Column column;
	private boolean primaryKey;
	private SqlDataType sqlDataType;
	
	private String joinTable;
	
	public EntityColumnImpl(Class<? extends DalEntity> c, String fieldName) {
		this.entityClass = c;
		
		try {
			Field f = entityClass.getDeclaredField(fieldName);
			column = f.getAnnotation(Column.class);
			if (column==null) {
				throw new RuntimeException("No @Column annotation on "+f);
			}
			
			joinTable = column.table();
			
			field = f;
			field.setAccessible(true);
			
			primaryKey = (null != f.getAnnotation(Id.class));

			Class<?> fclass = f.getType();
			for (SqlDataType sdt : SqlDataType.values()) {
				if (sdt.valueClass.isAssignableFrom(fclass)) {
					sqlDataType = sdt;
					break;
				}
			}
			
			if (sqlDataType==null) {
				throw new RuntimeException("No SqlDataType has a valueClass of "+fclass.getName());
			}
		} catch (NoSuchFieldException e) {
			// TODO look up the superclass chain stopping when we find DalEntity.class
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return entityClass.getSimpleName() + "." + column.name();
	}
	
	@Override
	public Class<?> getEntityClass() {
		return entityClass;
	}

	@Override
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	
	@Override
	public boolean isJoined() {
		return joinTable != null;
	}
	
	@Override
	public String getJoinTable() {
		return joinTable;
	}

	@Override
	public int getWidth() {
		return column.length();
	}

	@Override
	public String getColumnName() {
		return column.name();
	}

	@Override
	public boolean isRequired() {
		return ! column.nullable();
	}

	@Override
	public SqlDataType getSqlDataType() {
		return sqlDataType;
	}
}