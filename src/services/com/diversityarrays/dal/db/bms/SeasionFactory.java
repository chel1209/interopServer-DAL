package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Season;


/**
 * 
 * @author RHTOLEDO
 *
 */
public class SeasionFactory implements SqlEntityFactory<Season> {

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Season createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Season createEntity(JsonMap jsonMap) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
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
