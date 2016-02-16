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
package com.diversityarrays.dal.db.bms;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pearcan.util.StringUtil;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ClosureUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;

import com.diversityarrays.dal.db.AbstractDalDatabase;
import com.diversityarrays.dal.db.AuthenticationException;
import com.diversityarrays.dal.db.BufferedReaderEntityIterator;
import com.diversityarrays.dal.db.CollectionEntityIterator;
import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.db.EntityIterator;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.db.RecordCountCache;
import com.diversityarrays.dal.db.RecordCountCacheEntry;
import com.diversityarrays.dal.db.RecordCountCacheImpl;
import com.diversityarrays.dal.db.ResultSetEntityIterator;
import com.diversityarrays.dal.db.SystemGroupInfo;
import com.diversityarrays.dal.db.UserInfo;
import com.diversityarrays.dal.entity.Container;
import com.diversityarrays.dal.entity.Crop;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.GenParent;
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.entity.GenoTypeAliasStatus;
import com.diversityarrays.dal.entity.GenoTypeSpecimen;
import com.diversityarrays.dal.entity.Genotype;
import com.diversityarrays.dal.entity.GenotypeAlias;
import com.diversityarrays.dal.entity.GenotypeaAlias;
import com.diversityarrays.dal.entity.Genus;
import com.diversityarrays.dal.entity.Item;
import com.diversityarrays.dal.entity.ItemParent;
import com.diversityarrays.dal.entity.ItemUnit;
import com.diversityarrays.dal.entity.Observation;
import com.diversityarrays.dal.entity.Page;
import com.diversityarrays.dal.entity.Parent;
import com.diversityarrays.dal.entity.Project;
import com.diversityarrays.dal.entity.Sample;
import com.diversityarrays.dal.entity.Season;
import com.diversityarrays.dal.entity.Site;
import com.diversityarrays.dal.entity.Specimen;
import com.diversityarrays.dal.entity.SpecimenGroup;
import com.diversityarrays.dal.entity.State;
import com.diversityarrays.dal.entity.SystemGroup;
import com.diversityarrays.dal.entity.SystemUser;
import com.diversityarrays.dal.entity.TraitGroup;
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.entity.TrialEvent;
import com.diversityarrays.dal.entity.TrialType;
import com.diversityarrays.dal.entity.UnitType;
import com.diversityarrays.dal.entity.Workflow;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.server.DalSession;
import com.diversityarrays.dal.service.DalDbNotYetImplementedException;
import com.diversityarrays.dal.sqldb.JdbcConnectionParameters;
import com.diversityarrays.dal.sqldb.ResultSetVisitor;
import com.diversityarrays.dal.sqldb.SqlUtil;
import com.diversityarrays.dalclient.SessionExpiryOption;
import com.diversityarrays.util.Continue;
import com.diversityarrays.util.Either;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Provides an implementation of DalDatabase that understands the
 * old BMS database schema (GMS).
 * @author brian
 *
 */
public class BMS_DalDatabase extends AbstractDalDatabase {
	
	private static final String DATABASE_VERSION = "0.1";
	
	static class BMS_SystemGroupInfo implements SystemGroupInfo {
		
		private final String groupId;
		private final String groupName;
		private final boolean groupOwner;

		BMS_SystemGroupInfo(String groupId, String groupName, boolean groupOwner) {
			this.groupId = groupId;
			this.groupName = groupName;
			this.groupOwner = groupOwner;
		}

		@Override
		public String getGroupId() {
			return groupId;
		}

		@Override
		public String getGroupName() {
			return groupName;
		}

		@Override
		public boolean isGroupOwner() {
			return groupOwner;
		}
		
	}
	
	static class BMS_UserInfo implements UserInfo {

		private String userName;
		private String userId;
		
		public final int instalid;
		public final int ustatus;
		public final int utype;
		public final int personid;
		public final String adate;
		
		BMS_UserInfo(String nm, int id, 
				int instalid, int ustatus, int uaccess, int utype, int pid, String adate) 
		{
			this.userName = nm;
			this.userId = Integer.toString(id);
			
			this.instalid = instalid;
			this.ustatus = ustatus;
			this.utype = utype;
			this.personid = pid;
			this.adate = adate;
		}

		@Override
		public String getUserName() {
			return userName;
		}

		@Override
		public String getUserId() {
			return userId;
		}
		
	}
	
	// GenotypeAliasType:
	// SELECT fcode, fname, fldno, COUNT(*) FROM UDFLDS
	// WHERE ftable='NAMES' AND ftype='NAME'
	
	// Need to choose one as the primary name
	// Use ordered from the parameter
		

	public static final Closure<String> REPORT_PROGRESS = new Closure<String>() {
		String prefix = BMS_DalDatabase.class.getSimpleName() + ": ";
		@Override
		public void execute(String msg) {
			System.out.println(prefix + msg);
		}
	};
	

	static DalDbException getDalDbException(Either<Throwable,?> either) {
		if (either.isRight()) {
			return new DalDbException("Internal error: getDalDbException() called with no error");
		}
		Throwable t = either.left();
		if (t instanceof DalDbException) {
			return (DalDbException) t;
		}
		return new DalDbException(t);
	}
	
	private final JdbcConnectionParameters localParams;
	private final JdbcConnectionParameters centralParams;
	
	private BmsConnectionInfo bmsConnections;

	private List<DalOperation> operations;
	
	private Map<String,BMS_UserInfo> userInfoBySessionId = new HashMap<String, BMS_UserInfo>();
	
	//Store User Crop
	private List<Crop> cropUserSessionList = new ArrayList<Crop>(); 
	
	private Map<String,Class<? extends DalEntity>> entityClassByName = new HashMap<String,Class<? extends DalEntity>>();

	private EntityProvider<Genus> genusProvider = new EntityProvider<Genus>() {
		
		@Override
		public Genus getEntity(String id, String filterClause) throws DalDbNotYetImplementedException {
			if (filterClause != null) {
				throw new DalDbNotYetImplementedException("Filtering clause for genus");
			}
			// TODO use the genus table
			return bmsConnections.genusStore.getGenusById(id);
		}
		
		@Override
		public EntityIterator<? extends Genus> createIterator(int firstRecord, int nRecords, String filterClause) throws DalDbNotYetImplementedException {
			if (filterClause != null) {
				throw new DalDbNotYetImplementedException("Filtering clause for genus");
			}
			// TODO use the genus table
			return new CollectionEntityIterator<Genus>(bmsConnections.genusStore.getGenusValues());
		}

		@Override
		public int getEntityCount(String filterClause) throws DalDbNotYetImplementedException {
			if (filterClause != null) {
				throw new DalDbNotYetImplementedException("Filtering clause for genus");
			}
			// TODO use the genus table
			return bmsConnections.genusStore.getGenusCount();
		}

		@Override
		public EntityIterator<? extends Genus> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
		throws DalDbException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Genus> createIterator(int firstRecord,
				int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	private EntityProvider<Genotype> genotypeProvider = new EntityProvider<Genotype>() {

		private GenotypeFactory createFactory() {
			return new GenotypeFactory(bmsConnections.genusStore);
		}

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			String sql = createFactory().createCountQuery(filterClause);
			int total = 0;
			for (Connection c : bmsConnections.getConnections()) {
				total += SqlUtil.getSingleInteger(c, sql);
			}
			return total;
		}
		
		@Override
		public Genotype getEntity(String id, String filterClause) throws DalDbException {

			final GenotypeFactory factory = createFactory();

			try {
				final Genotype[] result = new Genotype[1];

				ResultSetVisitor visitor = new ResultSetVisitor() {
					@Override
					public Continue visit(ResultSet rs) {
						try {
							result[0] = factory.createEntity(rs);
						} catch (DalDbException e) {
							return Continue.error(e);
						}
						return Continue.STOP; // only the first
					}
				};
				
				String sql = factory.createGetQuery(id, filterClause);
				
				Connection c = bmsConnections.getConnectionFor(id);
				if (c != null) {
					Continue cont = SqlUtil.performQuery(c, sql, visitor);
					if (cont.isError()) {
						Throwable t = cont.throwable;
						if (t instanceof DalDbException) {
							throw ((DalDbException) t);
						}
						throw new DalDbException(t);
					}
				}

				return result[0];
			}
			finally {
				try { factory.close(); } 
				catch (IOException ignore) { }
			}
		}
		
		@Override
		public EntityIterator<? extends Genotype> createIterator(int firstRecord, int nRecords, String filterClause) throws DalDbException {
			
			String whereClause = GenotypeFactory.buildWhereAndLimit(filterClause, nRecords, firstRecord);
			
			StringBuilder sb = GenotypeFactory.createBaseQuery("g", "a", bmsConnections.getFldnoForGenus(), whereClause);
			
			String sql = sb.toString();
			
			GenotypeFactory factory = createFactory();
			
			try {
				Statement stmt = SqlUtil.createQueryStatement(bmsConnections.centralConnection);
				ResultSet rs = stmt.executeQuery(sql);
				
				
				return new ResultSetEntityIterator<Genotype>(stmt, rs, factory);
			} catch (SQLException e) {
				throw new DalDbException(e);
			} finally {
				try { factory.close(); } 
				catch (IOException ignore) { }
			}
		}

		@Override
		public EntityIterator<? extends Genotype> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
		throws DalDbException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void prepareDetailsSearch() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Genotype> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}		

	};
	
