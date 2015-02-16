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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.ops.DalOperation;
import com.diversityarrays.dal.server.DalSession;
import com.diversityarrays.dalclient.SessionExpiryOption;



/**
 * Defines the methods that will be called by the DalServer to provide the response
 * needed by each DAL operation.
 * <p>
 * DAL operations fall into several categories:
 * <ul>
 *   <li>Retrieval: <code>get</code> operations retrieve the record for a single
 *       <i>entity</i> whereas <code>list</code> operations may retrieve multiple
 *       records;<br>
 *       e.g. <code>get/genus/<i>ID</i></code> or <code>list/genus</code>)</li>
 *   <li>Update (not yet available)</li>
 *   <li>Delete (not yet available)</li>
 *   <li>System functions: These usually relate users, groups, entity metadata;
 *       e.g. <code>list/all/group</code> retrieves all the
 *       <i>SystemGroup</i> records in the database, whereas
 *       <code><i>TABLE_NAME</i>/list/field</code> retrieves the field names
 *       for the entity stored in the specified TABLE_NAME. If you need them,
 *       the <i>TABLE_NAME</i> for an entity is specified in the @Table annotation
 *       for any given sub-class of <code>DalEntity</code>.
 *   </li>
 *   <li>Session management: these relate to the login session; 
 *       e.g. <code>list/group</code> returns the <i>SystemGroup</i> records that
 *       the currently logged-in user belongs to (which in turn govern the
 *       access the session has to the various entities in the database).
 *       
 *   </li>
 * </ul>
 * Most of the <i>DAL entity</i> related operations are handled 
 * @author brian
 *
 */
public interface DalDatabase {

	/**
	 * Return the name for this DalDatabase.
	 * @return
	 */
	public String getDatabaseName();
	
	/**
	 * Return the version number of the database.
	 * @return String
	 * @throws DalDbException 
	 */
	public String getDatabaseVersion(DalSession session) throws DalDbException;

	/**
	 * Return all of the DalOperations that this DalDatabase supports.
	 * @return List of DalOperation
	 */
	public List<DalOperation> getOperations();

	/**
	 * Return all of the entity names for which there are operations.
	 * This is usually a lowercase name from the operation; e.g. "list/itemunit", "get/genus/_id"
	 * @return Collection of the entityNames
	 */
	public Collection<String> getEntityNames();

	/**
	 * Return the SystemGroupInfo of the specified userId/groupId combination.
	 * @param session
	 * @param groupId
	 * @return a SystemGroupInfo instance
	 * @throws DalDbException
	 */
	public SystemGroupInfo getSystemGroupInfo(DalSession session) throws DalDbException;

	/**
	 * 
	 * @param userId
	 * @param builder
	 * @param returnSql
	 */
	public void performListGroup(DalSession session, DalResponseBuilder builder,
			String[] returnSql) throws DalDbException;
	
	public void performListAllGroup(DalSession session, DalResponseBuilder builder,
			String[] returnSql) throws DalDbException;

	public UserInfo doLogin(String sessionId, String userName, SessionExpiryOption seo, Map<String, String> parms) throws AuthenticationException;
	public void doLogout(DalSession session);

	public boolean isInitialiseRequired();

	public String getDatabasePath();

	public void shutdown() throws DalDbException;

	public Class<? extends DalEntity> getEntityClass(String tname);

	public void initialise(Closure<String> progress) throws DalDbException;

	public DbDataLoader getDbDataLoader() throws DalDbException;

	public void performListField(DalSession session, String tableName, DalResponseBuilder responseBuilder) throws DalDbException;

}
