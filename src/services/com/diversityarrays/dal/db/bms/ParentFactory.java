package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.GeneralType;

/*
 * @author Raul Hernandez T.
 * @date 12-NOV-2015
 * 
 * @note Initial Approach, no mapping yet.
 * @version 1.0
 */
public class ParentFactory implements SqlEntityFactory<GeneralType>{

	private static final String TYPE_ID        = "typeId";
	private static final String TYPE_NAME      = "typeName";
	private static final String IS_TYPE_ACTIVE = "isTypeActive";
	private static final String TYPE_NOTE      = "typeNote";
	
	public String getURL(String filterClause){
		return BMSApiDataConnection.getSampleCall(filterClause);
	}	
	
	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GeneralType createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralType createEntity(JsonMap jsonMap) throws DalDbException {
		GeneralType generalType = new GeneralType();
		
		generalType.setTypeActive(new Boolean(jsonMap.get(IS_TYPE_ACTIVE).toString()));
		generalType.setTypeId(new Integer(jsonMap.get(TYPE_ID).toString()));
		generalType.setTypeName((String)jsonMap.get(TYPE_NAME));
		generalType.setTypeNote((String)jsonMap.get(TYPE_NOTE));		
		
		return generalType;
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
