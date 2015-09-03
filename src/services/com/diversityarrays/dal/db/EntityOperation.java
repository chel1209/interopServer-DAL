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

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import javax.persistence.Column;

import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.EntityTag;
import com.diversityarrays.dal.ops.AbstractDalOperation;

public abstract class EntityOperation<T extends DalEntity,DB extends DalDatabase> extends AbstractDalOperation<DB> {
	
	protected final Class<? extends T> entityClass;
	protected final EntityProvider<T> entityProvider;
	protected final String entityTagName;
	
	protected final Map<Field,Column> columnByField;
	
	protected NumberFormat doubleFormat = new DecimalFormat("0.000");
	
	public EntityOperation(DB db, String entityName, String template, 
			Class<? extends T> tclass, EntityProvider<T> provider)
	{
		super(db, entityName, template);
		
		this.entityClass = tclass;
		this.entityProvider = provider;
		
		EntityTag entityTag = entityClass.getAnnotation(EntityTag.class);
		if (entityTag == null) {
			throw new RuntimeException(entityClass.getName() + " is missing annotation @" + EntityTag.class.getSimpleName());
		}
		this.entityTagName = entityTag.value();
		
		this.columnByField = DalDatabaseUtil.buildEntityFieldColumnMap(entityClass);
	}

	protected void appendEntity(DalResponseBuilder responseBuilder, T entity)
			throws DalDbException {
		        System.out.println("BEGIN appendEntity in EntityOperation ====");
				DalResponseBuilder builder = responseBuilder.startTag(entityTagName);
				for (Field fld : columnByField.keySet()) {
					try {
						Object value = fld.get(entity);
						String attrValue = "";
						if (value != null) {
							Class<?> ftype = value.getClass();
							if (Double.class == ftype) {
								attrValue = doubleFormat.format(((Double) value).doubleValue());
							}
							else if (Boolean.class == ftype) {
								attrValue = ((Boolean) value).booleanValue() ? "1" : "0";
							}
							else {
								attrValue = value.toString();
							}
						}
						builder.attribute(columnByField.get(fld).name(), attrValue);
					} catch (IllegalArgumentException e) {
						throw new DalDbException(e);
					} catch (IllegalAccessException e) {
						throw new DalDbException(e);
					}
				}
				builder.endTag();
				System.out.println("END appendEntity in EntityOperation ====");
			}

}