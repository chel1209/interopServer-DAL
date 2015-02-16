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

import com.diversityarrays.dal.server.DalSession;

/**
 * A RecordCountCache supports a performance hack to reduce the time required
 * to respond to LIST commands of the form <code>list/<i>entity</i>/_nPerPage/page/_pageNumber</code>.
 * <p>
 * These flavours of LIST command need to return the total number of records and pages.
 * For implementations of DalDatabase where the database schema is such that complex SQL
 * statements with multiple JOINs are required, the cost of repeatedly obtaining the record
 * count for this purpose may result in poor response times. Caching the value for given
 * DalSession and <i>entity</i> helps to improve this response time.
 * <p>
 * An assumption in the use of RecordCache is that repeated requests for a given entity
 * will be for the same LIST command (with the same <i>Filtering</i> clause) but for a
 * different <code>_pageNumber</code> 
 * <p>
 * When a database schema more closely matches the <i>entity</i> requirements, it may
 * not be necessary to use a <code>RecordCountCache</code>.
 * @author brian
 *
 */
public interface RecordCountCache {

	public void removeEntriesFor(DalSession session);

	public RecordCountCacheEntry getEntry(DalSession session, Class<?> entityClass);

	public void setEntry(DalSession session, Class<?> entityClass, String filterClause, int count);

	public void tableReceivedUpdate(String tableName);


}