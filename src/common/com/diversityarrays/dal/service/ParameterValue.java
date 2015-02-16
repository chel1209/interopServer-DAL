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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a type-safe mechanism to store and retrieve the values
 * of DalDatabase configuration parameters. The various <code>static</code>
 * methods provide the means for retrieving the Parameter value.
 * @author brian
 *
 * @param <T>
 */
public class ParameterValue<T> {
	
	@SuppressWarnings("unchecked")
	static public <V> V getValue(Parameter<V> param, Collection<ParameterValue<?>> values) {
		V result = null;
		if (values != null) {
			for (ParameterValue<?> v : values) {
				if (v.parameter.equals(param)) {
					if (param.valueClass.isAssignableFrom(v.parameter.valueClass)) {
						result = (V) v.value;
						break;
					}
				}
			}
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	static public <V> V getValue(Parameter<V> param, Map<Parameter<?>, ParameterValue<?>> valueMap) {
		V result = null;
		if (valueMap != null) {
			ParameterValue<?> pv = valueMap.get(param);
			if (pv != null) {
				if (param.valueClass.isAssignableFrom(pv.parameter.valueClass)) {
					result = (V) pv.value;
				}
			}
		}
		return result;
	}

	static public Map<Parameter<?>, ParameterValue<?>> getParametersAsMap(Set<ParameterValue<?>> parameters) {
		Map<Parameter<?>, ParameterValue<?>> valueByParam = new LinkedHashMap<Parameter<?>, ParameterValue<?>>();
		for (ParameterValue<?> pv : parameters) {
			valueByParam.put(pv.parameter, pv);
		}
		return valueByParam;
	}
	
	public final Parameter<T> parameter;
	public final T value;

	public ParameterValue(Parameter<T> p, T value) {
		this.parameter = p;
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		return parameter.hashCode() * 17 + (value==null ? 0 : value.hashCode());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (! (o instanceof ParameterValue)) return false;

		ParameterValue<?> other = (ParameterValue<?>) o;

		return (other.parameter.equals(parameter))
				&&
				(value == null ? other.value==null : value.equals(other.value));
	}
	
	@Override
	public String toString() {
		return parameter.toString()+"="+value;
	}
}