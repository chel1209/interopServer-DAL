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
package com.diversityarrays.dal.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;
import net.pearcan.util.StringUtil;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ClosureUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.diversityarrays.dal.db.bms.BMS_DalDatabase;
import com.diversityarrays.dal.db.bms.GenotypeAliasFactory;
import com.diversityarrays.dal.db.kddart.KddartDalDatabase;
import com.diversityarrays.dal.db.kddart.KddartDalDbProviderService;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.ops.OperationMatch;
import com.diversityarrays.dal.ops.WordNode;
import com.diversityarrays.dal.server.DalServer;
import com.diversityarrays.dal.server.DalServerUtil;
import com.diversityarrays.dal.server.DalSession;
import com.diversityarrays.dal.service.DalDbProviderService;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterException;
import com.diversityarrays.dal.service.ParameterValue;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;
import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalUtil;
import com.diversityarrays.dalclient.SessionExpiryOption;

import fi.iki.elonen.NanoHTTPD.Method;

public class TestDalDatabase {
	
	static abstract class LoggedInTest {
		public final boolean wantTiming;
		public final String name;
		LoggedInTest(String name, boolean wantTiming) {
			this.name = name;
			this.wantTiming = wantTiming;
		}
		abstract public void execute(DalSession session);
	}
	
	private static String GET_GENUS_ID;
	
	private static String GET_GENOTYPE_ID;
	
	private static String LIST_GENOTYPE_ALIAS_ID;
	private static String GET_GENOTYPE_ALIAS_ID;

	private static final boolean WANT_TIMING =  Boolean.getBoolean(TestDalDatabase.class.getSimpleName()+".WANT_TIMING");
	private static final boolean NOISY = Boolean.getBoolean(TestDalDatabase.class.getSimpleName()+".NOISY");
	
	private static final boolean WANT_JSON = true;
	
	// Set true to get first login to display messages
	private boolean firstLogin = Boolean.getBoolean(TestDalDatabase.class.getSimpleName()+".firstLogin");
	
	static private final Map<String,String[]> CENTRAL_LOCAL_URL_PAIRS = new HashMap<String, String[]>();

	private static final int N_PER_PAGE = 10;
	
	static String USERNAME;
	static String PASSWORD;

	static {
		CENTRAL_LOCAL_URL_PAIRS.put("DART", new String[] {
			"jdbc:mysql://192.168.9.91:13306/ibdbv2_wheat_central?user=root",
			null // "jdbc:mysql://192.168.9.91:13306/ibdbv2_wheat_2_local?user=root"
		});

		CENTRAL_LOCAL_URL_PAIRS.put("BMAC", new String[] { 
			"jdbc:mysql://localhost:3306/iwis_gms?user=root",
			null // "jdbc:mysql://localhost:3306/iwis_gms?user=root"
		});
		
		CENTRAL_LOCAL_URL_PAIRS.put("HOME", new String[] { 
			"jdbc:mysql://localhost:4406/iwis_gms?user=root",
			null // "jdbc:mysql://localhost:4406/iwis_gms?user=root"
		});
	}
	
	private static Closure<String> PROGRESS = new Closure<String>() {
		@Override
		public void execute(String msg) {
//			if (NOISY) {
				System.err.println(TestDalDatabase.class.getName()+": " + msg);
//			}
		}
	};
	private static DalDatabase dalDatabase;
	
//	static private void x() {
//		Connection conn = null;
//		try {
//			conn = DbUtil.createConnection("jdbc:mysql://localhost:4406/iwis_gms?user=root", null, null, null);
//			System.out.println("Connected OK");
//		}
//		catch (Exception e) {
//			Throwable c = e.getCause();
//			if (c == null) {
//				c = e;
//			}
//				//jdbc:mysql://127.0.0.1:4406/?user=root
////			c.printStackTrace();
//			
//			System.err.println(c.getMessage());
//			throw e;
//		}
//		finally {
//			if (conn != null) {
//				conn.close();
//			}
//		}
//	}
	
