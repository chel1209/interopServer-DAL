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


public abstract class Parameter<T> {

	static public final boolean REQUIRED = true;
	static public final boolean OPTIONAL = false;
	
	
	public final String name;
	public final Class<? extends T> valueClass;
	public final boolean required;
	public final String description;

	public Parameter(String name, Class<? extends T> clz, String desc, boolean reqd) {
		this.name = name;
		this.valueClass = clz;
		this.description = desc;
		this.required = reqd;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"["+name+"]";
	}

	/**
	 * Transform the String version of the parameter value to an instance of the expected class.
	 * @param input
	 * @return instance of T or null
	 * @throws ParameterException
	 */
	abstract public T stringToValue(String input) throws ParameterException;
	
	/**
	 * Transform the provided value instance to a String.
	 * @param input
	 * @return a String
	 */
	abstract public String valueToString(T input);

}