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
package com.diversityarrays.dal.db.kddart;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.AbstractDalDatabase;
import com.diversityarrays.dal.db.AuthenticationException;
import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.DbDataLoader;
import com.diversityarrays.dal.db.SystemGroupInfo;
import com.diversityarrays.dal.db.UserInfo;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.Genotype;
import com.diversityarrays.dal.entity.Genus;
import com.diversityarrays.dal.ops.AbstractDalOperation;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.server.DalSession;
import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseFormatException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;
import com.diversityarrays.dalclient.DefaultDALClient;
import com.diversityarrays.dalclient.ResponseType;
import com.diversityarrays.dalclient.SessionExpiryOption;

import fi.iki.elonen.NanoHTTPD.Method;

public class KddartDalDatabase extends AbstractDalDatabase {

	private static final String DATABASE_VERSION = "0.1";

	private static final boolean USE_JSON = Boolean.parseBoolean(KddartDalDatabase.class.getName()+".USE_JSON");

	public static final String PARAM_USERNAME = "Username";
	public static final String PARAM_PASSWORD = "Password";

	private Map<String,Class<? extends DalEntity>> entityClassByName = new HashMap<String,Class<? extends DalEntity>>();
	private List<DalOperation> operations;
	
	private final String dalUrl;

	private Map<String,DALClient> clientBySessionId = new HashMap<String, DALClient>();

	private final String dalUsername;

	private final String dalPassword;
	
	private boolean autoSwitchGroupOnLogin;

	public KddartDalDatabase(Closure<String> progress, boolean test, URI uri, String username, String password, Boolean autoSwitchGroup) throws DalDbException {
		super("KDDart-DAL@" + uri.toString());
		
		this.dalUrl = uri.toString();
		this.dalUsername = username;
		this.dalPassword = password;
		this.autoSwitchGroupOnLogin = autoSwitchGroup==null ? false : autoSwitchGroup.booleanValue();
		
		entityClassByName.put("genus", Genus.class);
		entityClassByName.put("genotype", Genotype.class);
		
		if (test) {
			DALClient client = new DefaultDALClient(dalUrl);
			try {
				progress.execute("Attempting login to "+dalUrl);
				client.login(username, password);
			} catch (DalException e) {
				throw new DalDbException(e);
			} catch (IOException e) {
				throw new DalDbException(e);
			} finally {
				client.logout();
			}
		}
	}

	@Override
	public String getDatabaseVersion(DalSession session) throws DalDbException {
		DALClient client = session==null ? null : clientBySessionId.get(session.sessionId);
		if (client == null) {
			return DATABASE_VERSION;
		}
		else {
			try {
				DalResponse response = client.performQuery(DalOperation.GET_VERSION);
				return response.getRecordFieldValue(DALClient.TAG_INFO, DALClient.ATTR_VERSION);
			} catch (DalResponseException e) {
				throw new DalDbException(e);
			} catch (IOException e) {
				throw new DalDbException(e);
			}
		}
		
	}

	@Override
	public List<DalOperation> getOperations() {
		/*
<Operation REST="get/genus/_id" />
<Operation REST="list/genus" />

<Operation REST="genus/_genusid/list/genotype" />
<Operation REST="genus/_genusid/list/genotype/_nperpage/page/_num" />

<Operation REST="get/genotype/_id" />
<Operation REST="list/genotype/_nperpage/page/_num" />

<Operation REST="export/genotype" />

<Operation REST="genotype/_genoid/list/alias" />
<Operation REST="genotype/_genoid/list/ancestor" />
<Operation REST="genotype/_genoid/list/descendant" />
<Operation REST="genotype/_genoid/list/specimen" />
<Operation REST="genotype/_genoid/list/specimen/_nperpage/page/_num" />
<Operation REST="genotype/_genoid/remove/trait/_traitid" />
<Operation REST="genotype/_id/list/trait" />

list/group

		 */
		
		if (operations==null) {
			synchronized (this) {
				if (operations == null) {
					List<DalOperation> tmp = new ArrayList<DalOperation>();

					tmp.add(new ForwardingDalOperation("genus", "get/genus/_id"));
					tmp.add(new ForwardingDalOperation("genus", "list/genus"));

					tmp.add(new ForwardingDalOperation("genotype", "get/genotype/_id"));
					tmp.add(new ForwardingDalOperation("genotype", "list/genotype/_nperpage/page/_num"));
					
					tmp.add(new ForwardingDalOperation("genotypealias", "get/genotypealias/_id"));
					tmp.add(new ForwardingDalOperation("genotypealias", "list/genotypealias/_nperpage/page/_num"));
					tmp.add(new ForwardingDalOperation("genotypealias", "genotype/_genoid/list/alias"));
					
//					tmp.add(new ListGroupVariantDalOperation(DalOperation.LIST_GROUP));
//					tmp.add(new ListGroupVariantDalOperation(DalOperation.LIST_ALL_GROUP));

					operations = tmp;
				}
			}
		}
		return operations;
	}