	private EntityProvider<Trial> trialProvider = new EntityProvider<Trial>() {
		
		private CloseableHttpClient client;
		private HttpGet request;
		private HttpPost post;
		private TrialFactory trialFactory;
		private PageFactory pageFactory;
		private UserFactory userFactory;
		private SystemUser systemUser;

		private void createFactory() {
			trialFactory = new TrialFactory();
		}

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			if(trialFactory == null){
				createFactory();
			}
			String sql = trialFactory.createCountQuery(filterClause);
			int total = 0;
			for (Connection c : bmsConnections.getConnections()) {
				total += SqlUtil.getSingleInteger(c, sql);
			}
			return total;
		}
		
		@Override
		public Trial getEntity(String id, String filterClause) throws DalDbException {
			
			if(trialFactory == null){
				createFactory();
			}

			try {
				final Trial[] result = new Trial[1];

				ResultSetVisitor visitor = new ResultSetVisitor() {
					@Override
					public Continue visit(ResultSet rs) {
						try {
							result[0] = trialFactory.createEntity(rs);
						} catch (DalDbException e) {
							return Continue.error(e);
						}
						return Continue.STOP; // only the first
					}
				};
				
				String sql = trialFactory.createGetQuery(id, filterClause);
				
				Connection c = bmsConnections.getConnectionFor(id);
				if (c != null) {
					Continue cont = SqlUtil.performQuery(c, sql, visitor);
					if (cont.isError()) {
						Throwable t = cont.throwable;
						if (t instanceof DalDbException) {
							throw ((DalDbException) t);
						}
						throw new DalDbException(t);
					}
				}

				return result[0];
			}
			finally {
				try {trialFactory.close(); } 
				catch (IOException ignore) { }
			}
		}
		
		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,int firstRecord, int nRecords, String filterClause) throws DalDbException {
			
			//System.out.println("BEGIN createIdIterator (TRIAL) in BMS_DalDatabase class====");
			BufferedReader bufferedReader;
			
			if(trialFactory == null){
				createFactory();
			}
			
			if(userFactory == null){
				createUserFactory();
			}
			
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			request = new HttpGet(trialFactory.createListStudiesDetailsURL(id));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			//System.out.println("END createIdIterator (TRIAL) in BMS_DalDatabase class ====");
			return new BufferedReaderEntityIterator<Trial>(bufferedReader, trialFactory);
		}
		
		public void prepareDetailsSearch() throws DalDbException {
			HttpClientBuilder clients = HttpClientBuilder.create();
			client = clients.build();
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			((Trial)entity).getTrialId();
			BufferedReader bufferedReader;
			
			request = new HttpGet(trialFactory.createListStudiesDetailsURL(String.valueOf(((Trial)entity).getTrialId())));
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
						
			try{
				HttpResponse response = client.execute(request);
				trialFactory.processDetails(entity, new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			BufferedReader bufferedReader;
			request = new HttpGet(trialFactory.createListStudiesDetailsURL(String.valueOf(((Trial)entity).getTrialId())));
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());			
						
			try{
				HttpResponse response = client.execute(request);
				trialFactory.processTrial(entity, new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
		}
		
		@Override
		public EntityIterator<? extends Trial> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			BufferedReader bufferedReader;
			
			if(trialFactory == null){
				createFactory();
			}
					
			client = HttpClientBuilder.create().build();
			request = new HttpGet(trialFactory.createListStudiesURL(filterClause));
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<Trial>(bufferedReader, trialFactory);
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}
		
		private void createPageFactory() {
			pageFactory = new PageFactory();
		}
		
		private void createUserFactory() {
			userFactory = new UserFactory();
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			BufferedReader bufferedReader;
			Page result = null;
			
			int total = 0;
			
			if(pageFactory == null){
				createPageFactory();
			}
			
			if(trialFactory == null){
				createFactory();
			}
			
			if(userFactory == null){
				createUserFactory();
			}
			
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
				
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			request = new HttpGet(trialFactory.createCountQuery(filterClause));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER,systemUser.getPasswordSalt());
						
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				BufferedReaderEntityIterator<Page> entityIterator =  new BufferedReaderEntityIterator<Page>(bufferedReader, pageFactory);
				entityIterator.readLine();
				result = entityIterator.nextEntity();
				result.setTotalResults(entityIterator.getRecordCount());
				result.setPageSize(BMSApiDataConnection.BMS_MAX_PAGE_SIZE);
				result.setFirstPage(true);
				result.setHasNextPage(false);
				result.setHasPreviousPage(false);
				result.setLastPage(false);
				result.setPageNumber(1);
				result.setTotalPages(1);
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
					
			return result;
		}

		@Override
		public EntityIterator<? extends Trial> createIterator(int firstRecord,
				int nRecords, String filterClause, Page page)
				throws DalDbException {
			BufferedReader bufferedReader;
			
			if(trialFactory == null){
				createFactory();
			}
			
			if(userFactory == null){
				createUserFactory();
			}
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
			
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
				
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
					
			
			request = new HttpGet(trialFactory.createListStudiesURL(filterClause));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER,systemUser.getPasswordSalt());
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<Trial>(bufferedReader, trialFactory);
		}
		
		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,int firstRecord, int nRecords, String filterClause, Page page) throws DalDbException {
			
			//System.out.println("BEGIN createIdIterator (TRIAL) in BMS_DalDatabase class====");
			BufferedReader bufferedReader;
			
			if(trialFactory == null){
				createFactory();
			}
			
			if(userFactory == null){
				createUserFactory();
			}
			
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			request = new HttpGet(trialFactory.createListStudiesDetailsURL(id));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			//System.out.println("END createIdIterator (TRIAL) in BMS_DalDatabase class ====");
			return new BufferedReaderEntityIterator<Trial>(bufferedReader, trialFactory);
		}
	};	
	
	private EntityProvider<GeneralType> generalTypeProvider = new EntityProvider<GeneralType>() {
		
		GeneralTypeFactory generalTypeFactory = new GeneralTypeFactory();

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {

			
			final GeneralType[] result = new GeneralType[1];

			ResultSetVisitor visitor = new ResultSetVisitor() {
				@Override
				public Continue visit(ResultSet rs) {
					try {
						result[0] = generalTypeFactory.createEntity(rs);
					} catch (DalDbException e) {
						return Continue.error(e);
					}
					return Continue.STOP;
				}
			};

			String sql = generalTypeFactory.createGetQuery(id, filterClause);

			Continue cont = SqlUtil.performQuery(bmsConnections.centralConnection,
					sql,
					visitor);

			if (cont.isError()) {
				Throwable t = cont.throwable;
				if (t instanceof DalDbException) {
					throw ((DalDbException) t);
				}
				throw new DalDbException(t);
			}

			return result[0];
	
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			BufferedReader bufferedReader;

			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(generalTypeFactory.createListTermsURL(id));
			try{
				HttpResponse response = client.execute(request);
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				//System.out.println(rd.readLine());
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}

			return new  BufferedReaderEntityIterator<GeneralType>(bufferedReader, generalTypeFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}		
	};
	
	private EntityProvider<GenotypeAlias> genotypeAliasProvider = new EntityProvider<GenotypeAlias>() {
		
		GenotypeAliasFactory genotypeAliasFactory = new GenotypeAliasFactory();
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			String sql = genotypeAliasFactory.createCountQuery(filterClause);
			// TODO count across both connections
			return SqlUtil.getSingleInteger(bmsConnections.centralConnection, sql);
		}
		
		@Override
		public GenotypeAlias getEntity(String id, String filterClause) throws DalDbException {
		
			final GenotypeAlias[] result = new GenotypeAlias[1];

			ResultSetVisitor visitor = new ResultSetVisitor() {
				@Override
				public Continue visit(ResultSet rs) {
					try {
						result[0] = genotypeAliasFactory.createEntity(rs);
					} catch (DalDbException e) {
						return Continue.error(e);
					}
					return Continue.STOP; // only the first
				}
			};

			String sql = genotypeAliasFactory.createGetQuery(id, filterClause);

			// TODO query across both? but what about the JOIN?
			Continue cont = SqlUtil.performQuery(bmsConnections.centralConnection,
					sql,
					visitor);

			if (cont.isError()) {
				Throwable t = cont.throwable;
				if (t instanceof DalDbException) {
					throw ((DalDbException) t);
				}
				throw new DalDbException(t);
			}

			return result[0];
		}
		
		@Override
		public EntityIterator<? extends GenotypeAlias> createIterator(
				int firstRecord, int nRecords, String filterClause)
		throws DalDbException {

			String sql = genotypeAliasFactory.createPagedListQuery(firstRecord, nRecords, filterClause);
			
			try {
				// TODO query across both? but what about the JOIN?
				Statement stmt = SqlUtil.createQueryStatement(bmsConnections.centralConnection);
				ResultSet rs = stmt.executeQuery(sql);
				
				return new ResultSetEntityIterator<GenotypeAlias>(stmt, rs, genotypeAliasFactory);
			} catch (SQLException e) {
				throw new DalDbException(e);
			}
		}

		@Override
		public EntityIterator<? extends GenotypeAlias> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
		throws DalDbException {
			
			String sql = genotypeAliasFactory.createListAliasQuery(id, firstRecord, nRecords, filterClause);
			
			try {
				// TODO query across both? but what about the JOIN?
				Statement stmt = SqlUtil.createQueryStatement(bmsConnections.centralConnection);
				ResultSet rs = stmt.executeQuery(sql);
				
				return new ResultSetEntityIterator<GenotypeAlias>(stmt, rs, genotypeAliasFactory);
			} catch (SQLException e) {
				throw new DalDbException(e);
			}
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			throw new DalDbException(new UnsupportedOperationException());
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GenotypeAlias> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}		
	};
	
	private EntityProvider<ItemUnit> itemUnitProvider = new EntityProvider<ItemUnit>() { 
		
		ItemUnitFactory itemUnitFactory = new ItemUnitFactory();

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ItemUnit getEntity(String id, String filterClause)
				throws DalDbException {

			
			final ItemUnit[] result = new ItemUnit[1];

			ResultSetVisitor visitor = new ResultSetVisitor() {
				@Override
				public Continue visit(ResultSet rs) {
					try {
						result[0] = itemUnitFactory.createEntity(rs);
					} catch (DalDbException e) {
						return Continue.error(e);
					}
					return Continue.STOP;
				}
			};

			String sql = itemUnitFactory.createGetQuery(id, filterClause);

			Continue cont = SqlUtil.performQuery(bmsConnections.centralConnection,
					sql,
					visitor);

			if (cont.isError()) {
				Throwable t = cont.throwable;
				if (t instanceof DalDbException) {
					throw ((DalDbException) t);
				}
				throw new DalDbException(t);
			}

			return result[0];
	
		}

		@Override
		public EntityIterator<? extends ItemUnit> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			BufferedReader rd;

			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(itemUnitFactory.createListTermsURL(id));
			try{
				HttpResponse response = client.execute(request);
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				//System.out.println(rd.readLine());
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}

			return new BufferedReaderEntityIterator<ItemUnit>(rd, itemUnitFactory);
		}

		@Override
		public EntityIterator<? extends ItemUnit> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends ItemUnit> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}		
	};	

	public BMS_DalDatabase(Closure<String> progress, boolean initialise, JdbcConnectionParameters localParams, JdbcConnectionParameters centralParams) throws DalDbException {
		super("BMS-Interop[Central=" + centralParams + " Local=" + localParams + "]");
		
		this.localParams = localParams;
		this.centralParams = centralParams;
		
		if (localParams != null) {
			String local   = StringUtil.substringBefore(localParams.connectionUrl, "?");
			String central = StringUtil.substringBefore(centralParams.connectionUrl, "?");
			if (local.equals(central)) {
				throw new DalDbException("Local and Central connectionUrls may NOT point at the same database:: " + local);
			}
		}

		
		entityClassByName.put("genus", Genus.class);
		entityClassByName.put("genotype", Genotype.class);
		
		if (initialise) {
			getBmsConnections(progress, true);
		}
	}

    
	private EntityProvider<Observation> observationProvider = new EntityProvider<Observation>() {
		
		private CloseableHttpClient client;
		private ObservationFactory  observationFactory;
		private HttpPut             httpPut;
		private HttpEntity          entitySent;
		private HttpResponse        response;

		private void createFactory() {
			observationFactory = new ObservationFactory();
		}		

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Observation getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Observation> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Observation> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			
			if(observationFactory == null){
				createFactory();
			}
			
			client         = HttpClientBuilder.create().build();
			httpPut        = new HttpPut(observationFactory.getURL(dalOpParameters));
			String StrjSON = observationFactory.getJsonMapped(filePathByName);
			
			//System.out.println("StrjSON => " + StrjSON);
			
			entitySent  = new StringEntity(StrjSON,ContentType.create("application/json"));
	    	httpPut.setEntity(entitySent);		
	    	
	    	try{
		    	response = client.execute(httpPut);
		    	System.out.println(response.getStatusLine());	    		
	    	}catch(Exception e){
	    		System.out.println("Somethinh is wrong: " + e.toString());
	    	}
	    	
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Observation> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/**
	 * Modified by Raul Hernandez
	 * @date 1/25/2016
	 */
	private EntityProvider<Project> projectProvider = new EntityProvider<Project>() {
		
		private ProjectFactory      projectFactory;
		private PageFactory         pageFactory;
		private CloseableHttpClient client;
		private HttpGet             request;
		private SystemUser          systemUser;
		private UserFactory         userFactory;
		private HttpPost            post;
		
		private void createFactory() {
			projectFactory = new ProjectFactory();
			pageFactory    = new PageFactory();
		}
		
		private void createUserFactory() {
			userFactory = new UserFactory();
		}		

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Project getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Project> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(projectFactory == null || pageFactory == null){
				createFactory();
			}
			
			if(userFactory == null){
			   createUserFactory();
			}
					
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}			
			
			request =  new HttpGet(projectFactory.createPagedListQuery(firstRecord, nRecords, id));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
			
			
			try{
				HttpResponse response = client.execute(request);
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<Project>(bufferedReader, projectFactory);

		}

		@Override
		public EntityIterator<? extends Project> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
             return null; 			
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			
			//Simulate the pagination
			BufferedReader bufferedReader;
			Page           result;
			
			if(pageFactory==null || projectFactory == null){
				createFactory();
			}

			client = HttpClientBuilder.create().build();
			
			try{
				HttpResponseFactory factory = new DefaultHttpResponseFactory();
				HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
				response.setEntity(new StringEntity("{\"pageResults\": [{\"germplasmId\": \"2\",\"pedigreeString\": \"C\",\"names\": [\"R\",\"C\"],\"breedingMethod\": \"S\",\"location\": \"C\",\"parent1Id\": \"2\",\"parent1Url\": \"a\",\"parent2Id\": \"1\",\"parent2Url\": \"b\"}],\"pageNumber\": 1,\"pageSize\": 1000,\"totalResults\": 10000,\"hasPreviousPage\": false,\"firstPage\": true,\"hasNextPage\": true,\"lastPage\": false,\"totalPages\": 10}"));
				response.addHeader("Content-type", "application/json");
				
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				BufferedReaderEntityIterator<Page> entityIterator =  new BufferedReaderEntityIterator<Page>(bufferedReader, pageFactory);
				entityIterator.readLine();
				result = entityIterator.nextEntity();
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			
			return result;

		}

		@Override
		public EntityIterator<? extends Project> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page page) throws DalDbException {
			// TODO Auto-generated method stub
			BufferedReader bufferedReader;
			
			if(projectFactory==null || pageFactory==null){
				createFactory();
			}
			
			if(userFactory == null){
				   createUserFactory();
			}			

			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			
			
			
			client  = HttpClientBuilder.create().build();
			request = new HttpGet(projectFactory.createPagedListQuery(firstRecord, nRecords, filterClause));
			request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
			
			try{
				HttpResponse response = client.execute(request);
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}			
			
			return new BufferedReaderEntityIterator<Project>(bufferedReader, projectFactory, page);			

		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	@Override
	public boolean isInitialiseRequired() {
		return true;
	}

	@Override
	public void initialise(Closure<String> progress)
	throws DalDbException {
		getBmsConnections(progress, true);
	}
	
	private BmsConnectionInfo getBmsConnections(Closure<String> progress, boolean createIfNotPresent) throws DalDbException {
		if (bmsConnections == null && createIfNotPresent) {
			bmsConnections = new BmsConnectionInfo(localParams, centralParams, progress);
		}
		return bmsConnections;
	}

	@Override
	public String getDatabaseVersion(DalSession session) {
		return DATABASE_VERSION;
	}

	@Override
	public List<DalOperation> getOperations() {
		if (operations==null) {
			synchronized (this) {
				if (operations == null) {
					List<DalOperation> tmp = new ArrayList<DalOperation>();
					
					tmp.add(createOperation("get/genus/_id", Genus.class, genusProvider));
					tmp.add(createOperation("list/genus", Genus.class, genusProvider));
					
					tmp.add(createOperation("get/genotype/_id", Genotype.class, genotypeProvider));
					tmp.add(createOperation("list/genotype/_nperpage/page/_num", Genotype.class, genotypeProvider));
					
					tmp.add(createOperation("get/genotypealias/_id", GenotypeAlias.class, genotypeAliasProvider));
					tmp.add(createOperation("list/genotypealias/_nperpage/page/_num", GenotypeAlias.class, genotypeAliasProvider));

//					tmp.add(createOperation("genus/_genusid/list/genotype", Genotype.class, genusGenotypeProvider));
//					tmp.add(createOperation("genus/_genusid/list/genotype/_nperpage/page/_num", Genotype.class, genusGenotypeProvider));
					
					tmp.add(createOperation("genotype/_genoid/list/alias", GenotypeAlias.class, genotypeAliasProvider));
					//GeneralType operations
					tmp.add(createOperation("generaltype/_typeid", GeneralType.class, generalTypeProvider));
					
					//ItemUnit operations
					tmp.add(createOperation("itemunit/_itemunitid", ItemUnit.class, itemUnitProvider));
					
					//Trial operations
					tmp.add(createOperation("list/trial/_nperpage/page/_num", Trial.class, trialProvider));
					tmp.add(createOperation("trial/details/_program", Trial.class, trialProvider));
					
					//SetOperations
					tmp.add(createOperation("observation/_program/_trialid/_observationid", Observation.class, observationProvider));
					
					//Project Operations
					tmp.add(createOperation("list/project/_nperpage/page/_num", Project.class,projectProvider));

					//GetLocationsOperations
					tmp.add(createOperation("list/site/_nperpage/page/_num", Site.class, siteProvider));
					
					
					//Trial Type
					tmp.add(createOperation("list/type/trial/active", TrialType.class, trialTypeProvider));
					
					//Sample
					tmp.add(createOperation("list/type/sample/active", Sample.class, sampleProvider));
					
					//item
					tmp.add(createOperation("list/type/item/active", Item.class, itemProvider));
					
					//itemparent
					tmp.add(createOperation("list/type/itemparent/active", ItemParent.class, itemparentProvider));
					
					//unittype
					tmp.add(createOperation("list/type/unittype/active", UnitType.class, unittypeProvider));
					
					//state
					tmp.add(createOperation("list/type/state/active", State.class, stateProvider));
					
					//container
					tmp.add(createOperation("list/type/container/active", Container.class, containerProvider));
					
					//specimengroup
					tmp.add(createOperation("list/type/specimengroup/active", SpecimenGroup.class, specimengroupProvider));
					
					//parent
					tmp.add(createOperation("list/type/parent/active", Parent.class, parentProvider));
					
					//genotypealias
					tmp.add(createOperation("list/type/genotypealias/active", GenotypeaAlias.class, genotypealiasProvider));
					
					//genotypealiasstatus
					tmp.add(createOperation("list/type/genotypealiasstatus/active", GenoTypeAliasStatus.class, genotypealiasstatusProvider));
					
					//genparent
					tmp.add(createOperation("list/type/genparent/active", GenParent.class, genparentProvider));
					
					//genotypespecimen
					tmp.add(createOperation("list/type/genotypespecimen/active", GenoTypeSpecimen.class, genotypespecimenProvider));
					
					//trialevent
					tmp.add(createOperation("list/type/trialevent/active", TrialEvent.class, trialeventProvider));
					
					//workflow
					tmp.add(createOperation("list/type/workflow/active", Workflow.class, workflowProvider));
					
					//trialgroup
					tmp.add(createOperation("list/type/traitgroup/active", TraitGroup.class, traitgroupProvider));
					
					/*
					 * Call requested by Brian in November 19th
					 */
					tmp.add(createOperation("list/specimen/_nperpage/page/_num", Specimen.class, specimenProvider));
					
					//Season
					tmp.add(createOperation("list/factor", Season.class, seasonProvider));
					
					//Crop catalog
					tmp.add(createOperation("list/group", Crop.class, cropProvider));
					
					//Set crop
					tmp.add(createOperation("set/group/_id",Crop.class,setCropProvider));
					                           
					operations = tmp;
				}
			}
		}
		return operations;
	}


	private Map<Pattern, MatcherToOperation> factoryByPattern;
	
	private Collection<String> entityNames;

	@SuppressWarnings("unchecked")
	private Closure<String> defaultProgress = ClosureUtils.nopClosure();
	
	static interface MatcherToOperation {
		public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider);
	}
	
	
	protected DalOperation createOperation(String template, 
			Class<? extends DalEntity> entityClass, 
			EntityProvider<? extends DalEntity> provider) 
	{
		if (factoryByPattern==null) {
			factoryByPattern = createFactoryByPattern();
		}
		
		DalOperation result = null;

		for (Pattern p : factoryByPattern.keySet()) {
			Matcher m = p.matcher(template);
			if (m.matches()) {
				result = factoryByPattern.get(p).makeOperation(m, entityClass, provider);
				break;
			}
		}
		
		if (result == null) {
			throw new IllegalArgumentException("Unsupported operation template: '" + template + "'");
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void setDefaultProgress(Closure<String> p) {
		this.defaultProgress = p!=null ? p : ClosureUtils.nopClosure();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<Pattern, MatcherToOperation> createFactoryByPattern() {
		
		Map<Pattern,MatcherToOperation> map = new HashMap<Pattern,MatcherToOperation>();
		
		map.put(GetOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				String entity = GetOperation.getEntityName(m);
				return new GetOperation(BMS_DalDatabase.this, entity, "get/" + entity + "/_id", entityClass, provider);
			}
		});
		
		map.put(SimpleListOperation.PATTERN,  new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				String entity = SimpleListOperation.getEntityName(m);
				return new SimpleListOperation(BMS_DalDatabase.this, entity, entityClass, provider);
			}	
		});
		
		map.put(PagedListOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				String entity = PagedListOperation.getEntityName(m);
				return new PagedListOperation(BMS_DalDatabase.this, entity, entityClass, provider);
			}
		});
		
		map.put(GenotypeListAliasOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GenotypeListAliasOperation(BMS_DalDatabase.this, (EntityProvider<GenotypeAlias>) provider);
			}
		});
		
		map.put(GeneralTypeGetOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GeneralTypeGetOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});		
		