	private static WordNode wordNodeRoot = new WordNode();
	
	
	static OperationMatch getOperationMatch(String dalcmd) {
		StringBuilder errmsg = new StringBuilder();
		
		OperationMatch match = DalDatabaseUtil.findOperationMatch(dalcmd, wordNodeRoot, errmsg);
		
		if (match == null) {
			fail("'" + dalcmd + "' did not match an operation");
		}
		
		if (errmsg.length() > 0) {
			fail(errmsg.toString());
		}
		
		return match;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		String testDbNameProperty = TestDalDatabase.class.getSimpleName()+".TEST_DBNAME";
		String testDbName = System.getProperty(testDbNameProperty);
		if (testDbName == null || testDbName.isEmpty()) {
			testDbName = "BMS";
		}
		
		if ("BMS".equalsIgnoreCase(testDbName)) {
			GET_GENUS_ID = "2";
			
			GET_GENOTYPE_ID = "5988205";			

			GET_GENOTYPE_ALIAS_ID = "462355";
			LIST_GENOTYPE_ALIAS_ID = "5988209";

			dalDatabase = createBMS_DalDatabase();
		}
		else if ("KDDart".equalsIgnoreCase(testDbName)) {
			
			GET_GENUS_ID = "1";
			
			GET_GENOTYPE_ID = "1";

			GET_GENOTYPE_ALIAS_ID = "540";
			LIST_GENOTYPE_ALIAS_ID = "540";
			
			dalDatabase = createKddartDalDatabase();
		}
		else {
			throw new RuntimeException("Unsupported value '" + testDbName + "' for " + testDbNameProperty);
		}
		
		DalServerUtil.buildWordTree(dalDatabase.getOperations(), wordNodeRoot);
	}

