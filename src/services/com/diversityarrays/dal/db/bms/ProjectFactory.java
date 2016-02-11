package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Project;



/*
 * @author Raul Hernandez T.
 * @date   10/07/2015
 */
public class ProjectFactory implements SqlEntityFactory<Project>{
	
	
	/*
	private static final String USER_ID      = "userId";
	private static final String START_DATE   = "startDate";
	private static final String PROJECT_NAME = "projectName";
	private static final String CROP_TYPE    = "cropType";
	private static final String UNIQUE_ID    = "uniqueID";
	*/
	
	private static final String ID         = "id";
	private static final String UNIQUE_ID  = "uniqueID";
	private static final String NAME       = "name";
	private static final String CREATED_BY = "createdBy";
	private static final String MEMBERS    = "members";
	private static final String CROP       = "crop";
	private static final String START_DATE = "startDate";
	
	
	
	
	
	
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
		
		//no mapping
		project.setProjectManagerId(null);
		
		//createdBy => projectManagerName
		project.setProjectManagerName((String)jsonMap.get(CREATED_BY));
		
		//startDate => projectStartDate
		project.setProjectStartDate((String)jsonMap.get(START_DATE));
		
		//name => projectName
		project.setProjectName((String)jsonMap.get(NAME));
		
		//crop => projectNote
		project.setProjectNote((String)jsonMap.get(CROP));
		
		//no mapping
		project.setTypeId(null);
		
		//no mapping
		project.setProjectStatus(null);
		
		//no mapping
		project.setProjectEndDate(null);
		
		//uniqueID => projectID
		project.setProjectId((String)jsonMap.get(UNIQUE_ID));
		
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
		return BMSApiDataConnection.getProjectCall(filterClause);
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

}
