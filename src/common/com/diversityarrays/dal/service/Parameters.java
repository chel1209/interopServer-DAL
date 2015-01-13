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
 * Provides some standard Parameter instances for use in DalDbProviderServer implementations.
 * You can also hand-craft your own if needed.
 * @author brian
 */
public class Parameters {

	/**
	 * Standard parameter specifying the web server <i>host name</i> parameter.
	 */
	static public final Parameter<String> HOST_NAME = new StringParameter("Host Name", "Host name/address of HTTP server", Parameter.REQUIRED);

	/**
	 * Standard parameter specifying the web server <i>port number</i> parameter.
	 */
	static public final Parameter<Integer> HOST_PORT = new IntegerParameter("Port", "Port number of HTTP server", Parameter.REQUIRED);

	/**
	 * Standard parameter specifying the folder that contains the database data.
	 */
	static public final FileParameter DATABASE_FOLDER = new FileParameter(
			"Database Folder",
			"Folder on filesystem that holds the database data", Parameter.REQUIRED);

	/**
	 * Standard parameter for JDBC databases.
	 */
	static public final StringParameter JDBC_URL = new StringParameter("JDBC URL", "", Parameter.REQUIRED);
	/**
	 * Standard parameter for JDBC databases that provides the username for the connection.
	 * This is OPTIONAL as some databases may not required a username.
	 */
	static public final StringParameter JDBC_USERNAME = new StringParameter("JDBC Username", "", Parameter.OPTIONAL);

	/**
	 * Standard parameter for JDBC databases that provides the password for the connection.
	 * This is OPTIONAL as some databases may not required a password.
	 */
	static public final StringParameter JDBC_PASSWORD = new StringParameter("JDBC Password", "", Parameter.OPTIONAL);

	public static final StringParameter[] JDBC_PARAMETERS = {
		JDBC_URL,
		JDBC_USERNAME,
		JDBC_PASSWORD
	};

	// No instances
	private Parameters() {
	}

}
