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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DalSessionStore {
	
	static private final SessionStoreListener[] NO_LISTENERS = new SessionStoreListener[0];
	static private final DalSession[] NO_SESSIONS = new DalSession[0];
	
	private Map<String,DalSession> dalSessionById = new HashMap<String,DalSession>();
	
	private List<SessionStoreListener> listeners = new ArrayList<SessionStoreListener>();
	
	public void addSessionStoreListener(SessionStoreListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}
	
	public void removeSessionStoreListener(SessionStoreListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	protected SessionStoreListener[] getListeners() {
		SessionStoreListener[] result = NO_LISTENERS;
		synchronized (listeners) {
			if (! listeners.isEmpty()) {
				result = listeners.toArray(new SessionStoreListener[listeners.size()]);
			}
		}
		return result;
	}
	
	protected void fireSessionAdded(DalSession s) {
		SessionStoreListener[] tmp = getListeners();
		for (SessionStoreListener ssl : tmp) {
			ssl.sessionAdded(this, s);
		}
	}
	
	protected void fireSessionRemoved(DalSession s) {
		SessionStoreListener[] tmp = getListeners();
		for (SessionStoreListener ssl : tmp) {
			ssl.sessionRemoved(this, s);
		}
	}
	

	public DalSession[] getSessions() {
		DalSession[] result = NO_SESSIONS;
		synchronized (dalSessionById) {
			if (! dalSessionById.isEmpty()) {
				Collection<DalSession> values = dalSessionById.values();
				result = values.toArray(new DalSession[values.size()]);
			}
		}
		return result;
	}

	public void removeSession(DalSession dalSession) {
		synchronized (dalSessionById) {
			dalSessionById.remove(dalSession.sessionId);
		}
		fireSessionRemoved(dalSession);
	}

	public synchronized void addSession(DalSession dalSession) {
		synchronized (dalSessionById) {
			dalSessionById.put(dalSession.sessionId, dalSession);
		}
		fireSessionAdded(dalSession);
	}

	public synchronized DalSession getSession(String sessionId) {
		DalSession session;
		synchronized (dalSessionById) {
			session = dalSessionById.get(sessionId);
		}
		return session;
	}
}