	private static DalDatabase createBMS_DalDatabase() throws UnknownHostException, DalDbException {
		
		USERNAME = "GUEST";
		PASSWORD = "GUEST";
		
		String where = null;
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
		String h2 = InetAddress.getLocalHost().getHostName();
		
		
		if (hostname.startsWith("192.168.9.")) {
			where = "DART";
		}
		else {
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
			if (hostname.startsWith("pyrus")) {
				where = "HOME";
			}
			else if ("localhost".equals(hostname) || hostname.startsWith("bmac") || h2.startsWith("beepsmac")) {
				where = "BMAC";
			}
			else {
				where = "DART";
			}
		}

		String[] centralLocal = CENTRAL_LOCAL_URL_PAIRS.get(where);
		
		System.out.println(TestDalDatabase.class.getName()+" for BMS using:");
		System.out.println("central: " + centralLocal[0]);
		System.out.println("  local: " + centralLocal[1]);
		
		JdbcConnectionParameters centralParams = new JdbcConnectionParameters(centralLocal[0], null, null);
		JdbcConnectionParameters localParams   = null;
		if (centralLocal[1] != null && ! centralLocal[1].isEmpty()) {
			localParams   = new JdbcConnectionParameters(centralLocal[1], null, null);
		}

		@SuppressWarnings("unchecked")
		DalDatabase result = new BMS_DalDatabase(ClosureUtils.nopClosure(), false, localParams, centralParams);
		
		System.out.println(TestDalDatabase.class.getName()+" for KDDart: " + result.getDatabaseName());
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static private DalDatabase createKddartDalDatabase() throws DalDbException {
		
		Preferences preferences = Preferences.userNodeForPackage(DalServer.class);
		DalDbProviderService service = new KddartDalDbProviderService();

		Map<Parameter<?>, ParameterValue<?>> parameterValues = new HashMap<Parameter<?>, ParameterValue<?>>();
		Preferences serviceNode = preferences.node("service/"+service.getClass().getName());

		for (Parameter<?> param : service.getParametersRequired()) {
			String s = serviceNode.get(param.name, null);
			try {
				Object value = param.stringToValue(s);
				parameterValues.put(param, new ParameterValue(param, value));
				
				if (KddartDalDatabase.PARAM_USERNAME.equals(param.name)) {
					USERNAME = s;
				}
				else if (KddartDalDatabase.PARAM_PASSWORD.equals(param.name)) {
					PASSWORD = s;
				}
			} catch (ParameterException e) {
				Throwable t = e.getCause();
				if (t==null) {
					t = e;
				}
				if (t instanceof RuntimeException) {
					throw ((RuntimeException) t);
				}
				throw new RuntimeException(t);
			}
		}
		
		
		KddartDalDatabase result = (KddartDalDatabase) service.createDatabase(parameterValues.values(), ClosureUtils.nopClosure(), false);

		System.out.println(TestDalDatabase.class.getName()+" for KDDart: " + result.getDatabaseName());

		result.setAutoSwitchGroupOnLogin(true);
		return result;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (dalDatabase != null) {
			dalDatabase.shutdown();
		}
	}

	public Map<String, String> createLoginParms(String username, String password) {
		String url = "dummyURL";
		
		String rand = DalUtil.createRandomNumberString();
		String pwdUnameHash = DalUtil.computeHmacSHA1(password, username);
		String randhash = DalUtil.computeHmacSHA1(pwdUnameHash, rand);
		String signature = DalUtil.computeHmacSHA1(randhash, url);
		
		Map<String, String> parms = new HashMap<String, String>();
		parms.put("rand_num", rand);
		parms.put("url", url);
		parms.put("signature", signature);
		return parms;
	}
	
	@Test
	public void testGetVersion() {
		try {
			String version = dalDatabase.getDatabaseVersion(null);
			System.out.println("Logged-out version=" + version);
			
			LoggedInTest loggedInTest = new LoggedInTest("getVersion", true) {
				@Override
				public void execute(DalSession session) {
					try {
						String v = dalDatabase.getDatabaseVersion(session);
						System.out.println("Logged-in version=" + v);
					} catch (DalDbException e) {
						fail(e.getMessage());
					}
				}
			};
			doLoggedInTest(loggedInTest);
			
		} catch (DalDbException e) {
			fail(e.getMessage());
		}
	}
	
	private void doListGroup(String testName, DalSession session) {
		DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
		String[] returnSql = null;
		try {
			dalDatabase.performListGroup(session, responseBuilder, returnSql);
			checkJsonResult(testName, responseBuilder, "SystemGroup");
			
			if (NOISY) {
				showResponse(testName, responseBuilder);
			}

			
		} catch (DalDbException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testListGroup() {
		LoggedInTest loggedInTest = new LoggedInTest("listGroup", true) {
			@Override
			public void execute(DalSession session) {
				doListGroup("testListGroup", session);
			}
		};
		doLoggedInTest(loggedInTest);
//		doListGroup("testListGroup", null);
	}
	
	@Test
	public void testListAllGroup() {
		LoggedInTest loggedInTest = new LoggedInTest("listAllGroup", true) {
			@Override
			public void execute(DalSession session) {
				doListGroup("testListAllGroup", session);
			}
		};
		doLoggedInTest(loggedInTest );
	}
	
	@Test
	public void testListOperation() {
		
		System.out.println("==== List Operations");
		List<DalOperation> list = dalDatabase.getOperations();
		Collections.sort(list, new Comparator<DalOperation>() {
			@Override
			public int compare(DalOperation o1, DalOperation o2) {
				return o1.getCommandTemplate().compareTo(o2.getCommandTemplate());
			}
			
		});
		for (DalOperation op : list) {
			System.out.println(op.getCommandTemplate());
		}
		System.out.println("=====================");
	}

	static private Boolean LOGIN_FAILED = null;
	
	final private void doLoggedInTest(LoggedInTest loggedInTest) {
		
		if (LOGIN_FAILED != null && LOGIN_FAILED.booleanValue()) {
			fail("Previous login failed");
		}
		
		DalSession session = null;
		Map<String, String> parms = createLoginParms(USERNAME, PASSWORD);
		
		try {
			if (firstLogin) {
				firstLogin = false;
				if (dalDatabase instanceof BMS_DalDatabase) {
					((BMS_DalDatabase) dalDatabase).setDefaultProgress(PROGRESS);					
				}
			}
			
			String newSessionId = DalSession.createSessionId();
			SessionExpiryOption seo = SessionExpiryOption.AUTO_EXPIRE;
			
			long startNanos = System.nanoTime();
			UserInfo userInfo = dalDatabase.doLogin(newSessionId, USERNAME, seo, parms);
			long loginNanos = System.nanoTime() - startNanos;
			session = new DalSession(newSessionId, userInfo, seo);
			
			startNanos = System.nanoTime();
			loggedInTest.execute(session);
			long elapsedNanos = System.nanoTime() - startNanos;

			if (WANT_TIMING) {
				System.err.println(loggedInTest.name + ": loginTime=" + (loginNanos / 1_000_000.0) + " ms, testTime=" + (elapsedNanos / 1_000_000.0) + " ms");
			}
		} catch (AuthenticationException e) {
			LOGIN_FAILED = true;
			fail(e.getMessage());
		} finally {
			if (dalDatabase instanceof BMS_DalDatabase) {
				((BMS_DalDatabase) dalDatabase).setDefaultProgress(null);
			}
			if (session != null) {
				dalDatabase.doLogout(session);
			}
		}
	}
	
	@Test
	public void testGetGenus() {
		
		LoggedInTest loggedInTest = new LoggedInTest("getGenus", true) {
			
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "get/genus/" + GET_GENUS_ID;
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);
					
					checkJsonResult("testGetGenus", responseBuilder, "Genus");
					
					if (NOISY) {
						showResponse("testGetGenus", responseBuilder);
					}

				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}

			}
		};
		
		doLoggedInTest(loggedInTest);
	}

