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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.kddart.KddartDalDatabase;
import com.diversityarrays.dal.service.AbstractJdbcDalDbProviderService;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterValue;
import com.diversityarrays.dal.service.StringParameter;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;

public class BMS_DalDbProviderService extends AbstractJdbcDalDbProviderService {

	// Ideas for future:
	
	// FCODE values for GERMPLSM names: Use in this order to choose values for name from NAMES table...
	// CRSNM,VARNM,COMMON,AABBR,NAME_2,BCID,SELHIST,PEDIGREE
	
	// Could do:
	// CREATE TEMPORARY TABLE gid_name_priority (priority INT, bms_fldno INT, name VARCHAR(longest));
	//  INSERT INTO gid_name_priority VALUES
	//   (1, fldno, 'CRSNM'),
	//   (2, fldno, 'VARNM'),
	//  ... ;
	// And then use it in a join
	
	static private final StringParameter CENTRAL_URL = new StringParameter("Central JDBC URL", "", Parameter.REQUIRED);
	
	static private final StringParameter LOCAL_URL = new StringParameter("Local JDBC URL", "", Parameter.OPTIONAL);
	
	static private final StringParameter INTEROP_URL = new StringParameter("INTEROP JDBC URL", "", Parameter.REQUIRED);
	
	static private final Parameter<String> USERNAME = new StringParameter(KddartDalDatabase.PARAM_USERNAME, "Login as this user on the remote DAL service", Parameter.REQUIRED);
	
	static private final Parameter<String> PASSWORD = new StringParameter(KddartDalDatabase.PARAM_PASSWORD, "Use this password for the remote DAL service", Parameter.REQUIRED);
	
	private static final StringParameter[] PARAMETERS = {
		CENTRAL_URL,
//		LOCAL_URL, // TODO local incarnation delayed until I get a chance to talk to BMS folks
	};
	
	public BMS_DalDbProviderService() {
		super("BMS");
	}

	@Override
	public String getHtmlDescription() {
		return "Provides DAL-Interop operations on a BMS database.";
	}

	@Override
	public Set<Parameter<?>> getParametersRequired() {
		return new LinkedHashSet<Parameter<?>>(Arrays.asList(CENTRAL_URL,LOCAL_URL,USERNAME,PASSWORD));
	}

	@Override
	public List<ParameterValue<?>> getDefaultParameterValues() {
		List<ParameterValue<?>> result = new ArrayList<ParameterValue<?>>();
		result.add(new ParameterValue<String>(CENTRAL_URL, "jdbc:mysql://localhost:13306/DBNAME_central?user=root"));
		result.add(new ParameterValue<String>(LOCAL_URL,   "jdbc:mysql://localhost:13306/DBNAME_local?user=root"));
		result.add(new ParameterValue<String>(INTEROP_URL, "jdbc:mysql://172.17.60.87:13306/siu_bms?user=bmsadmin&password=bms;2015*"));
		return result;
	}

	@Override
	public boolean getSupportsTest() {
		return true;
	}

	@Override
	public DalDatabase createDatabase(
			Collection<ParameterValue<?>> parameterValues, Closure<String> progress, boolean initialise)
			throws DalDbException 
	{
		Map<Parameter<?>, String> base = getJdbcParameterValues(parameterValues);
		
		JdbcConnectionParameters central = new JdbcConnectionParameters(base.get(CENTRAL_URL), null, null); // base.get(CENTRAL_USERNAME), base.get(CENTRAL_PASSWORD));
		
		JdbcConnectionParameters local = null;
		String localUrl = base.get(LOCAL_URL);
		if (localUrl != null && ! localUrl.isEmpty()) {
			local   = new JdbcConnectionParameters(localUrl,   null, null); // base.get(LOCAL_USERNAME),   base.get(LOCAL_PASSWORD));
		}
		
		String interopUrl = base.get(INTEROP_URL);
		JdbcConnectionParameters interopParams = new JdbcConnectionParameters(interopUrl, "bmsadmin", "bms;2015*");
		
		//URI uri = ParameterValue.getValue(DAL_URL, parameterValues);
		String username = ParameterValue.getValue(USERNAME, parameterValues);
		String password = ParameterValue.getValue(PASSWORD, parameterValues);
		//Boolean autoSwitchGroup = ParameterValue.getValue(AUTO_SWITCH_GROUP, parameterValues);

		return new BMS_DalDatabase(progress, initialise, local, central,username,password,interopParams);
	}

}
