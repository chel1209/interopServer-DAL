package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Project;
import com.diversityarrays.dal.entity.Trial;



/*
 * @author Raul Hernandez T.
 * @date   10/07/2015
 */


public class ProjectFactory implements SqlEntityFactory<Project>{
	
	
	private static final String USER_ID      = "userId";
	private static final String START_DATE   = "startDate";
	private static final String PROJECT_NAME = "projectName";
	private static final String CROP_TYPE    = "cropType";
	private static final String UNIQUE_ID    = "uniqueID";
	
	
	
	public String getURL(String filterClause){
		return BMSApiDataConnection.getProjectCall(filterClause);
	}

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Project createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Project createEntity(JsonMap jsonMap) throws DalDbException {
		// TODO Auto-generated method stub
		
		Project project = new Project();
		
		project.setProjectManagerId(new Integer((jsonMap.get(USER_ID).toString())));
		project.setProjectManagerName(null);
		project.setProjectStartDate((String)jsonMap.get(START_DATE));
		project.setProjectName((String)jsonMap.get(PROJECT_NAME));
		project.setProjectNote((String)jsonMap.get(CROP_TYPE));
		project.setTypeId(null);
		project.setProjectStatus(null);
		project.setProjectEndDate(null);
		project.setProjectId(UNIQUE_ID);
		
		return project;
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
