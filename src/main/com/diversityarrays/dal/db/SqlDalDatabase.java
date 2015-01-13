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

/**
 * A DalDatabase that sits on top of a JDBC database.
 * @author brian
 *
 */
public interface SqlDalDatabase extends DalDatabase {



	/**
	 * Return the current JDBC Connection for this DalDatabase if it exists or <code>createIfNotPresent</code>
	 * is true. Otherwise, return <code>null</code>.
	 * @param createIfNotPresent
	 * @return Connection or null
	 * @throws DalDbException
	 */
	public Connection getConnection(boolean createIfNotPresent) throws DalDbException;
	
	/**
	 * Return the RecordCountCache for this database. The RecordCountCache is used
	 * to save the number of records for each table as a performance hack.
	 * @return RecordCountCache
	 */
	public RecordCountCache getRecordCountCache();
	
	/**
	 * Return SQL statement that lists the entity tables for the database.
	 * @return String
	 */
	public String createShowTablesSql();
	
	/**
	 * Return SQL statement that lists the columns of a specified table.
	 * @param tableName
	 * @return String
	 */
	public String createShowTableColumnsSql(String tableName);

//	public SqlDatabaseHelper getSqlDatabaseHelper();


}
