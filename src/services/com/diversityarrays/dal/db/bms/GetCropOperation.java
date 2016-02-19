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
import com.diversityarrays.dal.entity.SystemGroup;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

/**
 * 
 * @author Raul Hernandez T.
 * @date 2/9/2016
 *
 */
public class GetCropOperation extends EntityOperation<SystemGroup,BMS_DalDatabase>{
	
	public static final Pattern PATTERN = Pattern.compile("^list/group");
	public static final String  ENTITY_NAME = "Crop";	
	
	public GetCropOperation(BMS_DalDatabase db, EntityProvider<SystemGroup> provider){
		super(db, ENTITY_NAME, "list/group", SystemGroup.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		EntityIterator<? extends SystemGroup> iter = null;
		
		try{
			
			iter = entityProvider.createIterator( 0, 0, filterClause);
			responseBuilder.addResponseMeta(entityTagName);
			
			SystemGroup entity;
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
