package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.entity.SystemUser;

/*
 * @author Raul Hernandez T.
 * @date 12-NOV-2015
 * 
 * @note Initial Approach, no mapping yet.
 * @version 1.0
 */

public class UserFactory implements SqlEntityFactory<SystemUser>{

	private static final String USER_ID        = "UserId";
	private static final String uSER_NAME      = "UserName";
	private static final String USER_PASSWORD  = "UserPassword";
	private static final String PASSWORD_SALT  = "PasswordSalt"; 
	
	public String getURL(String filterClause){
		return BMSApiDataConnection.getSampleCall(filterClause);
	}	
	
	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SystemUser createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemUser createEntity(JsonMap jsonMap) throws DalDbException {
		
		SystemUser systemUser = new SystemUser();
		
		/**BMS only returns a token, at the moment this is the only thing required to perform further calls to the API*/
		/*systemUser.setUserId(new Integer(jsonMap.get("user").toString()));
		systemUser.setUserName(jsonMap.get("user").toString());
		systemUser.setUserPassword(jsonMap.get("password").toString());*/
		systemUser.setPasswordSalt(jsonMap.get("token").toString());

		return systemUser;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createGetQuery(String id, String filterClause)
			throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String createLoginQuery(){
		return BMSApiDataConnection.getLoginURL();
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

}
