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

/**
 * Provides an implementation of Parameter for String.
 * @author brian
 *
 */
public class StringParameter extends Parameter<String> {

	public StringParameter(String name, String desc, boolean reqd) {
		super(name, String.class, desc, reqd);
	}

	@Override
	public String stringToValue(String input) throws ParameterException {
		return input;
	}

	@Override
	public String valueToString(String input) {
		return input;
	}

}
