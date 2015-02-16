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

import java.util.HashMap;
import java.util.Map;

import com.diversityarrays.dal.server.DalSession;

/**
 * Provides a basic implementation of RecordCountCache.
 * @author brian
 *
 */
public class RecordCountCacheImpl implements RecordCountCache {
	
	private Map<String, Map<Class<?>,RecordCountCacheEntry>> mapBySessionId = new HashMap<String, Map<Class<?>,RecordCountCacheEntry>>();

	@Override
	public void removeEntriesFor(DalSession session) {
		mapBySessionId.remove(session.sessionId);
	}
	
	@Override
	public RecordCountCacheEntry getEntry(DalSession session, Class<?> entityClass) {
		Map<Class<?>, RecordCountCacheEntry> map = mapBySessionId.get(session.sessionId);
		return map==null ? null : map.get(entityClass);
	}

	@Override
	public void setEntry(DalSession session, Class<?> entityClass, String filterClause, int count) {
		RecordCountCacheEntry cacheEntry = new RecordCountCacheEntry(filterClause, count);
		Map<Class<?>, RecordCountCacheEntry> map = mapBySessionId.get(session.sessionId);
		if (map == null) {
			map = new HashMap<Class<?>, RecordCountCacheEntry>();
			mapBySessionId.put(session.sessionId, map);
		}
		map.put(entityClass, cacheEntry);
	}

	@Override
	public void tableReceivedUpdate(String tableName) {
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}
}
