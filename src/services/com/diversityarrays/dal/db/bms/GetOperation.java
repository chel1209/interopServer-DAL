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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;

import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

class GetOperation<T extends DalEntity> extends EntityOperation<T,BMS_DalDatabase> {

	public static final Pattern PATTERN = Pattern.compile("^get/([a-z]+)/_id$");
	
	public static String getEntityName(Matcher m) {
		return m.group(1);
	}
	
	public GetOperation(BMS_DalDatabase db, String entityName, String template,
			Class<? extends T> tclass, EntityProvider<T> provider)
	{
		super(db, entityName, template, tclass, provider);
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

		String id = dalOpParameters.get(0);
		
		// TODO implement filtering translation (in entityProvider)
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);

		T entity = entityProvider.getEntity(id, filterClause);
		
		if (entity != null) {
			
			DalResponseBuilder builder = responseBuilder
					.addResponseMeta(entityTagName);
					
			builder.startTag(entityTagName);
			
			for (Field fld : columnByField.keySet()) {
				Column column = columnByField.get(fld);
				
				try {
					Object value = fld.get(entity);
					builder.attribute(column.name(), value==null ? "" : value.toString());
				} catch (IllegalArgumentException e) {
					throw new DalDbException(e);
				} catch (IllegalAccessException e) {
					throw new DalDbException(e);
				}
				
			}
			builder.endTag();
		}
	}
	
}