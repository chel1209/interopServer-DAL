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
package com.diversityarrays.dal.db.kddart;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.service.AbstractDalDbProviderService;
import com.diversityarrays.dal.service.BooleanParameter;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterException;
import com.diversityarrays.dal.service.ParameterValue;
import com.diversityarrays.dal.service.StringParameter;

public class KddartDalDbProviderService extends AbstractDalDbProviderService {
	
	static private final Parameter<URI> DAL_URL = new Parameter<URI>(
			"DAL URL", URI.class, "URL for the remote DAL service to which to forward requests", Parameter.REQUIRED) 
	{
		@Override
		public URI stringToValue(String input) throws ParameterException {
			try {
				return input==null ? null : new URI(input);
			} catch (URISyntaxException e) {
				throw new ParameterException(e);
			}
		}

		@Override
		public String valueToString(URI input) {
			return input==null ? null : input.toString();
		}
		
	};
	
	static private final Parameter<String> USERNAME = new StringParameter(KddartDalDatabase.PARAM_USERNAME, "Login as this user on the remote DAL service", Parameter.REQUIRED);
	
	static private final Parameter<String> PASSWORD = new StringParameter(KddartDalDatabase.PARAM_PASSWORD, "Use this password for the remote DAL service", Parameter.REQUIRED);
	
	static private final Parameter<Boolean> AUTO_SWITCH_GROUP = new BooleanParameter("Auto Switch Group", "Automatically login with the first System Group the user is a member of", Parameter.OPTIONAL);
	
	public KddartDalDbProviderService() {
		super("KDDart-DAL");
	}

	@Override
	public boolean getSupportsTest() {
		return true;
	}

	@Override
	public String getHtmlDescription() {
		return "Forwards all requests to a DAL server using pre-supplied credentials";
	}

	@Override
	public Set<Parameter<?>> getParametersRequired() {
		return new LinkedHashSet<Parameter<?>>(Arrays.asList(DAL_URL, USERNAME, PASSWORD, AUTO_SWITCH_GROUP));
	}
	

	@Override
	public List<ParameterValue<?>> getDefaultParameterValues() {
		List<ParameterValue<?>> result = new ArrayList<ParameterValue<?>>();
		try {
			result.add(new ParameterValue<URI>(DAL_URL, new URI("http://kddart-dal.diversityarrays.com/dal")));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}


	@Override
	public DalDatabase createDatabase(Collection<ParameterValue<?>> parameterValues, Closure<String> progress, boolean test) throws DalDbException {
		
		URI uri = ParameterValue.getValue(DAL_URL, parameterValues);
		String username = ParameterValue.getValue(USERNAME, parameterValues);
		String password = ParameterValue.getValue(PASSWORD, parameterValues);
		Boolean autoSwitchGroup = ParameterValue.getValue(AUTO_SWITCH_GROUP, parameterValues);
		
		return new KddartDalDatabase(progress, test, uri, username, password, autoSwitchGroup);
	}


}
