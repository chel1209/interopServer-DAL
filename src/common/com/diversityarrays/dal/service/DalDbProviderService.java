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
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;

/**
 * Implementations of these are retrieved using ServiceRegistry.lookupProviders()
 * and are responsible for supplying the list of Parameters and any default values
 * for presentation to the user as well as creating a DalDatabase instance when
 * required.
 * @author brian
 *
 */
public interface DalDbProviderService {

	/**
	 * Return the name of this provider for visual identification.
	 * @return
	 */
	public String getProviderName();

	/**
	 * Return an HTML string that is used to describe the provider.
	 * @return
	 */
	public String getHtmlDescription();

	/**
	 * Return whether this service supports use of <code>createDatabase()</code> for
	 * testing. For example, a JDBC-based service may allow this to happen so that
	 * the user can see if the connection details are correct. Other services may not
	 * wish to allow creation because of side-effects or extremely lengthy initialisation.
	 * @return boolean
	 */
	public boolean getSupportsTest();

	/**
	 * Return details of the parameters required by this service for the <code>createDatabase()</code> method.
	 * @return
	 */
	public Set<Parameter<?>> getParametersRequired();

	public List<ParameterValue<?>> getDefaultParameterValues();

	/**
	 * Create a new DalDatabase using the provided parameters.
	 * @param parameters
	 * @param test
	 * @return a DalDatabase
	 * @throws DalDbException if unable to create
	 */
	public DalDatabase createDatabase(Collection<ParameterValue<?>> parameterValues, Closure<String> progress, boolean test) throws DalDbException;



}