//		map.put(GenotypeListSpecimenOperation.PATTERN, new MatcherToOperation() {
//			@Override
//			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
//				return new GenotypeListSpecimenOperation(BMS_DalDatabase.this, (EntityProvider<Specimen>) provider);
//			}
//		});
		
		map.put(ItemUnitGetOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new ItemUnitGetOperation(BMS_DalDatabase.this, (EntityProvider<ItemUnit>) provider);
			}
		});
		
		map.put(GetTrialOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetTrialOperation(BMS_DalDatabase.this, (EntityProvider<Trial>) provider);
			}
		});
		
		map.put(GetTrialsOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetTrialsOperation(BMS_DalDatabase.this, (EntityProvider<Trial>) provider);
			}
		});
		
		map.put(SetObservationOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new SetObservationOperation(BMS_DalDatabase.this, (EntityProvider<Observation>) provider);
			}
		});
		
		map.put(GetProjectOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetProjectOperation(BMS_DalDatabase.this, (EntityProvider<Project>) provider); 
			}
		});
		
		map.put(GetLocationsOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetLocationsOperation(BMS_DalDatabase.this, (EntityProvider<Site>) provider);
			}
		});
		
		//TrialType
		map.put(GetTrialTypeOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetTrialTypeOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});		
		
		//Sample		
		map.put(GetSampleOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetSampleOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//Item
		map.put(GetItemOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetItemOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//ItemParent
		map.put(GetItemParentOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetItemParentOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//UnitType
		map.put(GetUnitTypeOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetUnitTypeOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//State
		map.put(GetStateOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetStateOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//Container
		map.put(GetContainerOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetContainerOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//SpecimenGroup
		map.put(GetSpecimenGroupOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetSpecimenGroupOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//Parent
		map.put(GetParentOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetParentOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//GenotypeAlias
		map.put(GetGenoTypeAliasOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetGenoTypeAliasOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//GenotypeAliasStatus
		map.put(GetGenoTypeAliasStatusOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetGenoTypeAliasStatusOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//GenParent
		map.put(GetGenParentOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetGenParentOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//GenotypeSpecimen
		map.put(GetGenotypeSpecimenOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetGenotypeSpecimenOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//TrialEvent
		map.put(GetTrialEventOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetTrialEventOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//Workflow
		map.put(GetWorkflowOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetWorkflowOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});
		
		//TraitGroup
		map.put(GetTraitGroupOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetTraitGroupOperation(BMS_DalDatabase.this, (EntityProvider<GeneralType>) provider);
			}
		});		
		
		//Specimen
		map.put(GetSpecimenOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetSpecimenOperation(BMS_DalDatabase.this, (EntityProvider<Specimen>) provider);
			}
		});		
		
		//Season
		map.put(GetSeasonOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetSeasonOperation(BMS_DalDatabase.this, (EntityProvider<Season>) provider);
			}
		});	
		
		//Get Crop
		map.put(GetCropOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new GetCropOperation(BMS_DalDatabase.this, (EntityProvider<SystemGroup>) provider);
			}
		});
		
		//Set Crop
		map.put(SetCropOperation.PATTERN, new MatcherToOperation() {
			@Override
			public DalOperation makeOperation(Matcher m, Class<? extends DalEntity> entityClass, EntityProvider<? extends DalEntity> provider) {
				return new SetCropOperation(BMS_DalDatabase.this, (EntityProvider<Crop>) provider);
			}
		});		
		
		return map;
	}

	@Override
	public Collection<String> getEntityNames() {
		if  (entityNames == null) {
			Set<String> set = new HashSet<String>();
			for (DalOperation op : getOperations()) {
				set.add(op.getEntityName());
			}
			entityNames = set;
		}
		return entityNames;
	}

	@Override
	public SystemGroupInfo getSystemGroupInfo(DalSession session) throws DalDbException {
		
		BMS_UserInfo userInfo = userInfoBySessionId.get(session.sessionId);
		
		if (userInfo == null) {
			throw new DalDbException("Not logged in");
		}

		UdfldsRecord udfldsRecord = bmsConnections.userTypesByFldno.get(userInfo.utype);
		if (udfldsRecord == null) {
			throw new DalDbException("Missing UDFLDS record for utype=" + userInfo.utype);
		}
		
		boolean owner = udfldsRecord.fname.contains("ADMINISTRATOR");
		
		BMS_SystemGroupInfo result = new BMS_SystemGroupInfo(session.getGroupId(), udfldsRecord.fname, owner);
		
		return result;
	}
	
	@Override
	public void performListAllGroup(DalSession session, DalResponseBuilder builder,
			String[] returnSql) throws DalDbException
	{
		BMS_UserInfo userInfo = userInfoBySessionId.get(session.sessionId);
		if (userInfo == null) {
			throw new DalDbException("Not logged in");
		}

		builder.addResponseMeta("SystemGroup");

		for (UdfldsRecord r : bmsConnections.userTypesByFldno.values()) {
			builder.startTag("SystemGroup")
				.attribute("SystemGroupId", Integer.toString(r.fldno))
				.attribute("SystemGroupName", r.fcode)
				.attribute("SystemGroupDescription", r.fname)
			.endTag();
		}
	}

	@Override
	public void performListGroup(DalSession session, DalResponseBuilder builder, String[] returnSql) 
	throws DalDbException 
	{
		BMS_UserInfo userInfo = userInfoBySessionId.get(session.sessionId);
		if (userInfo == null) {
			throw new DalDbException("Not logged in");
		}

		builder.addResponseMeta("SystemGroup");

		UdfldsRecord r = bmsConnections.userTypesByFldno.get(userInfo.utype);
		if (r == null) {
			System.err.println("WARNING: Missing UDFLDS record for utype=" + userInfo.utype);

			builder.startTag("SystemGroup")
			.attribute("SystemGroupId", "0")
			.attribute("SystemGroupName", "Unknown-" + userInfo.utype)
			.attribute("SystemGroupDescription", "Missing UDFLDS record for utype=" + userInfo.utype)
			.endTag();
		}
		else {
			builder.startTag("SystemGroup")
			.attribute("SystemGroupId", Integer.toString(r.fldno))
			.attribute("SystemGroupName", r.fcode)
			.attribute("SystemGroupDescription", r.fname)
			.endTag();
			
			//Set the crops in memory
			getCropUser();
			
			//adding to the xml
 		    Iterator<Crop> it = cropUserSessionList.iterator();
			   
			while(it.hasNext()){
	  			  Crop c = it.next();
	  			  builder.startTag("SystemGroup")
				  .attribute("SystemGroupId", c.getSystemGroupId())
				  .attribute("SystemGroupName", c.getSystemGroupName())
				  .attribute("SystemGroupDescription", c.getSystemGroupDescription())
				  .endTag();
			} 
		}
	}
	
	
	public void getCropUser() throws DalDbException{
		
		    SystemUser          systemUser      = null;
		    UserFactory         userFactory     = new UserFactory();
		    CropFactory         cropFactory     = new CropFactory();
		    CloseableHttpClient client          = HttpClientBuilder.create().build();
		    HttpPost            post;
		    BufferedReader      bufferedReader;
		    HttpGet             request;
		    
			/**
			 * BMS API Login
			 */
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}
			/**
			 * End BMS API Login
			 */
			
			try{
				
				request = new HttpGet(cropFactory.getURL());
				request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
  		        HttpResponse response = client.execute(request);
  		        bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
  		        
  		        String value = bufferedReader.readLine();
  		        StringTokenizer strTokenizer = new StringTokenizer(value.substring(1, value.length()-1),",");
  		        
  		        Crop cropObj = null;
  		        List<Crop> objArrayCrop = new ArrayList<Crop>();
  		        int cont=1;
  		        
  		        while(strTokenizer.hasMoreTokens()){
  		        	  cropObj =new Crop();
  		          	  cropObj.setSystemGroupId(String.valueOf(cont++));
  		          	  cropObj.setSystemGroupName(strTokenizer.nextToken().replaceAll("\"", ""));
  		          	  cropObj.setSystemGroupDescription("");
  		        	  objArrayCrop.add(cropObj);
  		        }
  		        
  		        //Set in memory all the crops from BMS API  
  		        cropUserSessionList.addAll(objArrayCrop);
  		        
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}		
	}
	
	@Override
	public UserInfo doLogin(String newSessionId, final String userName, SessionExpiryOption seo,
			final Map<String, String> parms) throws AuthenticationException 
	{
		String sql = "SELECT userid, upswd, instalid, ustatus, uaccess, utype, personid, adate FROM users WHERE uname = '" + DbUtil.doubleUpSingleQuote(userName.toUpperCase()) + "'";

		final BMS_UserInfo[] result = new BMS_UserInfo[1];
		ResultSetVisitor visitor = new ResultSetVisitor() {
			@Override
			public Continue visit(ResultSet rs) {
				try {
					int userid = rs.getInt(1);
					String pswd = rs.getString(2);
					
					
					int instalid = rs.getInt(3);
					int ustatus = rs.getInt(4);
					int uaccess = rs.getInt(5);
					int utype = rs.getInt(6);
					int pid = rs.getInt(7);
					String adate = rs.getString(8);
					
					String errmsg = DalDatabaseUtil.getUsernamePasswordErrorMessage(userName, pswd, parms);
					
					if (errmsg != null) {
						return Continue.error(new AuthenticationException(errmsg));
					}
					
					result[0] = new BMS_UserInfo(userName, userid,
							instalid, ustatus, uaccess, utype, pid, adate);
					
					return Continue.STOP; // Only want the first match?
				} catch (SQLException e) {
					return Continue.error(e);
				}
			}
		};

		Continue qResult = null;

		try {
			qResult= SqlUtil.performQuery(getBmsConnections(defaultProgress, true).centralConnection, sql, visitor);
		} catch (DalDbException e) {
			throw new AuthenticationException(e);
		}
		
		if (qResult.isError()) {
			Throwable t = qResult.throwable;
			if (t instanceof AuthenticationException) {
				throw (AuthenticationException) t;
			}
			throw new AuthenticationException("Internal error", qResult.throwable);
		}
		
		BMS_UserInfo ui = result[0];
		
		if (ui == null) {
			throw new AuthenticationException("Invalid username or password");
		}
		
		userInfoBySessionId.put(newSessionId, ui);
		
		//reset user crop list
		cropUserSessionList = new ArrayList<Crop>();
		
		return ui;	
	}

	@Override
	public String getDatabasePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(centralParams)
			.append("$$")
			.append(localParams);
		return sb.toString();
	}

	@Override
	public void shutdown() throws DalDbException {
		if (bmsConnections != null) {
			try {
				bmsConnections.closeConnections();
			} finally {
				bmsConnections = null;
			}
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
	public void performListField(DalSession session, String tableName,
			DalResponseBuilder responseBuilder) 
	throws DalDbException {
		
		Class<? extends DalEntity> entityClass = getEntityClass(tableName);
		if (entityClass == null) {
			throw new DalDbException("Unknown tableName: '" + tableName + "'");
		}
		
		DalDatabaseUtil.addEntityFields(entityClass, responseBuilder);
	}
	
	@Override
	public void doLogout(DalSession session) {
		recordCountCache.removeEntriesFor(session);
		BMS_UserInfo ui = userInfoBySessionId.remove(session.sessionId);
		if (ui != null) {
			// Do something else maybe ?
		}
	}

	private final RecordCountCache recordCountCache = new RecordCountCacheImpl();

	public RecordCountCacheEntry getRecordCountCacheEntry(DalSession session, Class<?> entityClass) {
		return recordCountCache.getEntry(session, entityClass);
	}

	public void setRecordCountCacheEntry(DalSession session, Class<?> entityClass, String filterClause, int count) {
		recordCountCache.setEntry(session, entityClass, filterClause, count);
	}
	
	private EntityProvider<Crop> setCropProvider = new EntityProvider<Crop>(){
		
		private CloseableHttpClient client;
		private HttpPut             httpPut;
		private HttpEntity          entitySent;
		private HttpResponse        response;
		private CropFactory         cropFactory;
		
		private void createFactory(){
			cropFactory = new CropFactory();
		}
		

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Crop getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Crop> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Crop> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Crop> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			
			if(cropFactory == null){
				createFactory();
			}
			
			//Get parameter and set the crop in memory
			List<String> dalparam = dalOpParameters;
			String cropId = "";
			
			
			if(dalparam.size() > 0){
			   cropId = dalparam.get(0);
			   //Keep in memory only the crop that was selected
			   Iterator<Crop> it = cropUserSessionList.iterator();
			   
			   while(it.hasNext()){
		  			  Crop c = it.next();
					  if(!c.getSystemGroupId().equals(cropId)){
						 it.remove();		
					  }
				}			   
			   
			}
		}

		@Override
		public EntityIterator<? extends DalEntity> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	
	private EntityProvider<SystemGroup> cropProvider = new EntityProvider<SystemGroup>(){ 
		
		private CropFactory         cropFactory;
		private CloseableHttpClient client;
		private HttpGet             request;
		private SystemUser          systemUser;
		private UserFactory         userFactory;
		private HttpPost            post;
		
        private void createFactory(){
        	cropFactory = new CropFactory();
        }
        
    	private void createUserFactory() {
			userFactory = new UserFactory();
		}        
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SystemGroup getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends SystemGroup> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

           return null;			
			
		}

		@Override
		public EntityIterator<? extends SystemGroup> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			BufferedReader bufferedReader;
			
			if(cropFactory == null){
			   createFactory();
			}
			
			if(userFactory == null){
			   createUserFactory();
		    }
			
			client = HttpClientBuilder.create().build();
			
			if(systemUser == null){
				post = new HttpPost(userFactory.createLoginQuery());
				
				StringEntity httpEntity = null;
				try{
					httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
					httpEntity.setContentType("application/x-www-form-urlencoded");
				}catch(UnsupportedEncodingException uee){
					System.out.println("Exception when setting login parameters" + uee);
					throw new DalDbException(uee);
				}catch(Exception e){
					System.out.println("Exception when setting login parameters" + e);
					throw new DalDbException(e);
				}
				
				if(httpEntity!=null){
					post.setEntity(httpEntity);
				}
						
				try{
					HttpResponse loginResponse = client.execute(post);
					bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
					BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
					entityIterator.readLine();
					systemUser = entityIterator.nextEntity();				
				}catch(ClientProtocolException cpex){
					throw new DalDbException("Protocol error: " + cpex);
				}catch(IOException ioex){
					throw new DalDbException("Input/Output error when executing request: " + ioex);
				}catch(Exception ex){
					throw new DalDbException("Exception: " + ex);
				}
			}			
			
			try{
				
				request = new HttpGet(cropFactory.getURL());
				request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());
  		        HttpResponse response = client.execute(request);
  		        bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
  		        
  		        String value = bufferedReader.readLine();
  		        StringTokenizer strTokenizer = new StringTokenizer(value.substring(1, value.length()-1),",");
  		        
  		        Crop cropObj = null;
  		        List<Crop> objArrayCrop = new ArrayList<Crop>();
  		        int cont=1;
  		        
  		        while(strTokenizer.hasMoreTokens()){
  		        	  cropObj =new Crop();
  		          	  cropObj.setSystemGroupId(String.valueOf(cont++));
  		          	  cropObj.setSystemGroupName(strTokenizer.nextToken().replaceAll("\"", ""));
  		          	  cropObj.setSystemGroupDescription("");
  		        	  objArrayCrop.add(cropObj);
  		        }
  		        
  		        //Set in memory all the crops from BMS API  
  		        cropUserSessionList.addAll(objArrayCrop);
  		        
  		        Gson g = new Gson();
  		        String result = g.toJson(objArrayCrop);
  		        
  		        HttpResponseFactory factory = new DefaultHttpResponseFactory();
			    response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			    response.setEntity(new StringEntity(result,Consts.UTF_8));
			    response.addHeader("Content-type", "application/json");
			    
			    bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
  		        
  		        

			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<SystemGroup>(bufferedReader, cropFactory);
		}
		

		@Override
		public EntityIterator<? extends SystemGroup> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends DalEntity> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	
	private EntityProvider<Season> seasonProvider = new EntityProvider<Season>(){

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Season getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Season> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Season> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Season> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends DalEntity> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   17-DEC-2015
	 */	
	private EntityProvider<Specimen> specimenProvider = new EntityProvider<Specimen>(){

		private SpecimenFactory     specimenFactory;
		private PageFactory         pageFactory;
		private CloseableHttpClient client;
		private HttpGet             request;
		
		private void createFactories(){
			specimenFactory = new SpecimenFactory();
			pageFactory     = new PageFactory();
		}			
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			
			
			//Simulate
			BufferedReader bufferedReader;
			Page result = null;
			
			if(pageFactory==null || specimenFactory==null){
				createFactories();
			}
			
			client = HttpClientBuilder.create().build();
			
			try{
				HttpResponseFactory factory = new DefaultHttpResponseFactory();
				HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
				response.setEntity(new StringEntity("{\"pageResults\": [{\"germplasmId\": \"2\",\"pedigreeString\": \"C\",\"names\": [\"R\",\"C\"],\"breedingMethod\": \"S\",\"location\": \"C\",\"parent1Id\": \"2\",\"parent1Url\": \"a\",\"parent2Id\": \"1\",\"parent2Url\": \"b\"}],\"pageNumber\": 1,\"pageSize\": 1000,\"totalResults\": 10000,\"hasPreviousPage\": false,\"firstPage\": true,\"hasNextPage\": true,\"lastPage\": false,\"totalPages\": 10}"));
				response.addHeader("Content-type", "application/json");
				
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				BufferedReaderEntityIterator<Page> entityIterator =  new BufferedReaderEntityIterator<Page>(bufferedReader, pageFactory);
				entityIterator.readLine();
				result = entityIterator.nextEntity();
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			
			return result;
		}

		@Override
		public Specimen getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Specimen> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

			BufferedReader bufferedReader;
			
			if(specimenFactory == null || pageFactory == null){
				createFactories();
			}
			
			client  = HttpClientBuilder.create().build();
			
			request = new HttpGet(specimenFactory.createPagedListQuery(firstRecord,nRecords,id));
			
			try{
				
				HttpResponse response = client.execute(request);
				//System.out.println("[createIdIterator URL] " + request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			//("[createIdIterator bufferedReader] " + bufferedReader.toString());
			
			return new BufferedReaderEntityIterator<Specimen>(bufferedReader, specimenFactory);
		}

		@Override
		public EntityIterator<? extends Specimen> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends Specimen> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			
			BufferedReader bufferedReader;
			if(specimenFactory == null || pageFactory == null){
				createFactories();
			}
			
            //Extract the id from filterClause
			//I do not know if this is the best way
			//filterClause = "SpecimenId+IN+(268069,268070)";
			int beginIndex = filterClause.indexOf('(');
			int endIndex   = filterClause.indexOf(')');
			String ids      = filterClause.substring(beginIndex+1, endIndex);
			StringTokenizer stTokenizer = new StringTokenizer(ids,",");
			
			
			try{
				
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("[");
				while(stTokenizer.hasMoreTokens()){
					client = HttpClientBuilder.create().build();
					request = new HttpGet("http://"+BMSApiDataConnection.IP + ":" + BMSApiDataConnection.PORT + "/bmsapi/germplasm/" + BMSApiDataConnection.CROP + "/" + stTokenizer.nextToken());
					HttpResponse response = client.execute(request);
					bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					strBuffer.append(bufferedReader.readLine());
					strBuffer.append(",");
				}
				
				String data = strBuffer.toString().substring(0, strBuffer.toString().length()-1) + "]";
				
				byte b[] = data.getBytes();
				InputStream myInputStream = new ByteArrayInputStream(b);
				bufferedReader = new BufferedReader(new InputStreamReader(myInputStream));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			

			return new BufferedReaderEntityIterator<Specimen>(bufferedReader, specimenFactory, pageNumber);			
			

		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> traitgroupProvider = new EntityProvider<GeneralType>(){

		private TraitGroupFactory    traitGroupFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			traitGroupFactory = new TraitGroupFactory();
		}		
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

			BufferedReader bufferedReader;
			
			if(traitGroupFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, traitGroupFactory);			
			
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> workflowProvider = new EntityProvider<GeneralType>(){

		
		private WorkflowFactory      workflowFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			workflowFactory = new WorkflowFactory();
		}
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

			BufferedReader bufferedReader;
			
			if(workflowFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, workflowFactory);			
			
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> trialeventProvider = new EntityProvider<GeneralType>(){

		private TrialEventFactory    trialEventFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			trialEventFactory = new TrialEventFactory();
		}
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(trialEventFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, trialEventFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> genotypespecimenProvider = new EntityProvider<GeneralType>(){

		private GenotypeSpecimenFactory genotypeSpecimenFactory;
		private CloseableHttpClient     client;
		
		private void createFactory(){
			genotypeSpecimenFactory = new GenotypeSpecimenFactory();
		}	
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(genotypeSpecimenFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, genotypeSpecimenFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> genparentProvider = new EntityProvider<GeneralType>(){

		private GenParentFactory    genParentFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			genParentFactory = new GenParentFactory();
		}	
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(genParentFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, genParentFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> genotypealiasstatusProvider = new EntityProvider<GeneralType>(){

		private GenoTypeAliasStatusFactory genoTypeAliasStatusFactory;
		private CloseableHttpClient        client;
		
		private void createFactory(){
			genoTypeAliasStatusFactory = new GenoTypeAliasStatusFactory();
		}	
		
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(genoTypeAliasStatusFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, genoTypeAliasStatusFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}


		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> genotypealiasProvider = new EntityProvider<GeneralType>(){

		private GenoTypeAFactory     genoTypeAFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			genoTypeAFactory = new GenoTypeAFactory();
		}			
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			BufferedReader bufferedReader;
			
			if(genoTypeAFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, genoTypeAFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> parentProvider = new EntityProvider<GeneralType>(){

		private ParentFactory        parentFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			parentFactory = new ParentFactory();
		}		
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			BufferedReader bufferedReader;
			
			if(parentFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, parentFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> specimengroupProvider = new EntityProvider<GeneralType>(){

		private SpecimenGroupFactory specimenGroupFactory;
		private CloseableHttpClient  client;
		
		private void createFactory(){
			specimenGroupFactory = new SpecimenGroupFactory();
		}			
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(
				String id, int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

			BufferedReader bufferedReader;
			
			if(specimenGroupFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, specimenGroupFactory);			

		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> containerProvider = new EntityProvider<GeneralType>(){

		private ContainerFactory    containerFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			containerFactory = new ContainerFactory();
		}		
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			BufferedReader bufferedReader;
			
			if(containerFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, containerFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> stateProvider = new EntityProvider<GeneralType>(){

		private StateFactory        stateFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			stateFactory = new StateFactory();
		}
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(stateFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, stateFactory);
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> unittypeProvider = new EntityProvider<GeneralType>(){

		private UnitTypeFactory     unitTypeFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			unitTypeFactory = new UnitTypeFactory();
		}
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {

			BufferedReader bufferedReader;
			
			if(unitTypeFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, unitTypeFactory);			
			
			
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> itemparentProvider = new EntityProvider<GeneralType>(){

		private ItemParentFactory   itemParentFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			itemParentFactory = new ItemParentFactory();
		}
				
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(itemParentFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, itemParentFactory);			
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}


		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> itemProvider = new EntityProvider<GeneralType>(){

		
		private ItemFactory         itemFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			itemFactory = new ItemFactory();
		}	
		
		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(itemFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, itemFactory);				

		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> sampleProvider = new EntityProvider<GeneralType>(){
		
		private SampleFactory       sampleFactory;
		private CloseableHttpClient client;
		
		private void createFactory(){
			sampleFactory = new SampleFactory();
		}		

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			BufferedReader bufferedReader;
			
			if(sampleFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"\",\"typeNote\":\"\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, sampleFactory);	
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(int firstRecord,
				int nRecords, String filterClause, Page pageNumber)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	/*
	 * @author Raul Hernandez T.
	 * @date   12/10/2015
	 */
	private EntityProvider<GeneralType> trialTypeProvider = new EntityProvider<GeneralType>(){
		
		
		private TrialTypeFactory trialTypeFactory;
		private CloseableHttpClient client;
		private HttpGet request;
		
		private void createFactory(){
			trialTypeFactory = new TrialTypeFactory();
		}

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Page getEntityCountPage(String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GeneralType getEntity(String id, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			
			
			BufferedReader bufferedReader;
			
			if(trialTypeFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			//request = new HttpGet(trialTypeFactory.getURL(filterClause));
			
			try{
			     /*
			      * Simulate call
			      * @author Raul Hernandez T.
			      * @date   14-DIC-2015
			      */
			     HttpResponseFactory factory = new DefaultHttpResponseFactory();
			     HttpResponse response =factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
			     response.setEntity(new StringEntity("[{\"isTypeActive\":1,\"typeId\":1,\"typeName\":\"Nursery\",\"typeNote\":\"Nursery Type\"},{\"isTypeActive\":1,\"typeId\":2,\"typeName\":\"Trial\",\"typeNote\":\"Trial Type\"}]",Consts.UTF_8));
			     response.addHeader("Content-type", "application/json");
			     
			     //HttpResponse response = client.execute(request);
 				 bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<GeneralType>(bufferedReader, trialTypeFactory);		

			
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public EntityIterator<? extends GeneralType> createIterator(
				int firstRecord, int nRecords, String filterClause,
				Page pageNumber) throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void prepareDetailsSearch() throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendDataUsingPut(Map<String, String> parameters,
				List<String> dalOpParameters, Map<String, String> filePathByName)
				throws DalDbException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	private EntityProvider<Site> siteProvider = new EntityProvider<Site>() {
		
		private CloseableHttpClient client;
		private HttpGet request;		
		private SiteFactory siteFactory;
		private PageFactory pageFactory;

		private void createFactory() {
			siteFactory = new SiteFactory();
		}
		
		private void createPageFactory() {
			pageFactory = new PageFactory();
		}		

		@Override
		public Page getEntityCountPage(String filterClause) throws DalDbException {
			
			BufferedReader bufferedReader;
			Page result = null;
			
			int total = 0;
			
			if(pageFactory == null){
				createPageFactory();
			}
			
			if(siteFactory == null){
				createFactory();
			}
			
			client = HttpClientBuilder.create().build();
			request = new HttpGet(siteFactory.createCountQuery(filterClause));
						
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				BufferedReaderEntityIterator<Page> entityIterator =  new BufferedReaderEntityIterator<Page>(bufferedReader, pageFactory);
				entityIterator.readLine();
				result = entityIterator.nextEntity();
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
					
			return result;
		}
		
		@Override
		public Site getEntity(String id, String filterClause) throws DalDbException {
			
			if(siteFactory == null){
				createFactory();
			}

			try {
				final Site[] result = new Site[1];

				ResultSetVisitor visitor = new ResultSetVisitor() {
					@Override
					public Continue visit(ResultSet rs) {
						try {
							result[0] = siteFactory.createEntity(rs);
						} catch (DalDbException e) {
							return Continue.error(e);
						}
						return Continue.STOP; // only the first
					}
				};
				
				String sql = siteFactory.createGetQuery(id, filterClause);
				
				Connection c = bmsConnections.getConnectionFor(id);
				if (c != null) {
					Continue cont = SqlUtil.performQuery(c, sql, visitor);
					if (cont.isError()) {
						Throwable t = cont.throwable;
						if (t instanceof DalDbException) {
							throw ((DalDbException) t);
						}
						throw new DalDbException(t);
					}
				}

				return result[0];
			}
			finally {
				try {siteFactory.close(); } 
				catch (IOException ignore) { }
			}
		}
		
		@Override
		public EntityIterator<? extends Site> createIdIterator(String id,int firstRecord, int nRecords, String filterClause) throws DalDbException {
			
			//System.out.println("BEGIN createIdIterator (Site) in BMS_DalDatabase class====");
			BufferedReader bufferedReader;
			
			if(siteFactory == null){
				createFactory();
			}
					
			client = HttpClientBuilder.create().build();
			request = new HttpGet(siteFactory.createPagedListQuery(0, 0, null));
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			//System.out.println("END createIdIterator (Site) in BMS_DalDatabase class ====");
			return new BufferedReaderEntityIterator<Site>(bufferedReader, siteFactory);
		}
		
		public void prepareDetailsSearch() throws DalDbException {
			HttpClientBuilder clients = HttpClientBuilder.create();
			client = clients.build();
		}

		@Override
		public void getDetails(DalEntity entity) throws DalDbException {
			((Site)entity).getSiteId();
			request = new HttpGet(siteFactory.createListStudiesDetailsURL(String.valueOf(((Site)entity).getSiteId())));
						
			try{
				HttpResponse response = client.execute(request);
				siteFactory.processDetails(entity, new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
		}

		@Override
		public void getFullDetails(DalEntity entity) throws DalDbException {
			request = new HttpGet(siteFactory.createListStudiesDetailsURL(String.valueOf(((Site)entity).getSiteId())));
						
			try{
				HttpResponse response = client.execute(request);
				siteFactory.processTrial(entity, new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
		}
		
		@Override
		public EntityIterator<? extends Site> createIterator(int firstRecord,
				int nRecords, String filterClause) throws DalDbException {
			BufferedReader bufferedReader;
			
			if(siteFactory == null){
				createFactory();
			}
					
			client = HttpClientBuilder.create().build();
			request = new HttpGet(siteFactory.createPagedListQuery(0,0,null));
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			
			return new BufferedReaderEntityIterator<Site>(bufferedReader, siteFactory);
		}
		
		@Override
		public void sendDataUsingPut(Map<String, String> parameters,List<String> dalOpParameters,Map<String, String> filePathByName) throws DalDbException{
			throw new UnsupportedOperationException("Not supported yet.");	
		}

		@Override
		public int getEntityCount(String filterClause) throws DalDbException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public EntityIterator<? extends Site> createIterator(int firstRecord,
				int nRecords, String filterClause, Page page)
				throws DalDbException {
			//System.out.println("BEGIN createIdIterator (Site) in BMS_DalDatabase class====");
			BufferedReader bufferedReader;
			
			if(siteFactory == null){
				createFactory();
			}
					
			client = HttpClientBuilder.create().build();
			request = new HttpGet(siteFactory.createPagedListQuery(0, nRecords, null,page.getPageNumber()));
			
			try{
				HttpResponse response = client.execute(request);
				//System.out.println(request.getURI());
				bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
			//System.out.println("END createIdIterator (Site) in BMS_DalDatabase class ====");
			return new BufferedReaderEntityIterator<Site>(bufferedReader, siteFactory, page);
		}

		@Override
		public EntityIterator<? extends Trial> createIdIterator(String id,
				int firstRecord, int nRecords, String filterClause, Page page)
				throws DalDbException {
			// TODO Auto-generated method stub
			return null;
		}		

	};	

}
