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
package com.diversityarrays.dal.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides the base for implementations of DalDbProviderService that use a
 * JDBC connection to the database entities.
 * @author brian
 *
 */
public abstract class AbstractJdbcDalDbProviderService extends
		AbstractDalDbProviderService {

	public AbstractJdbcDalDbProviderService(String name) {
		super(name);
	}

	@Override
	public Set<Parameter<?>> getParametersRequired() {
		return new LinkedHashSet<Parameter<?>>(Arrays.asList(Parameters.JDBC_PARAMETERS));
	}
	
	protected Map<Parameter<?>,String> getJdbcParameterValues(Collection<ParameterValue<?>> parameterValues) {
		Map<Parameter<?>,String> result = new HashMap<Parameter<?>, String>();
		for (Parameter<?> p : getParametersRequired()) {
			Object value = ParameterValue.getValue(p, parameterValues);
			if (value != null) {
				result.put(p, value.toString());
			}
		}
//		for (StringParameter sp : Parameters.JDBC_PARAMETERS) {
//			String value = ParameterValue.getValue(sp, parameterValues);
//			if (value != null) {
//				result.put(sp, value);
//			}
//		}
		return result;
	}

}
