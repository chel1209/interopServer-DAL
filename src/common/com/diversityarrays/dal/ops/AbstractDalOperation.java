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
package com.diversityarrays.dal.ops;

import java.util.ArrayList;
import java.util.List;

import com.diversityarrays.dal.db.DalDatabase;

/**
 * Super class for DalOperation implementations for all the "common stuff".
 * @author brian
 *
 * @param <T> specifies the DalDatabase type
 */
public abstract class AbstractDalOperation<T extends DalDatabase> implements DalOperation {
	
	protected final T context;
	private final String entityName;
	private final String commandTemplate;
	private final List<String> parameters = new ArrayList<String>();

	public AbstractDalOperation(T context, String entityName, String template) {
		this.context = context;
		this.entityName = entityName;
		this.commandTemplate = template;
		String[] parts = commandTemplate.split("/", 0);
		for (String p : parts) {
			if (p.startsWith("_")) {
				parameters.add(p);
			}
		}
	}

	@Override
	public String getCommandTemplate() {
		return commandTemplate;
	}

	@Override
	public int getParameterCount() {
		return parameters.size();
	}

	@Override
	public String getParameter(int index) throws IndexOutOfBoundsException {
		return parameters.get(index);
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

}
