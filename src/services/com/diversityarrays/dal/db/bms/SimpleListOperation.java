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
package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityIterator;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

class SimpleListOperation<T extends DalEntity> extends EntityOperation<T,BMS_DalDatabase> {

	public static final Pattern PATTERN = Pattern.compile("^list/([a-z]+)$");

	public static String getEntityName(Matcher m) {
		return m.group(1);
	}

	public SimpleListOperation(BMS_DalDatabase db, String entityName, 
			Class<? extends T> tclass, EntityProvider<T> provider) 
	{
		super(db, entityName, "list/" + entityName, tclass, provider);
	}

	@Override
	public void execute(DalSession session, 
			DalResponseBuilder responseBuilder,
			Method method,
			String dalcmd, 
			List<String> dalOpParameters,
			Map<String, String> methodParms,
			Map<String, String> filePathByName) 
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);

		responseBuilder.addResponseMeta(entityTagName);
		
		EntityIterator<? extends T> iter = entityProvider.createIterator(0, 0, filterClause);
		try {
			T entity;
			while (null != (entity = iter.nextEntity())) {
				appendEntity(responseBuilder, entity);
			}
		} finally {
			if (iter != null) {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}
	}
}