	private void checkJsonResult(String where, DalResponseBuilder responseBuilder, String ... keysExpected) {

		String response = responseBuilder.asString();
		
		assertNotEquals(where + ": No record returned", "{}", response);
		
		try {
			JsonParser parser = new JsonParser(response);
			if (! parser.isMapResult()) {
				fail(where + ": result is not a JsonMap");
			}
			
			JsonMap jsonMap = parser.getMapResult();
			List<String> responseKeys = jsonMap.getKeysInOrder();
			
			if (responseKeys.size() != (keysExpected.length + 1)) {
				fail(where + " : expected key count mismatch: " + responseKeys.size() + "<>" + (keysExpected.length+1)
						+ "\n\tactual= " + StringUtil.join(",", responseKeys)
						+" \n\texpected= " + DALClient.TAG_RECORD_META + "," + StringUtil.join(",", (Object[]) keysExpected));
			}
			
			Set<String> keySet;
			
			keySet = new HashSet<String>(Arrays.asList(keysExpected));
			keySet.add(DALClient.TAG_RECORD_META); // must ALWAYS expect 'RecordMeta'
			
			boolean checkPagination = keySet.contains(DALClient.TAG_PAGINATION);
			
			keySet.removeAll(responseKeys);
			if (! keySet.isEmpty()) {
				fail(where + ": missing keys in response: " + StringUtil.join(",", keySet));
			}

			keySet = new HashSet<String>(responseKeys);
			
			keySet.removeAll(Arrays.asList(keysExpected));
			keySet.remove("RecordMeta");
			
			if (! keySet.isEmpty()) {
				fail(where + ": unexpected keys in response: " + StringUtil.join(",", keySet));
			}
			
			if (checkPagination) {
				Object paginationObject = jsonMap.get(DALClient.TAG_PAGINATION);
				assertNotNull("Missing tag '" + DALClient.TAG_PAGINATION + "'", paginationObject);
				
				if (! (paginationObject instanceof List)) {
					fail(DALClient.TAG_PAGINATION +" is not a List: " + paginationObject.getClass().getName());
				}
				
				@SuppressWarnings("rawtypes")
				List list = (List) paginationObject;
				assertEquals(DALClient.TAG_PAGINATION + " is not a List of size 1", 1, list.size());
				
				Object mapObject = list.get(0);
				assertEquals(DALClient.TAG_PAGINATION +"[0] is not a JsonMap", JsonMap.class, mapObject.getClass());
				
				JsonMap pagination = (JsonMap) mapObject;
				
				Set<String> paginationKeys = new HashSet<String>(pagination.getKeysInOrder());
				
				for (String attrName : PAGINATION_ATTRIBUTES) {
					Object attrObject = pagination.get(attrName);
					
					assertNotNull("Missing attribute " + DALClient.TAG_PAGINATION + "." + attrName, attrObject);

					assertEquals(DALClient.TAG_PAGINATION + "." + attrName + " is not a String", String.class, attrObject.getClass());
					
					try {
						Integer.parseInt((String) attrObject);
					} catch (NumberFormatException e) {
						fail(DALClient.TAG_PAGINATION + "." + attrName +" is not a valid Integer");
					}
					
					paginationKeys.remove(attrName);
				}
				
				if (! paginationKeys.isEmpty()) {
					fail("Unexpected keys in " + DALClient.TAG_PAGINATION + ": " + StringUtil.join(",", paginationKeys));
				}
			}
		} catch (ParseException e) {
			fail(where + ": invalid JSON : " + e.getMessage());
		}
	}
	
