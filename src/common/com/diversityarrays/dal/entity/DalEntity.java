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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the super class for all entities that are served by a DalServer/DalDatabase.
 * @author brian
 *
 */
public abstract class DalEntity {
	
	static private Map<Class<? extends DalEntity>,Map<String,EntityColumn>> ENTITY_COLUMN_MAP_BY_CLASS = new HashMap<Class<? extends DalEntity>, Map<String,EntityColumn>>();
	
	static protected EntityColumn createEntityColumn(Class<? extends DalEntity> eclass, String fieldName) {
		EntityColumn result = new EntityColumnImpl(eclass, fieldName);
		
		Map<String, EntityColumn> entityColumnByFieldName = ENTITY_COLUMN_MAP_BY_CLASS.get(eclass);
		if (entityColumnByFieldName==null) {
			entityColumnByFieldName = new LinkedHashMap<String, EntityColumn>();
			ENTITY_COLUMN_MAP_BY_CLASS.put(eclass, entityColumnByFieldName);
		}
		else {
			if (entityColumnByFieldName.containsKey(fieldName)) {
				throw new RuntimeException("Duplicate definition of field '"+fieldName+"' for "+eclass.getName());
			}
		}
		entityColumnByFieldName.put(fieldName, result);
		return result;
	}
	

	public static EntityColumn[] getEntityColumns(Class<? extends DalEntity> cls) {
		Map<String, EntityColumn> map = ENTITY_COLUMN_MAP_BY_CLASS.get(cls);
		
		// TODO check if the return order is the same as the key order
		List<EntityColumn> tmp = new ArrayList<EntityColumn>();
		if (map != null) {
			tmp.addAll(map.values());
		}
		return tmp.toArray(new EntityColumn[tmp.size()]);
	}


	DalEntity() {
	}
	
}
