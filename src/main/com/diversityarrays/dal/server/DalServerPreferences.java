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
package com.diversityarrays.dal.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.service.DalDbProviderService;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterException;
import com.diversityarrays.dal.service.ParameterValue;

public class DalServerPreferences {

	static private final String WWW_ROOT = "wwwRootDir";
	static private final String SERVER_HOST_NAME = "serverHostName";
	static private final String SERVER_PORT_NUMBER = "serverPortNumber";
	
	private Preferences preferences;
	public DalServerPreferences(Preferences prefs) {
		this.preferences = prefs;
	}
	
	public File getWebRoot(File defalt) {
		String path = preferences.get(WWW_ROOT, null);
		return path==null ? defalt : new File(path);
	}
	
	public void setWebRoot(File file) {
		preferences.put(WWW_ROOT, file.getPath());
	}

	public void setServerHostName(String hostName) {
		preferences.put(SERVER_HOST_NAME, hostName);
	}
	
	public String getServerHostName(String def) {
		return preferences.get(SERVER_PORT_NUMBER, def);
	}

	public void setServerPort(int port) {
		preferences.putInt(SERVER_PORT_NUMBER, port);
	}
	
	public int getServerPort(int def) {
		return preferences.getInt(SERVER_PORT_NUMBER, def);
	}

	public void save() {
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			System.err.println(this.getClass().getName()+".save: "+e.getMessage());
		}
		
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public void saveAsPropertiesFile(DalDbProviderService service, Set<ParameterValue<?>> values, OutputStream os) 
	throws IOException {
		Properties props = new Properties();
		for (ParameterValue pv : values) {
			String v = pv.parameter.valueToString(pv.value);
			props.setProperty(pv.parameter.name, v);
		}
		props.store(os , "Settings for " + service.getProviderName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void saveServiceSettings(DalDbProviderService service, Set<ParameterValue<?>> values) throws BackingStoreException {
		Preferences serviceNode = preferences.node("service/"+service.getClass().getName());
		serviceNode.clear();
		for (ParameterValue pv : values) {
			String v = pv.parameter.valueToString(pv.value);
			serviceNode.put(pv.parameter.name, v);
		}
		
		serviceNode.flush();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<Parameter<?>, ParameterValue<?>> loadSavedSettings(
			DalDbProviderService service, 
			Set<Parameter<?>> parametersRequired,
			Map<Parameter<?>,Throwable> errors) 
	{
		Map<Parameter<?>, ParameterValue<?>> result = new HashMap<Parameter<?>, ParameterValue<?>>();
		Preferences serviceNode = preferences.node("service/"+service.getClass().getName());
		for (Parameter param : parametersRequired) {
			String s = serviceNode.get(param.name, null);
			if (s==null && param.required) {
				errors.put(param, new DalDbException("No saved value in preferences"));
			}
			else {
				try {
					Object value = param.stringToValue(s);
					result.put(param, new ParameterValue(param, value));
				} catch (ParameterException e) {
					Throwable t = e.getCause();
					if (t==null) {
						t = e;
					}
					errors.put(param, t);
				}
			}
		}
		return result;
	}

}