	static private final String[] PAGINATION_ATTRIBUTES = {
		DALClient.ATTR_PAGE,
		DALClient.ATTR_NUM_OF_RECORDS,
		DALClient.ATTR_NUM_OF_PAGES,
		DALClient.ATTR_NUM_PER_PAGE
	};

	@Test
	public void testListGenus() {

		
		LoggedInTest loggedInTest = new LoggedInTest("listGenus", true) {
			
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "list/genus";
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							null, 
							null, 
							null);
					
					checkJsonResult("testListGenus", responseBuilder , "Genus");
					
					if (NOISY) {
						showResponse("testListGenus", responseBuilder);
					}

				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}

			}
		};
		
		doLoggedInTest(loggedInTest);
	}

	
	@Test
	public void testGetGenotypeAlias() {
		
		LoggedInTest loggedInTest = new LoggedInTest("getGenotypeAlias", true) {
			
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "get/genotypealias/" + GET_GENOTYPE_ALIAS_ID;
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);
					
					checkJsonResult("testGetGenotypeAlias", responseBuilder, "GenotypeAlias");
					
					if (NOISY) {
						showResponse("testGetGenotypeAlias", responseBuilder);
					}

				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}

			}
		};
		
		doLoggedInTest(loggedInTest);
	}
	
	@Test
	public void testGenotypeListAlias() {
		
		LoggedInTest loggedInTest = new LoggedInTest("genotypeListAlias", true) {
			
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "genotype/" + LIST_GENOTYPE_ALIAS_ID + "/list/alias";
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);

					checkJsonResult("testGenotypeListAlias," + dalcmd, responseBuilder, "GenotypeAlias");
					
					if (NOISY) {
						showResponse("testGenotypeListAlias", responseBuilder);
					}

				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}

			}
		};
		
		doLoggedInTest(loggedInTest);
	}
	
	@Test
	public void testListGenotypeAlias() {
		
		LoggedInTest loggedInTest = new LoggedInTest("listGenotypeAlias", true) {
			
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "list/genotypealias/" + N_PER_PAGE + "/page/1";
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);

					checkJsonResult("testListGenotypeAlias", responseBuilder, DALClient.TAG_PAGINATION, "GenotypeAlias");
					if (NOISY) {
						showResponse("testListGenotypeAlias", responseBuilder);
					}

				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}

			}
		};
		
		doLoggedInTest(loggedInTest);
	}

	@Test
	public void testGetGenotype() {
		
		LoggedInTest loggedInTest = new LoggedInTest("getGenotype", true) {
			@Override
			public void execute(DalSession session) {
				
				String dalcmd = "get/genotype/" + GET_GENOTYPE_ID;
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);
					
					checkJsonResult("testGetGenotype", responseBuilder, "Genotype");

					if (NOISY) {
						showResponse("testGetGenotype", responseBuilder);
					}
				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}
			}
			
		};
		
		doLoggedInTest(loggedInTest);
	}
	
	@Test
	public void testListGenotype() {
		
		LoggedInTest loggedInTest = new LoggedInTest("listGenotype", true) {
			
			@Override
			public void execute(DalSession session) {

				String dalcmd = "list/genotype/" + N_PER_PAGE + "/page/1";
				
				OperationMatch match = getOperationMatch(dalcmd);
				
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(WANT_JSON);
				
				try {
					match.node.getOperation().execute(session,
							responseBuilder, 
							Method.GET, 
							dalcmd, 
							match.getParameterValues(), 
							null, 
							null);
					
					checkJsonResult("testListGenotype", responseBuilder, DALClient.TAG_PAGINATION, "Genotype");
					if (NOISY) {
						showResponse("testListGenotype", responseBuilder);
					}
				} catch (DalDbException e) {
					fail(e.getMessage());
				} catch (ParseException e) {
					fail(e.getMessage());
				}
			}
		};
		
		doLoggedInTest(loggedInTest);
	}
	
	static class FilteringTest {
		public final String input;
		public final String countClause; 
		public final String getClause; 
		public final String listAlias; 
		public final String pagedList;
		
		
		FilteringTest(String input) {
			this(input, null, null, null, null);
		}
		
		FilteringTest(String input, 
				String countClause, 
				String getClause, 
				String listAlias, 
				String pagedList) 
		{
		// 
			this.input = input;
			this.countClause = countClause;
			this.getClause = getClause;
			this.listAlias = listAlias;
			this.pagedList = pagedList;
		}
	}
	
	@Test
	public void testGenotypeAliasFiltering() {
		
		if (! dalDatabase.getDatabaseName().startsWith("BMS-Interop[")) {
			return;
		}

		FilteringTest[] GENOTYPE_ALIAS_FACTORY_FILTER_CLAUSES = {
				new FilteringTest("GenotypeAliasId = 7", 
						"(nstat!=9) AND ( nid = 7 )",
						"(nstat!=9) AND (nid="+GET_GENOTYPE_ALIAS_ID+") AND ( nid = 7 )", 
						"(nstat!=9) AND (gid="+GET_GENOTYPE_ID+") AND ( nid = 7 ) LIMIT 20 OFFSET 1",
						"(nstat!=9) AND ( nid = 7 ) LIMIT 20 OFFSET 1"),
				
				
				new FilteringTest("GenotypeAliasName = 'ABC'", 
						"(nstat!=9) AND ( nval = 'ABC' )",
						"(nstat!=9) AND (nid="+GET_GENOTYPE_ALIAS_ID+") AND ( nval = 'ABC' )",
						"(nstat!=9) AND (gid="+GET_GENOTYPE_ID+") AND ( nval = 'ABC' ) LIMIT 20 OFFSET 1",
						"(nstat!=9) AND ( nval = 'ABC' ) LIMIT 20 OFFSET 1"),
						
				new FilteringTest("GenotypeAliasName LIKE '%abc%'",
						"(nstat!=9) AND ( nval LIKE '%abc%' )",
						"(nstat!=9) AND (nid="+GET_GENOTYPE_ALIAS_ID+") AND ( nval LIKE '%abc%' )",
						"(nstat!=9) AND (gid="+GET_GENOTYPE_ID+") AND ( nval LIKE '%abc%' ) LIMIT 20 OFFSET 1",
						"(nstat!=9) AND ( nval LIKE '%abc%' ) LIMIT 20 OFFSET 1" ),
						
				new FilteringTest("GenotypeAliasStatus = 23",
						"(nstat!=9) AND ( nstat = 23 )",
						"(nstat!=9) AND (nid="+GET_GENOTYPE_ALIAS_ID+") AND ( nstat = 23 )",
						"(nstat!=9) AND (gid="+GET_GENOTYPE_ID+") AND ( nstat = 23 ) LIMIT 20 OFFSET 1",
						"(nstat!=9) AND ( nstat = 23 ) LIMIT 20 OFFSET 1" ),
				
				new FilteringTest("GenotypeAliasLang = 'Chinese'",
						"(nstat!=9) AND ( nstat IN (3,4) )",
						"(nstat!=9) AND (nid="+GET_GENOTYPE_ALIAS_ID+") AND ( nstat IN (3,4) )",
						"(nstat!=9) AND (gid="+GET_GENOTYPE_ID+") AND ( nstat IN (3,4) ) LIMIT 20 OFFSET 1",
						"(nstat!=9) AND ( nstat IN (3,4) ) LIMIT 20 OFFSET 1" ),
						
				// "countClause==null" means we expect this to fail
				new FilteringTest("GenotypeAliasLang LIKE 'def%'"),
			};
		GenotypeAliasFactory genotypeAliasFactory = new GenotypeAliasFactory();
		
		
		for (FilteringTest ftest : GENOTYPE_ALIAS_FACTORY_FILTER_CLAUSES) {
			String filterClause = ftest.input;
			try {
				List<String> failures = new ArrayList<String>();

				String sql;

				sql = genotypeAliasFactory.createCountQuery(filterClause);
				if (ftest.countClause==null) {
					fail("Should not have worked: " + filterClause);
				}
				checkEquals("genotypeAliasFactory.createCountQuery", ftest.countClause, sql, failures);
				if (NOISY) {
					System.out.println("testGenotypeAliasFiltering." + ".createCountQuery=" + sql);
				}
				
				sql = genotypeAliasFactory.createGetQuery(GET_GENOTYPE_ALIAS_ID, filterClause);
				checkEquals("genotypeAliasFactory.createGetQuery", ftest.getClause, sql, failures);
				if (NOISY) {
					System.out.println("testGenotypeAliasFiltering." + ".createGetQuery=" + sql);
				}
				
				sql = genotypeAliasFactory.createListAliasQuery(GET_GENOTYPE_ID, 1, 20, filterClause);
				checkEquals("genotypeAliasFactory.createListAliasQuery", ftest.listAlias, sql, failures);
				if (NOISY) {
					System.out.println("testGenotypeAliasFiltering." + ".createListAliasQuery=" + sql);
				}
				
				sql = genotypeAliasFactory.createPagedListQuery(1, 20, filterClause);
				checkEquals("genotypeAliasFactory.createPagedListQuery", ftest.pagedList, sql, failures);
				if (NOISY) {
					System.out.println("testGenotypeAliasFiltering." + ".createPagedListQuery=" + sql);
				}
				
				if (! failures.isEmpty()) {
					StringBuilder sb = new StringBuilder("testGenotypeAliasFiltering");
					for (String f : failures) {
						sb.append("\n").append(f);
					}
					fail(sb.toString());
				}
			} catch (DalDbException e) {
				if (ftest.countClause==null) {
					// Well that's what we expect !
				}
				else {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		}
		

	}

	private void checkEquals(String testName, String expected, String sql, List<String> failures) {
		
		int pos = sql.indexOf("WHERE ");
		if (pos <= 0) {
			failures.add(testName + ":  missing 'WHERE '");
			return;
		}
		
		String afterWhere = sql.substring(pos + 6);
		
//		pos = afterWhere.indexOf(" LIMIT ");
//		if (pos > 0) {
//			afterWhere = afterWhere.substring(0, pos);
//		}

		if (! expected.equals(afterWhere)) {
			failures.add(testName + ": got '" + afterWhere + "' but expected '" + expected + "'");
		}
	}

	private void showResponse(String heading, DalResponseBuilder responseBuilder) throws ParseException {
		String json_s = responseBuilder.asString();
		JsonParser json = new JsonParser(json_s);
		
		if (! json.isMapResult()) {
			throw new RuntimeException(heading+":  not a JsonMap result");
		}
		JsonMap jm = json.getMapResult();
		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(baos);
//		json.printOn(ps);
//		ps.close();
		
		System.out.println("========== " + heading);
		System.out.println(jm.toJsonString());
//		System.out.println(baos.toString());
	}

}
