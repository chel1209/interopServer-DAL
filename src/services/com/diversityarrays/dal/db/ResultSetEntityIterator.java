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

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.sqldb.SqlUtil;

public class ResultSetEntityIterator<T extends DalEntity> implements EntityIterator<T>, Closeable {

	private final Statement stmt;
	private final ResultSet rs;
	
	private final EntityFactory<T> tfactory;
	
	private boolean noMore;
	
	public ResultSetEntityIterator(Statement s, ResultSet r, EntityFactory<T> tfactory) throws SQLException {
		stmt = s;
		rs = r;
		this.tfactory = tfactory;
		
	}


	@Override
	public void close() throws IOException {
		try {
			SqlUtil.closeSandRS(stmt, rs);
		}
		finally {
			tfactory.close();
		}
	}

	@Override
	public T nextEntity() throws DalDbException {
		T result = null;
		if (! noMore) {
			try {
				if (rs.next()) {
					result = tfactory.createEntity(rs);
				}
				else {
					noMore = true;
				}
			} catch (SQLException e) {
				throw new DalDbException(e);
			}
		}
		return result;
	}

}