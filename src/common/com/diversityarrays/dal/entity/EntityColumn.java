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

import com.diversityarrays.dal.sqldb.SqlDataType;

/**
 * Collates the information about an attribute (field/column) of a DalEntity.
 * @author brian
 *
 */
public interface EntityColumn {

	public Class<?> getEntityClass();

	public boolean isPrimaryKey();

	public int getWidth();

	public String getColumnName();

	public boolean isRequired();

	public SqlDataType getSqlDataType();

	public boolean isJoined();
	public String getJoinTable();

}