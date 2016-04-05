package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityIterator;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.Project;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;



/**
 * @author Raul Hernandez T.
 * @date 10-07-2015
 */


public class GetProjectOperation extends EntityOperation<Project,BMS_DalDatabase>{
	
	public static final Pattern PATTERN = Pattern.compile("^list/project/[a-z]*/page/[a-z]*");
	public static final String  ENTITY_NAME = "Project";
	

	public GetProjectOperation(BMS_DalDatabase db, EntityProvider<Project> provider){
		super(db, ENTITY_NAME, "list/project/_nperpage/page/_num", Project.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		String id = dalOpParameters.get(0);
		
		EntityIterator<? extends Project> iter = null;
		
		try{
			
			iter = entityProvider.createIdIterator(id, 0, 0, filterClause);
			responseBuilder.addResponseMeta(entityTagName);
			
			Project entity;
			iter.readLine();
			
			while (null != (entity = iter.nextEntity())) {
				appendEntity(responseBuilder, entity);
			} 
			
		} finally{
			if(iter != null){
				try{iter.close();}
				catch(IOException ignore){}
			}
		}
	}
}