	@Override
	public Collection<String> getEntityNames() {
		return Collections.unmodifiableCollection(entityClassByName.keySet());
	}

	@Override
	public SystemGroupInfo getSystemGroupInfo(DalSession session) throws DalDbException {
		DALClient client = clientBySessionId.get(session.sessionId);
		if (client == null) {
			throw new AuthenticationException("Not logged in");
		}
		
		return new SystemGroupInfoImpl(client.getGroupId(), client.getGroupName(), client.isInAdminGroup());
	}
	
	@Override
	public void performListAllGroup(DalSession session, DalResponseBuilder builder,
			String[] returnSql) throws DalDbException
	{
		DALClient client = clientBySessionId.get(session.sessionId);
		if (client == null) {
			throw new AuthenticationException("Not logged in");
		}
		
		try {
			DalResponse response = client.performQuery(DalOperation.LIST_ALL_GROUP);
			feedResponseToResponseBuilder(response, builder);
		} catch (DalResponseException e) {
			throw new DalDbException(e);
		} catch (IOException e) {
			throw new DalDbException(e);
		}
	}

	@Override
	public void performListGroup(DalSession session, DalResponseBuilder builder, String[] returnSql)
	throws DalDbException 
	{
		DALClient client = clientBySessionId.get(session.sessionId);
		if (client == null) {
			throw new AuthenticationException("Not logged in");
		}
		
		try {
			DalResponse response = client.performQuery(DalOperation.LIST_GROUP);
			feedResponseToResponseBuilder(response, builder);
		} catch (DalResponseException e) {
			throw new DalDbException(e);
		} catch (IOException e) {
			throw new DalDbException(e);
		}
	}
	

