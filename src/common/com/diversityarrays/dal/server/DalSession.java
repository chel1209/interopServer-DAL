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
import java.util.List;
import java.util.UUID;

import com.diversityarrays.dal.db.UserInfo;
import com.diversityarrays.dalclient.DalUtil;
import com.diversityarrays.dalclient.SessionExpiryOption;

import fi.iki.elonen.NanoHTTPD.Cookie;

public class DalSession {
	
	public static final String COOKIE_NAME_DAL_SESSION_ID = "KDDArT_DAL_SESSID";
	
	private static final String COOKIE_NAME_RANDOM_NUMBER = "KDDArT_RANDOM_NUMBER";
	private static final String COOKIE_NAME_DOWNLOAD_SESSION_ID = "KDDArT_DOWNLOAD_SESSID";
	private static final String COOKIE_NAME_DOWNLOAD = "KDDArT_DOWNLOAD";
	
	static public final String[] COOKIE_NAMES = { 
		COOKIE_NAME_DAL_SESSION_ID,
		COOKIE_NAME_RANDOM_NUMBER,
		COOKIE_NAME_DOWNLOAD_SESSION_ID,
		COOKIE_NAME_DOWNLOAD,
	};
	
	static public String createSessionId() {
		return UUID.randomUUID().toString().replaceAll("[\\W]", "");
	}
	
	public final String sessionId;
	private final UserInfo userInfo;

	private String groupId;
	// public String groupName;

	public final String writeToken;
	public final SessionExpiryOption sessionExpiryOption;

	private java.util.Date lastActive = new java.util.Date();
	private String randomNumber;
	private String downloadSessionId;
	private String download;

	public DalSession(String newSessionId,
			UserInfo userInfo,
			SessionExpiryOption seo) 
	{
		this.userInfo = userInfo;
		this.sessionExpiryOption = seo;

		// Only digits and letters (UUID will never contain '_' so it's ok to use the \W pattern group)
		this.sessionId = newSessionId;
		this.writeToken = createSessionId();
		this.downloadSessionId = createSessionId();

		// TODO what value is needed for download and when is it used?
		// this.download = "???";
		this.randomNumber = DalUtil.createRandomNumberString();
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String gid) {
		groupId = gid;
	}
	
	public java.util.Date getLastActive() {
		return lastActive;
	}

	public List<Cookie> getCookies() {
		List<Cookie> result = new ArrayList<Cookie>();

		result.add(new Cookie(COOKIE_NAME_DAL_SESSION_ID, sessionId)
				.setPath("/"));
		result.add(new Cookie(COOKIE_NAME_RANDOM_NUMBER, randomNumber)
				.setPath("/"));
		result.add(new Cookie(COOKIE_NAME_DOWNLOAD_SESSION_ID,
				downloadSessionId).setPath("/"));
		if (download == null) {
			System.err
					.println("TODO: create a value for DalSession.download");
		} else {
			result.add(new Cookie(COOKIE_NAME_DOWNLOAD, download)
					.setPath("/"));
		}

		return result;
	}

	@Override
	public String toString() {
		return "DalSession[" + sessionId + " name=" + userInfo.getUserName() + " id="
				+ userInfo.getUserId() + "]";
	}

	public String getUserName() {
		return userInfo.getUserName();
	}
	
	public String getUserId() {
		return userInfo.getUserId();
	}
	
	public void delayExpiry() {
		lastActive = new java.util.Date();
	}

	public boolean hasExpired(long millis) {
		boolean result = false;
		if (SessionExpiryOption.AUTO_EXPIRE == sessionExpiryOption) {
			long elapsed = new java.util.Date().getTime()
					- lastActive.getTime();
			result = elapsed > millis;
		}
		return result;
	}
}