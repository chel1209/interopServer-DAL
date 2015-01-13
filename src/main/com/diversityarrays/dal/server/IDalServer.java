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

import java.beans.PropertyChangeListener;

import com.diversityarrays.dal.db.DalDatabase;

import fi.iki.elonen.NanoHTTPD;

/**
 * Exists so that DalServer and ServerGui and not co-dependent.
 * @author brian
 *
 */
public interface IDalServer {
	
	static final String DOT_DALSERVER = ".dalserver";

//	String getServerAddress();
	String getListeningAddressPort();

	String getListeningUrl();

	int getListeningPort();

	DalDatabase getDalDatabase();

	boolean isVerbose();

	boolean isQuiet();

	void setQuiet(boolean q);

	int getMaxInactiveMinutes();
	void setMaxInactiveMinutes(int maxInactiveMinutes);

	NanoHTTPD getHttpServer();

	void removePropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener);
	void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener);

	String getDalServerVersion();

	boolean isAlive();

	int getDalOperationCount();

	boolean getUseSimpleDatabase();

}