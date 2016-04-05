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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.imageio.spi.ServiceRegistry;
import javax.swing.SwingUtilities;

import net.pearcan.ui.GuiUtil;
import net.pearcan.ui.desktop.MacApplication;
import net.pearcan.ui.desktop.MacApplicationException;
import net.pearcan.util.Util;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.AuthenticationException;
import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.db.SqlDalDatabase;
import com.diversityarrays.dal.db.SystemGroupInfo;
import com.diversityarrays.dal.db.UserInfo;
//import com.diversityarrays.dal.db.bms.BMS_DalDbProviderService;
//import com.diversityarrays.dal.db.kddart.KddartDalDbProviderService;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.ops.OperationMatch;
import com.diversityarrays.dal.ops.WordNode;
import com.diversityarrays.dal.service.DalDbProviderService;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterValue;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.SessionExpiryOption;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.SimpleWebServer;

public class DalServer extends SimpleWebServer implements IDalServer {

	private static final String YOU_NEED_TO_LOGIN_FIRST = "You need to login first";

	static final private String DAL_SERVER_VERSION = "1.0.1";
	
	public String contentType;

	static private void fatal(String msg) {
		System.err.println("?" + msg);
		giveHelpThenExit(1);
	}

	static private final String[] CMD_HELP_LINES = {
			"Usage: java -jar dalserver.jar [OPTIONS] [databaseServiceName]",
			"Options are:",
			"--            ignore everything after this 'option'",
			"-version      print version number and exit",
			"-help         print this help and exit",
			"-expire NN    auto-expiry minutes for sessions",
			"-localhost    listen on localhost:PORT instead of <ip-address>:PORT",
			"-port PORT    specifies the port number to listen on (default is "
					+ DalServerUtil.DEFAULT_DAL_SERVER_PORT + ")", "",
			"-docroot DIR  specifies a folder from which to service files",
	};

	public static void giveHelpThenExit(int code) {
		for (String line : CMD_HELP_LINES) {
			System.out.println(line);
		}
		System.out.println("Available databaseServiceNames are:");
		for (Iterator<DalDbProviderService> iter = ServiceRegistry.lookupProviders(DalDbProviderService.class);
				iter.hasNext(); ) 
		{
			DalDbProviderService provider = iter.next();
			System.out.println("\t" + provider.getProviderName());
		}
		System.exit(code);
	}

