package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.pearcan.json.JsonUtil;

import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityIterator;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class GetTrialsOperation extends EntityOperation<Trial, BMS_DalDatabase> {
	
	//public static final Pattern PATTERN = Pattern.compile("^trial/details/_[a-z]*");
	public static final Pattern PATTERN = Pattern.compile("^trial/details/_[a-z]*");
	
	public static final String ENTITY_NAME = "trial";

	public GetTrialsOperation(BMS_DalDatabase db, 
			EntityProvider<Trial> provider) 
	{
		super(db, ENTITY_NAME, "trial/details/_trial", Trial.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		EntityIterator<? extends Trial> iter = null;
		try {
			//Probar parametros que vienen desde IHttpSession
			//System.out.println("mapa dentro: " + methodParms);
			
			String[] trialIds = dalOpParameters.get(0).split("&");
			
			responseBuilder.addResponseMeta(entityTagName);
			for(String id : trialIds){
				String[] ids = id.split("=");
				id = ids[1];
				iter = entityProvider.createIdIterator(id,0, 0, filterClause);
							
				Trial entity;
				iter.readLine();
				do{
					if (null != (entity = iter.nextEntity())) {
						appendEntity(responseBuilder, entity);
					}
				}while(iter.isPending());
			}
			
		} finally {
			if (iter != null) {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}
	}
	
}