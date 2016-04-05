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
import com.diversityarrays.dal.entity.OutputFile;
import com.diversityarrays.dal.entity.TrialUnits;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class TrialExportOperation extends EntityOperation<OutputFile, BMS_DalDatabase> {
	
														
	public static final Pattern PATTERN = Pattern.compile("^export/samplemeasurement/csv");
	
	public static final String ENTITY_NAME = "OutputFile";
	
	public TrialExportOperation(BMS_DalDatabase db,EntityProvider<OutputFile> provider) 
	{
		super(db, ENTITY_NAME, "export/samplemeasurement/csv", OutputFile.class, provider);
	}
	
	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		//String id = dalOpParameters.get(0);
		EntityIterator<? extends OutputFile> iter = null;
		try {
/*			//Probar parametros que vienen desde IHttpSession
			//System.out.println("mapa dentro: " + methodParms);
			
			//String[] trialIds = dalOpParameters.get(0).split("&");
			//String trialIds = dalOpParameters.get(0);
			
			String id = methodParms.get("TrialIdCSV");
			
			responseBuilder.addResponseMeta(entityTagName);
			for(String id : trialIds){
				String[] ids = id.split("=");
				id = ids[1];
				iter = entityProvider.createIdIterator(id,0, 0, filterClause);
							
				OutputFile entity;
				iter.readLine();
				do{
					if (null != (entity = iter.nextEntity())) {
						appendEntity(responseBuilder, entity);
					}
				}while(iter.isPending());
			//}
*/			
			iter = entityProvider.createIterator(0, 0, filterClause);
			responseBuilder.addResponseMeta(entityTagName);
			
			OutputFile entity;
			iter.readLine();
			
			while (null != (entity = iter.nextEntity())) {
				appendEntity(responseBuilder, entity);
			} 
			
			
			
			
		} finally {
			if (iter != null) {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}		
		
	}

}
