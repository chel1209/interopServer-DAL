package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.SystemGroup;


/**
 * 
 * @author Raul Hernandez T.
 * @date 2/8/2016
 * @version 1.0
 */
public class CropFactory implements SqlEntityFactory<SystemGroup> {
	
	
	private static final String SYSTEM_GROUP_NAME        = "systemGroupName"; 
	private static final String SYSTEM_GROUP_DESCRIPTION = "systemGroupDescription";
	private static final String SYSTEM_GROUP_ID          = "systemGroupId";
	
	
	public String getURL(){
		return BMSApiDataConnection.getCropURL();
	}

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SystemGroup createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemGroup createEntity(JsonMap jsonMap) throws DalDbException {
		
		SystemGroup crop = new SystemGroup();
		
		crop.setSystemGroupId((String) jsonMap.get(SYSTEM_GROUP_ID));
		
		crop.setSystemGroupName((String) jsonMap.get(SYSTEM_GROUP_NAME));
		
		crop.setSystemGroupDescription((String) jsonMap.get(SYSTEM_GROUP_DESCRIPTION));
		
		return crop;
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
