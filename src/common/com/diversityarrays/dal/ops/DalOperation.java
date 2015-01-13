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

import java.util.List;
import java.util.Map;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;


public interface DalOperation {
	
	/**
	 * Parameter name used to provide filtering a la SQL WHERE clauses
	 * for commands of the form <code>list/<i>entity</i>/_nperpage/page/_page_number</code>.
	 */
	static public final String OPTION_FILTERING = "Filtering";

	public static final String LIST_ALL_GROUP = "list/all/group";
	
	public static final String LIST_GROUP = "list/group";

	public static final String SWITCH_GROUP_STEM = "switch/group/";

	public static final String GET_VERSION = "get/version";

	public static final String LIST_FIELD_TAIL = "/list/field";

	public static final String GET_LOGIN_STATUS = "get/login/status";

	public static final String LOGOUT = "logout";

	public static final String LOGIN_STEM = "login/";

	public static final String LIST_OPERATION = "list/operation";
	
	static public final String[] MORE_OPS = {
		"login",
		GET_LOGIN_STATUS,
		
		LIST_ALL_GROUP,
		LIST_GROUP,
		
		SWITCH_GROUP_STEM + "_groupid",
		
		GET_VERSION,
		LOGOUT,
		LIST_OPERATION,
		
		// TODO implement this somehow?
//		"_tname" + LIST_FIELD_TAIL,
	};
	
	
	/**
	 * Return the command template with parameter portions introduced
	 * by an underscore ( '_' ) as the  leading character.
	 * @return a String
	 */
	public String getCommandTemplate();

	/**
	 * Return the number of paramters that this DalOperation expects
	 * @return an int value at least zero
	 */
	public int getParameterCount();
	
	/**
	 * Return the name of the specified parameter.
	 * @param index
	 * @return a String
	 * @throws IndexOutOfBoundsException
	 */
	public String getParameter(int index) throws IndexOutOfBoundsException;

	/**
	 * Return the entity name that this command relates to.
	 * @return a String
	 */
	public String getEntityName();

	/**
	 * Perform the command using the inputs provided.
	 * @param userId may be null
	 * @param responseBuilder is used to construct the response
	 * @param method is GET or POST
	 * @param dalcmd is the URI string from the HTTP request excluding the DAL url prefix
	 * (it does not include a leading slash)
	 * @param dalOpParameters are the parameter values for this DalOperation in the
	 * order they are encountered in the <code>dalcmd</code>
	 * @param methodParms are the parameters provided in the HTTP request
	 * @param filePathByName is the path to any file uploads keyed by the parameter name
	 * @throws DalDbException
	 */
	public void execute(DalSession session,
			DalResponseBuilder responseBuilder, 
			Method method, 
			String dalcmd,
			List<String> dalOpParameters, 
			Map<String, String> methodParms, 
			Map<String, String> filePathByName) 
	throws DalDbException;

}
