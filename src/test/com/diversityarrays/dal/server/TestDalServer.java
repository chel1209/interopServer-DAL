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

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;
import com.diversityarrays.dalclient.DefaultDALClient;

public class TestDalServer {
	
	static private final boolean SHOW_RESPONSE = true;

	static DALClient client;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		boolean success = false;
		try {
			client = new DefaultDALClient("http://localhost:40112/dal/");
			client.setAutoSwitchGroupOnLogin(true);
			client.login("username", "password");
			
			success = true;
		}
		finally {
			if (! success) {
				if (client != null) {
					client.logout();
				}
				client = null;
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (client != null) {
			client.logout();
			client = null;
		}
	}

	@Test
	public void testListOperation() {
		try {
			DalResponse response = client.performQuery("list/operation");
			if (SHOW_RESPONSE) {
				showResponse(response);
			}
		} catch (DalResponseException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testListGenus() {
		try {
			DalResponse response = client.performQuery("list/genus");
			if (SHOW_RESPONSE) {
				showResponse(response);
			}
		} catch (DalResponseException | IOException e) {
			fail(e.getMessage());
		}
	}

	private void showResponse(DalResponse response) {
		System.out.println("=== " + response.getUrl());
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
			
			@Override
			public boolean visitResponseRecord(String tag, DalResponseRecord rr) {
				System.out.println("  " + tag + ":");
				for (String k : rr.rowdata.keySet()) {
					System.out.println("    " + k + "=" + rr.rowdata.get(k));
				}
				return true;
			}
		};
		try {
			response.visitResults(visitor);
		} catch (DalResponseException e) {
			System.out.println("  **  ERROR: " + e.getMessage());
		}
	}

}