	public static void main(String[] args) {

		String host = null;
		int port = DalServerUtil.DEFAULT_DAL_SERVER_PORT;

		int inactiveMins = DalServerUtil.DEFAULT_MAX_INACTIVE_MINUTES;

		File docRoot = null;
		
		String serviceName = null;

		for (int i = 0; i < args.length; ++i) {
			String argi = args[i];
			if (argi.startsWith("-")) {
				if ("--".equals(argi)) {
					break;
				}

				if ("-version".equals(argi)) {
					System.out.println(DAL_SERVER_VERSION);
					System.exit(0);
				}
				
				if ("-help".equals(argi)) {
					giveHelpThenExit(0);
				}

				if ("-docroot".equals(argi)) {
					if (++i >= args.length || args[i].startsWith("-")) {
						fatal("missing value for " + argi);
					}
					docRoot = new File(args[i]);
				} else if ("-sqllog".equals(argi)) {
					SqlUtil.logger = Logger.getLogger(SqlUtil.class.getName());
				} else if ("-expire".equals(argi)) {
					if (++i >= args.length || args[i].startsWith("-")) {
						fatal("missing value for " + argi);
					}

					try {
						inactiveMins = Integer.parseInt(args[i], 10);
						if (inactiveMins <= 0) {
							fatal("invalid minutes: " + args[i]);
						}
					} catch (NumberFormatException e) {
						fatal("invalid minutes: " + args[i]);
					}
				} else if ("-localhost".equals(argi)) {
					host = "localhost";
				} else if ("-port".equals(argi)) {
					if (++i >= args.length || args[i].startsWith("-")) {
						fatal("missing value for " + argi);
					}
					try {
						port = Integer.parseInt(args[i], 10);
						if (port < 0 || port > 65535) {
							fatal("invalid port number: " + args[i]);
						}
					} catch (NumberFormatException e) {
						fatal("invalid port number: " + args[i]);
					}
				} else {
					fatal("invalid option: " + argi);
				}
			}
			else {
				if (serviceName != null) {
					fatal("multiple serviceNames not supported: " + argi);
				}
				serviceName = argi;
			}
		}

		final DalServerPreferences preferences = new DalServerPreferences(Preferences.userNodeForPackage(DalServer.class));

		if (docRoot == null) {
			docRoot = preferences.getWebRoot(new File(System.getProperty("user.dir"), "www"));
		}

		DalServer server = null;

		if (serviceName!=null && docRoot.isDirectory()) {
			try {
				DalDatabase db = createDalDatabase(serviceName, preferences);
				if (db.isInitialiseRequired()) {
					Closure<String> progress = new Closure<String>() {
						@Override
						public void execute(String msg) {
							System.out.println("Database Initialisation: " + msg);
						}
					};
					db.initialise(progress);
				}
				server = create(preferences, host, port, docRoot, db);
			} catch (NoServiceException e) {
				throw new RuntimeException(e);
			} catch (DalDbException e) {
				throw new RuntimeException(e);
			}
		}
		
		Image serverIconImage = null;
		InputStream imageIs = DalServer.class.getResourceAsStream("dalserver-24.png");
		if (imageIs != null) {
			try {
				serverIconImage = ImageIO.read(imageIs);
				
				if (Util.isMacOS()) {
					try {
						MacApplication macapp = new MacApplication(null);
						macapp.setDockIconImage(serverIconImage);
					} catch (MacApplicationException e) {
						System.err.println(e.getMessage());
					}
				}

			} catch (IOException ignore) {
			}
		}

		if (server != null) {
			server.setMaxInactiveMinutes(inactiveMins);
		}
		else {
			AskServerParams asker = new AskServerParams(serverIconImage, null,
					"DAL Server Start", docRoot, preferences);
			GuiUtil.centreOnScreen(asker);
			asker.setVisible(true);

			if (asker.cancelled) {
				System.exit(0);
			}

			host = asker.dalServerHostName;
			port = asker.dalServerPort;
			inactiveMins = asker.maxInactiveMinutes;

			server = create(preferences, host, port, asker.wwwRoot, asker.dalDatabase);
			// server.setUseSimpleDatabase(asker.useSimpleDatabase);
		}

		final DalServer f_server = server;
		final File f_wwwRoot = docRoot;
		final Image f_serverIconImage = serverIconImage;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DalServerFactory factory = new DalServerFactory() {
					@Override
					public DalServer create(String hostName, int port, File wwwRoot, DalDatabase dalDatabase) {
						return DalServer.create(preferences, hostName, port, wwwRoot, dalDatabase);
					}
				};
				ServerGui gui = new ServerGui(f_serverIconImage, f_server, factory, f_wwwRoot, preferences);
				gui.setVisible(true);
			}
		});

	}

	/**
	 * 
	 * @param databaseFolder
	 * @param databaseServer
	 * @param databasePort
	 * @param wwwRoot
	 * @param host
	 * @param serviceName
	 * @param prefs
	 * @param errors
	 * @throws IllegalArgumentException if no provider or unable to create
	 * @return
	 * @throws NoServiceException 
	 */
	private static DalDatabase createDalDatabase(String dalDbServiceName, DalServerPreferences prefs)
	throws NoServiceException 
	{
		DalDbProviderService provider = null;
		//DalDbProviderService providerDart = new KddartDalDbProviderService();
		//DalDbProviderService providerBMS = new BMS_DalDbProviderService();
		for (Iterator<DalDbProviderService> iter = ServiceRegistry.lookupProviders(DalDbProviderService.class);
			iter.hasNext(); ) 
		{
			provider = iter.next();
			if (dalDbServiceName.equals(provider.getProviderName())) {
				break;
			}
		}
		
		if (provider == null) {
			throw new NoServiceException("No provider for " + dalDbServiceName);
		}
		

		Set<Parameter<?>> required = provider.getParametersRequired();
		Map<Parameter<?>, Throwable> errors = new LinkedHashMap<Parameter<?>, Throwable>();
		Map<Parameter<?>, ParameterValue<?>> savedSettings = prefs.loadSavedSettings(provider, required, errors);

		if (! errors.isEmpty()) {
			StringBuilder sb = new StringBuilder("Parameter errors:");
			for (Parameter<?> p : errors.keySet()) {
				sb.append("\n  ").append(p.name).append(errors.get(p).getMessage());
			}
			throw new NoServiceException(sb.toString());
		}

		Set<ParameterValue<?>> parameterValues = new LinkedHashSet<ParameterValue<?>>();
		for (Parameter<?> p : required) {
			ParameterValue<?> v = savedSettings.get(p);
			if (v != null) {
				parameterValues.add(v);
			}
		}
		
		try {
			Closure<String> progress = new Closure<String>() {
				@Override
				public void execute(String msg) {
					System.out.println(msg);
				}
			};
			return provider.createDatabase(parameterValues, progress, false);

		} catch (DalDbException e) {
			throw new NoServiceException("provider "
					+ provider.getProviderName()
					+ " couldn't create DalDatabase",
					e);
		}
	}

	private boolean useSimpleDatabase;

	public void setUseSimpleDatabase(boolean b) {
		useSimpleDatabase = b;
	}

	@Override
	public boolean getUseSimpleDatabase() {
		return useSimpleDatabase;
	}

	static public DalServer create(DalServerPreferences prefs, String host, int port, File wwwroot,
			DalDatabase dd) {

		if (host == null) {
			try {
				host = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				host = "localhost";
			}
		}

		return new DalServer(prefs, host, port, wwwroot, dd);
	}

	private boolean verbose;
	private final DalDatabase dalDatabase;
	private WordNode wordNodeRoot = new WordNode();

	private DalSessionStore dalSessionStore = new DalSessionStore();

	private int maximumInactivityMinutes = DalServerUtil.DEFAULT_MAX_INACTIVE_MINUTES;
	private long maximumInactivityMillis = maximumInactivityMinutes * 60 * 1000L;


	@SuppressWarnings("unused")
	private final DalServerPreferences preferences;;

	public DalServer(DalServerPreferences prefs, String host, int port, File wwwroot, DalDatabase dd) {
		super(host, port, wwwroot, true);
		this.preferences = prefs;
		this.dalDatabase = dd;

		DalServerUtil.buildWordTree(dalDatabase.getOperations(), wordNodeRoot);
	}

	public NanoHTTPD getHttpServer() {
		return this;
	}

	@Override
	public void setMaxInactiveMinutes(int mins) {
		this.maximumInactivityMinutes = mins;
		this.maximumInactivityMillis = mins * 60 * 1000L;
	}

	public int getMaxInactiveMinutes() {
		return maximumInactivityMinutes;
	}

	public String getDalServerVersion() {
		return DAL_SERVER_VERSION;
	}

	public int getDalOperationCount() {
		return dalDatabase.getOperations().size();
	}

	public DalDatabase getDalDatabase() {
		return dalDatabase;
	}

	public boolean isQuiet() {
		return !isVerbose();
	}

	public void setQuiet(boolean b) {
		setVerbose(!b);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public Response serve(IHTTPSession session) {

		Map<String, String> filePathByName = new HashMap<String, String>();
		Method method = session.getMethod();
		if (Method.PUT.equals(method) || Method.POST.equals(method)) {
			try {
				session.parseBody(filePathByName);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return new Response(Response.Status.INTERNAL_ERROR,
						MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			} catch (ResponseException re) {
				re.printStackTrace();
				return new Response(re.getStatus(), MIME_PLAINTEXT,
						re.getMessage());
			}
		}

		Response r = serveImpl(session.getUri(), method, session, filePathByName);

		r.addHeader("Access-Control-Allow-Methods", "GET, POST");
		// r.addHeader("Access-Control-Allow-Credentials", "true");
		r.addHeader("Access-Control-Allow-Origin", "*");
		r.addHeader("X-XSS-Protection", "0"); // supposedly to allow Safari debugging
		// r.addHeader("Access-Control-Allow-Headers", "Content-Type, *");

		return r;
	}

	private Response serveImpl(String uri, 
			Method method, 
			IHTTPSession session,
			Map<String, String> filePathByName) 
	{
		Response result;

		boolean wantJson = "application/json".equals(session.getHeaders().get("content-type"));
		/**
		 * Added by Raul Hernandez T.
		 * Used when is necessary to return cvs responses 
		 */
		contentType = session.getHeaders().get("content-type");

		if (verbose) {
			System.out.println(method + " '" + uri + "' ");

			Map<String, String> headers = session.getHeaders();
			Iterator<String> e = headers.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  HDR: '" + value + "' = '"
						+ headers.get(value) + "'");
			}

			Map<String, String> parms = session.getParms();
			e = parms.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				System.out.println("  PRM: '" + value + "' = '"
						+ parms.get(value) + "'");
			}
		}

		if ("/help".equals(uri)) {
			result = giveHelp();
		} else if (uri.equals("/sessions")) {
			result = doListSessions();
		} else if (Method.GET.equals(method) && uri.startsWith("/entity:")) {
			result = doEntityInfo(uri.substring(8));
		} else if (Method.POST.equals(method) && uri.endsWith("/entity")) {
			result = doEntityInfo(session.getParms().get("entity"));
		} else if (Method.GET.equals(method) && uri.startsWith("/sql:")) {
			if (dalDatabase instanceof SqlDalDatabase) {
				result = createSqlQueryResponse((SqlDalDatabase) dalDatabase, uri.substring(5), null);
			}
			else {
				result = DalServerUtil.buildNotSqlDalDatabaseTextResponse(dalDatabase);
			}
		} else if (Method.POST.equals(method) && uri.endsWith("/sql")) {
			if (dalDatabase instanceof SqlDalDatabase) {
				result = createSqlQueryResponse((SqlDalDatabase) dalDatabase, session.getParms().get("sql"), null);
			}
			else {
				result = DalServerUtil.buildNotSqlDalDatabaseTextResponse(dalDatabase);
			}
		} else if (Method.GET.equals(method) && uri.startsWith("/table:")) {
			result = doTable(uri.substring(7));
		} else if (Method.POST.equals(method) && uri.endsWith("/table")) {
			result = doTable(session.getParms().get("table"));
		} else if (uri.startsWith("/dal/")) {
			String[] returnSql = new String[1];
			result = doDal(wantJson, method, uri.substring(5), session, filePathByName, returnSql);
			if (result != null) {
				IStatus status = result.getStatus();
				String desc = status.getDescription();
				System.out.println("\tresult.status=" + desc);
				if (returnSql[0] != null) {
					System.out.println("\tSQL: " + returnSql[0]);
				}
			}
		} else {
			result = super.serve(session);
		}

		return result;
	}

	private Response doListSessions() {

		DalSession[] sessions = dalSessionStore.getSessions();

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h2>Active Sessions:").append(sessions.length)
				.append("</h2>");
		if (sessions.length > 0) {
			emitSessions(sessions, sb);
		}
		sb.append("</body></html>");

		return new Response(Response.Status.OK, MIME_HTML, sb.toString());
	}

	private void emitSessions(DalSession[] sessions, StringBuilder sb) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

		sb.append("<table border='1'>");
		sb.append("<thead><tr>");
		sb.append("<th>Username</th>");
		sb.append("<th>User Id</th>");
		sb.append("<th>Last Active</th>");
		sb.append("<th>Expiry Option</th>");
		sb.append("<th>WriteToken</th>");
		sb.append("<th>Session Id</th>");
		sb.append("</tr></thead><tbody>");

		for (DalSession sess : sessions) {
			sb.append("<tr>").append("<td>")
					.append(DbUtil.htmlEscape(sess.getUserName())).append("</td>")
					.append("<td>").append(sess.getUserId()).append("</td>")
					.append("<td>").append(df.format(sess.getLastActive()))
					.append("</td>").append("<td>")
					.append(sess.sessionExpiryOption).append("</td>")
					.append("<td>").append(sess.writeToken).append("</td>")
					.append("<td>").append(sess.sessionId).append("</td>")
					.append("</tr>");
		}
		sb.append("</tbody></table>");
	}

	private Response doDal(boolean wantJson, 
			Method method, 
			String dalcmd,
			IHTTPSession session, 
			Map<String, String> filePathByName, 
			String[] returnSql)
	{

		System.out.println("DAL: " + dalcmd);

		Response result;

		CookieHandler cookies = session.getCookies();
		String sessionId = cookies.read(DalSession.COOKIE_NAME_DAL_SESSION_ID);

		DalSession dalSession = null;
		if (sessionId != null) {
			dalSession = dalSessionStore.getSession(sessionId);

			if (dalSession == null) {
				System.out.println("\tNO session for sessionId=" + sessionId);
			} else {
				System.out.println("\tsession: " + dalSession);
				if (dalSession.hasExpired(maximumInactivityMillis)) {
					System.out.println("\t **expired**");
					dalSessionStore.removeSession(dalSession);
					// But if they are trying to login again, this isn't an error !
					if (! dalcmd.startsWith(DalOperation.LOGIN_STEM)) {
						return DalServerUtil.buildAuthErrorResponse(wantJson,
								"Session expired. Please login again.");
					}
					dalSession = null;
				}
			}
		}

		if (dalcmd.startsWith(DalOperation.LOGIN_STEM)) {
			if (dalSession == null) {
				result = handleLogin(wantJson, dalcmd, session);
			} 
			else {
				removeCookies(session);
				dalSessionStore.removeSession(dalSession);
				System.out.println("!!! Removed session: " + dalSession);
				result = DalServerUtil.buildAuthErrorResponse(wantJson,
						"You were already logged-in as " + dalSession.getUserName()
						+ ". You are now logged-out");
				// result = DalServerUtil.buildAuthErrorResponse(wantJson, "Already login.");
			}
		} else if (dalcmd.equals(DalOperation.LOGOUT)) {
			if (dalSession != null) {
				result = handleLogout(dalSession, wantJson, dalcmd, session);
				dalSessionStore.removeSession(dalSession);
				System.out.println("!!! Removed session: " + dalSession);
			}
			else {
				// TODO check if need to do: removeCookies(session);
				result = DalServerUtil.buildAuthErrorResponse(wantJson, "You are not logged-in");
			}
		} else if (dalcmd.equals(DalOperation.GET_LOGIN_STATUS)) {
			result = handleGetLoginStatus(wantJson, dalSession);
		} else if (dalcmd.equals(DalOperation.GET_VERSION)) {
			// We can do this regardless of login status
			result = createGetVersionResult(wantJson, dalSession);
		} else if (dalSession == null) {
			// If there isn't a session you can get past here!
			result = DalServerUtil.buildAuthErrorResponse(wantJson, "(0) "
					+ YOU_NEED_TO_LOGIN_FIRST);
		} else {
			if (dalSession != null) {
				dalSession.delayExpiry();
			}

			if (DalOperation.LIST_OPERATION.equals(dalcmd)) {
				result = doListOperation(wantJson);
			} 
			else if (DalOperation.LIST_GROUP.equals(dalcmd) || DalOperation.LIST_ALL_GROUP.equals(dalcmd)) {
				result = handleListOrListAllGroup(wantJson, dalcmd, dalSession, returnSql);
			}
			else if (dalcmd.endsWith(DalOperation.LIST_FIELD_TAIL)) {
				result = handleListField(wantJson, dalcmd, dalSession);
			}
			else if (dalcmd.startsWith(DalOperation.SWITCH_GROUP_STEM)) {
				result = handleSwitchGroup(wantJson, dalcmd, dalSession);
			}
			else if (dalcmd.equals(DalOperation.GET_VERSION)) {
				// Should have already been done but here for completeness
				result = createGetVersionResult(wantJson, dalSession);
			}
			else {
				StringBuilder errmsg = new StringBuilder();
				OperationMatch match = getOperationMatch(dalcmd, errmsg);
				if (match == null || match.node.getOperation() == null) {
					result = DalServerUtil.buildNotFoundResponse(wantJson,
							"No matching operation for '" + dalcmd + "' ("
									+ errmsg + ")");
				} else {
					result = doOperation(dalSession, wantJson, match, method, dalcmd, session, filePathByName);
				}
			}
		}
		return result;
	}

	public Response createGetVersionResult(boolean wantJson, DalSession session) {
		try {
			return DalServerUtil
					.createBuilder(wantJson)
					.startTag(DALClient.TAG_INFO)
					.attribute("Copyright",
							"Copyright (c) 2011-2014, Diversity Arrays Technology, All rights reserved.")
					.attribute(DALClient.ATTR_VERSION, dalDatabase.getDatabaseVersion(session))
					.attribute("ServerVersion", DAL_SERVER_VERSION)
					.attribute("About", "Data Access Layer").endTag()
					.build(Response.Status.OK);
		} catch (DalDbException e) {
			return DalServerUtil.buildInternalErrorResponse(wantJson, e);
		}
	}

	public Response handleGetLoginStatus(boolean wantJson, DalSession dalSession) {
		
		Response result;
		
		DalResponseBuilder builder = DalServerUtil.createBuilder(wantJson);

		builder.startTag(DALClient.TAG_INFO);

		if (dalSession == null) {
			builder.attribute(DALClient.ATTR_GROUP_SELECTION_STATUS, "0")
					.attribute(DALClient.ATTR_LOGIN_STATUS, "0");
		} else {
			builder.attribute(DALClient.ATTR_GROUP_SELECTION_STATUS, dalSession.getGroupId() == null ? "0" : "1")
					.attribute(DALClient.ATTR_LOGIN_STATUS, "1"); // dalSession implies logged-in
		}

		builder.endTag();

		result = builder.build(Response.Status.OK);
		return result;
	}

	private Response handleSwitchGroup(boolean wantJson, String dalcmd, DalSession dalSession) 
	{
		Response result;
			
		// TODO pass this on to dalDatabase.switchGroup() in case it is a forwarder
		//      and react appropriately

		String[] parts = dalcmd.split("/");
		if (parts.length == 3 && parts[2].matches("\\d+")) {
			String groupId = parts[2];

			try {
				// Does the database have this group?
				SystemGroupInfo groupInfo = dalDatabase.getSystemGroupInfo(dalSession);
				if (groupInfo == null) {
					result = DalServerUtil.buildAuthErrorResponse(wantJson,
							"User " + dalSession.getUserId()
							+ " is not a member of group "
							+ groupId);
				} else {
					// Yup - and this user is a member...
					dalSession.setGroupId(groupInfo.getGroupId());

					result = DalServerUtil
							.createBuilder(wantJson)
							.startTag(DALClient.TAG_INFO)
							.attribute(
									DALClient.ATTR_MESSAGE,
									"You have been switched to " + groupId + " successfully.")
							.attribute(DALClient.ATTR_GROUP_NAME, groupInfo.getGroupName())
							.attribute(DALClient.ATTR_GADMIN, groupInfo.isGroupOwner() ? "TRUE" : "FALSE")
							.endTag()
							.build(Response.Status.OK);
				}
			} catch (DalDbException e) {
				result = DalServerUtil.buildInternalErrorResponse(wantJson, e);
			}
		} else {
			result = DalServerUtil.buildNotFoundResponse(wantJson, "Invalid command: " + dalcmd);
		}

		return result;
	}

	private Response handleListOrListAllGroup(boolean wantJson, String dalcmd,
			DalSession session, String[] returnSql) 
	{
		Response result = null;

		DalResponseBuilder builder = DalServerUtil.createBuilder(wantJson);

		try {
			if (DalOperation.LIST_ALL_GROUP.equals(dalcmd)) {
				dalDatabase.performListAllGroup(session, builder, returnSql);
				result = builder.build(Response.Status.OK);
			} else if (DalOperation.LIST_GROUP.equals(dalcmd)) {
				

				dalDatabase.performListGroup(session, builder, returnSql);

				result = builder.build(Response.Status.OK);
			} else {
				// handled by if (result==null) below...
			}
		} catch (DalDbException e) {
			Throwable t = e.getCause();
			if (t == null) {
				t = e;
			}
			result = DalServerUtil.buildInternalErrorResponse(wantJson,
					t.getMessage() + ":" + dalcmd);
		}

		if (result == null) {
			result = DalServerUtil.buildNotFoundResponse(wantJson,
					"Unrecognised LIST command: " + dalcmd);
		}

		return result;
	}

	private Response handleLogout(DalSession dalSession, boolean wantJson, String dalcmd, IHTTPSession session) 
	{
		Response r = null;
		try {
			dalDatabase.doLogout(dalSession);
		}
		finally {
			r = DalServerUtil
					.createBuilder(wantJson)
					.startTag(DALClient.TAG_INFO)
					.attribute(DALClient.ATTR_MESSAGE,
							"You have logged out successfully.").endTag()
					.build(Response.Status.OK);

			removeCookies(session);
		}

		return r;
	}

	public void removeCookies(IHTTPSession session) {
		CookieHandler cookies = session.getCookies();
		for (String cookieName : DalSession.COOKIE_NAMES) {
			cookies.delete(cookieName);
		}
	}

	private Response handleLogin(boolean wantJson, String dalcmd, IHTTPSession session) {
		Response result;
		String[] parts = dalcmd.split("/");
		if (parts.length != 3) {
			result = DalServerUtil.buildAuthErrorResponse(wantJson,
					"(1) missing parameters");
		} else {
			String userName = parts[1];
			String seoName = parts[2];

			SessionExpiryOption seo = SessionExpiryOption.lookup(seoName);
			if (seo == null) {
				return DalServerUtil.buildAuthErrorResponse(wantJson,
						"(2) Invalid SessionExpiryOption=" + seoName);
			}

			try {
				String newSessionId = DalSession.createSessionId();
				UserInfo userInfo = dalDatabase.doLogin(newSessionId, userName, seo, session.getParms());

				DalSession dalSession = new DalSession(newSessionId, userInfo, seo);
				dalSessionStore.addSession(dalSession);

				result = DalServerUtil
						.createBuilder(wantJson)

						.startTag(DALClient.TAG_USER)
						.attribute(DALClient.ATTR_USER_ID, userInfo.getUserId())
						.endTag()

						.startTag(DALClient.TAG_WRITE_TOKEN)
						.attribute(DALClient.ATTR_VALUE, dalSession.writeToken)
						.endTag()

						.build(Response.Status.OK);

				List<Cookie> cookies = dalSession.getCookies();
				if (cookies != null) {
					System.out.println("Cookies for dalSession:" + dalSession);
					for (Cookie cookie : cookies) {
						System.out.println("\t" + cookie);
						session.getCookies().set(cookie);
					}
				}
			} catch (AuthenticationException ae) {
				Throwable e = ae.getCause();
				if (e == null) {
					e = ae;
				}

				System.err.println(e.getMessage());
				result = DalServerUtil.buildAuthErrorResponse(wantJson, "(3) "
						+ e.getMessage());
			}
		}
		return result;
	}

	private OperationMatch getOperationMatch(String dalcmd, StringBuilder errmsg) {

		OperationMatch result = null;

		StringBuilder entityErrmsg = new StringBuilder();
		StringBuilder nonEntityErrmsg = new StringBuilder();
		
		OperationMatch match = DalDatabaseUtil.findOperationMatch(dalcmd, wordNodeRoot, entityErrmsg);

		if (match.node != null) {
			result = match;
		} else {
			String e = entityErrmsg.length() > 0 ? entityErrmsg.toString()
					: nonEntityErrmsg.toString();
			errmsg.append(e);
		}

		return result;
	}

	private Response doEntityInfo(String entityName) {
		Response result;
		if (entityName==null || entityName.isEmpty()) {
			result = new Response(Response.Status.OK, MIME_PLAINTEXT,
					"Entity Names:\n"
							+ DbUtil.join("\n", dalDatabase.getEntityNames()));
		} else {
			String usedName = entityName.toLowerCase();
			List<DalOperation> ops = new ArrayList<DalOperation>();

			for (DalOperation op : dalDatabase.getOperations()) {
				if (entityName.equals(op.getEntityName())) {
					ops.add(op);
				} else if (usedName.equals(op.getEntityName())) {
					ops.add(op);
				}
			}

			StringBuilder sb = new StringBuilder("<html><body>");

			if (ops.isEmpty()) {
				sb.append("<b>No operations found for '").append(entityName)
						.append("'");
			} else {
				sb.append("<b>Operations for '");
				sb.append(entityName).append("'</b>");
				if (!usedName.equals(entityName)) {
					sb.append(" (as ").append(usedName).append(")");
				}
				sb.append("<ul>");
				for (DalOperation op : ops) {
					sb.append("<li>").append(DbUtil.htmlEscape(op.getCommandTemplate()))
							.append("</li>");
				}
				sb.append("</ul><hr/>");

				try {
					if (dalDatabase instanceof SqlDalDatabase) {
						SqlDalDatabase sqldb = (SqlDalDatabase) dalDatabase;
						appendPossibleEntityTableDetails(sqldb,
								sqldb.createShowTableColumnsSql(entityName), sb);
					}
				} catch (DalDbException e) {
					sb.append("<hr/><b>Exception</b><br/>").append(
							DbUtil.htmlEscape(e.getMessage()));
				}
			}
			sb.append("</body></html>");

			result = new Response(Response.Status.OK, MIME_HTML, sb.toString());
		}
		return result;
	}

	static private void appendPossibleEntityTableDetails(
			SqlDalDatabase sqldb, String sql, StringBuilder sb)
	throws DalDbException 
	{

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {

			conn = sqldb.getConnection(false);

			stmt = conn.createStatement();

			// sb.append("<code>").append(DbUtil.htmlEscape(sql)).append("</code><hr/>");

			boolean hasResultSet = stmt.execute(sql);

			if (hasResultSet) {
				rs = stmt.getResultSet();
				DalServerUtil.appendResultSetRowsAsTable("No entity table", rs,
						sb);
			} else {
				int n = stmt.getUpdateCount();
				sb.append("Update count=").append(n);
			}
		} catch (SQLException e) {
			throw new DalDbException(e);
		} finally {
			SqlUtil.closeSandRS(stmt, rs);
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignore) {
				}
			}
		}

	}

	private Response doOperation(DalSession dalSession,
			boolean wantJson, 
			OperationMatch match,
			Method method, 
			String uri, 
			IHTTPSession session, 
			Map<String, String> filePathByName)
	{
		Response result;

		if (DalOperation.LIST_OPERATION.equals(match.node.getOperation() .getCommandTemplate())) {
			/*
			 * Note that we really shouldn't ever get here as this command should
			 * have been detected and dispatched by our caller. 
			 */
			result = doListOperation(wantJson);
		} else {
			// NOT "list/operation"
			DalOperation dalop = match.node.getOperation();
			List<String> dalOpParameters = collectDalOperationParameters(match, dalop);
		    DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(wantJson);
			
			try {
				dalop.execute(dalSession,
						responseBuilder, method, uri, 
						dalOpParameters, 
						session.getParms(), 
						filePathByName);
				result = responseBuilder.build(Response.Status.OK);
			} catch (AuthenticationException e) {
				result = DalServerUtil.buildAuthErrorResponse(wantJson, e.getMessage());
			} catch (DalDbException e) {
				Throwable t = e.getCause();
				if (t == null) {
					t = e;
				}
				result = DalServerUtil.buildInternalErrorResponse(wantJson, t);
			}
		}

		return result;
	}
	
	/**
	 * List all of the operations available for the DalDatabase we are serving.
	 * @param wantJson
	 * @return the Response
	 */
	private Response doListOperation(boolean wantJson) {
		DalResponseBuilder builder = DalServerUtil.createBuilder(wantJson);
		builder.addResponseMeta("Operation");
		
		Set<String> seen = new HashSet<String>();
		for (String op: DalOperation.MORE_OPS) {
			seen.add(op);
			builder.startTag("Operation").attribute("REST", op).endTag();
		}
		
		for (DalOperation dalop : dalDatabase.getOperations()) {
			String op = dalop.getCommandTemplate();
			if (! seen.contains(op)) {
				builder.startTag("Operation").attribute("REST", op).endTag();
			}
		}

		return builder.build(Response.Status.OK);
	}

	// dalcmd ends with LIST_FIELD_TAIL
	private Response handleListField(boolean wantJson, String dalcmd, DalSession session) {	
		Response result;
		
		String[] parts = dalcmd.split("/");
		if (parts.length != 3) {
			result = DalServerUtil.buildNotFoundResponse(wantJson, "Unknown operation: "+dalcmd);
		}
		else {
			String _tname = parts[0];
			try {
				DalResponseBuilder responseBuilder = DalServerUtil.createBuilder(wantJson);
				dalDatabase.performListField(session, _tname, responseBuilder);
				
				result = responseBuilder.build(Response.Status.OK);
			}
			catch (DalDbException e) {
				result = DalServerUtil.buildInternalErrorResponse(wantJson, e.getMessage());
			}
		}
	
		return result;
	}


	private List<String> collectDalOperationParameters(
			OperationMatch match, DalOperation dalop) 
	{
		List<String> parameters = new ArrayList<String>();
		int nParameters = dalop.getParameterCount();
		for (int i = 0; i < nParameters; ++i) {
			if (i >= match.getParameterValueCount()) {
				break;
			}
			parameters.add(match.getParameterValue(i));
		}
		return parameters;
	}

	static private final String[] HELP_LINES = {
			"help             gives this output",
			// "dbinit[:NAMEs]   initialises the database (?yes to actually do the init instead of just showing the commands)",
			// "                 if supplied then NAMEs will only action the names provided",
			"sql:SQL          runs the SQL statement (SELECT/INSERT/UPDATE/DELETE)",
			"table:NAME       describes the NAMEd table or lists all tables if NAME is not supplied",
			"entity:NAME      lists the operations for NAME or all entity names if NAME is not supplied",
			"sessions         lists details of all sessions", "",
			"dal/...          are treated as DAL commands" };

	private Response giveHelp() {
		return new Response(Response.Status.OK, MIME_PLAINTEXT, DbUtil.join(
				"\n", (Object[]) HELP_LINES));
	}

	private Response doTable(String tableCommand) {
		
		if (! (dalDatabase instanceof SqlDalDatabase)) {
			return DalServerUtil.buildNotSqlDalDatabaseTextResponse(dalDatabase);
		}
		
		SqlDalDatabase sqldb = (SqlDalDatabase) dalDatabase;
		
		String tableName = tableCommand.trim();

		String sql;
		if (tableName.isEmpty()) {
			sql = sqldb.createShowTablesSql();
		} else {
			sql = sqldb.createShowTableColumnsSql(tableName);
		}
		return createSqlQueryResponse(sqldb, sql, null);
	}

	private SqlWorker createSqlWorker(SqlDalDatabase sqldb) {
		SqlWorker w = new SqlWorker(sqldb);
		w.setVerbose(isVerbose());
		return w;
	}

	private Response createSqlQueryResponse(SqlDalDatabase sqldb, String sql, String metaTagName) {
		SqlWorker sqlWorker = createSqlWorker(sqldb);
		try {
			return sqlWorker.createResponse(SqlResponseType.TEXT, sql,
					metaTagName, null);
		} finally {
			sqlWorker.close();
		}
	}

}