	@Override
	public void performListField(DalSession session, String tableName, DalResponseBuilder responseBuilder) throws DalDbException {
		
		DALClient client = clientBySessionId.get(session.sessionId);
		if (client == null) {
			throw new AuthenticationException("Not logged in");
		}
		
		try {
			DalResponse response = client.performQuery(tableName + DalOperation.LIST_FIELD_TAIL);
			feedResponseToResponseBuilder(response, responseBuilder);
		} catch (DalResponseException e) {
			throw new DalDbException(e);
		} catch (IOException e) {
			throw new DalDbException(e);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isAutoSwitchGroupOnLogin() {
		return autoSwitchGroupOnLogin;
	}

	/**
	 * Provided to assist in testing
	 * @param autoSwitchGroupOnLogin
	 */
	public void setAutoSwitchGroupOnLogin(boolean b) {
		this.autoSwitchGroupOnLogin = b;
	}


	@Override
	public UserInfo doLogin(String newSessionId, String userName, SessionExpiryOption seo, Map<String, String> parms) throws AuthenticationException {
		
		String errmsg = DalDatabaseUtil.getUsernamePasswordErrorMessage(userName, dalPassword, parms);
		if (errmsg != null) {
			throw new AuthenticationException(errmsg + " (0)");
		}
		
		try {
			DALClient client = new DefaultDALClient(dalUrl);
			if (USE_JSON) {
				client.setResponseType(ResponseType.JSON);
			}
			client.setSessionExpiryOption(seo);
			client.setAutoSwitchGroupOnLogin(autoSwitchGroupOnLogin);
			client.login(dalUsername, dalPassword);
			clientBySessionId.put(newSessionId, client);
			return new UserInfoImpl(userName, client.getUserId());
		} catch (DalException e) {
			throw new AuthenticationException(e);
		} catch (IOException e) {
			throw new AuthenticationException(e);
		}
	}
	
	@Override
	public void doLogout(DalSession session) {
		DALClient client = clientBySessionId.remove(session.sessionId);
		if (client != null) {
			client.logout();
		}
	}

	@Override
	public boolean isInitialiseRequired() {
		return false;
	}

	@Override
	public void initialise(Closure<String> progress) throws DalDbException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDatabasePath() {
		return dalUrl;
	}
	

	@Override
	protected void finalize() throws Throwable {
		try {
			shutdown();
		}
		finally {
			super.finalize();
		}
	}

	@Override
	public void shutdown() throws DalDbException {
		Collection<DALClient> clients = clientBySessionId.values();
		clientBySessionId.clear();
		for (DALClient client : clients) {
			client.logout();
		}
	}

	@Override
	public Class<? extends DalEntity> getEntityClass(String tname) {
		Class<? extends DalEntity> result = null;
		for (String name : entityClassByName.keySet()) {
			if (name.equalsIgnoreCase(tname)) {
				result = entityClassByName.get(name);
				break;
			}
		}
		return result;
	}

	@Override
	public DbDataLoader getDbDataLoader() throws DalDbException {
		throw new DalDbException("Not yet implemented");
		// TODO implement KddartDalDatabase.getDbDataLoader()
	}
	
	protected void feedResponseToResponseBuilder(DalResponse response, final DalResponseBuilder responseBuilder)
	throws DalResponseFormatException, DalResponseException 
	{
		final Set<String> tags = new LinkedHashSet<String>();
		DalResponseRecordVisitor tagCollector = new DalResponseRecordVisitor() {	
			@Override
			public boolean visitResponseRecord(String tag, DalResponseRecord rr) {
				tags.add(tag);
				return true;
			}
		};
		response.visitResults(tagCollector);
		for (String tag : tags) {
			responseBuilder.addResponseMeta(tag);
		}
		
		/*
		 * The Pagination record isn't usually included in the visited tags because it isn't
		 * included in the RecordMeta list of tags. So we need to find it and process it manually
		 */
		DalResponseRecord paginationRecord = response.getFirstRecord(DALClient.TAG_PAGINATION);
		if (paginationRecord != null && ! paginationRecord.rowdata.isEmpty()) {
			copyResponseRecord(responseBuilder, DALClient.TAG_PAGINATION, paginationRecord);
		}
		
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {	
			@Override
			public boolean visitResponseRecord(String tag, DalResponseRecord rr) {
				copyResponseRecord(responseBuilder, tag, rr);
				return true;
			}
		};
		response.visitResults(visitor, tags);
	}

	private void copyResponseRecord(final DalResponseBuilder responseBuilder, String tag, DalResponseRecord rr) {
		responseBuilder.startTag(tag);
		for (String k : rr.rowdata.keySet()) {
			responseBuilder.attribute(k, rr.rowdata.get(k));
		}
		responseBuilder.endTag();
	}

//	class ListGroupVariantDalOperation extends AbstractDalOperation<KddartDalDatabase> {
//
//		public ListGroupVariantDalOperation(String template) {
//			super(KddartDalDatabase.this, "group", template);
//		}
//
//		@Override
//		public void execute(String userId, 
//				DalResponseBuilder responseBuilder, 
//				Method method,
//				String dalcmd,
//				List<String> dalOpParameters,
//				Map<String, String> methodParms,
//				Map<String,String> filePathByName)
//		throws DalDbException 
//		{
//			if (DalOperation.LIST_ALL_GROUP.equals(getCommandTemplate())) {
//				performListGroup(null, responseBuilder, null);
//			}
//			else {
//				performListGroup(userId, responseBuilder, null);
//			}
//		}
//	}

	class ForwardingDalOperation extends AbstractDalOperation<KddartDalDatabase> {

		public ForwardingDalOperation(String entityName, String template) {
			super(KddartDalDatabase.this, entityName, template);
		}

		@Override
		public void execute(DalSession session,
				final DalResponseBuilder responseBuilder, 
				Method method,
				String dalcmd,
				List<String> dalOpParameters,
				Map<String, String> methodParms,
				Map<String,String> filePathByName) 
		throws DalDbException {
			
			DALClient client = clientBySessionId.get(session.sessionId);

			if (client == null) {
				throw new AuthenticationException("Not logged in");
			}
			
			if (Method.GET == method) {
				try {
					DalResponse response = client.performQuery(dalcmd);

					feedResponseToResponseBuilder(response, responseBuilder);
					
				} catch (DalResponseException e) {
					throw new DalDbException(e);
				} catch (IOException e) {
					throw new DalDbException(e);
				}
			}
			else if (Method.POST == method) {
				throw new DalDbException("Not Yet Implemented: " + method.name() + " for " + dalcmd);
			}
			else {
				throw new DalDbException("Unsupported request method: " + method.name());
			}
		}
		
	}

}
