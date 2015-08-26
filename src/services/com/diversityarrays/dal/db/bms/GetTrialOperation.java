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
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class GetTrialOperation extends EntityOperation<Trial, BMS_DalDatabase> {
	
	public static final Pattern PATTERN = Pattern.compile("^trial/_[a-z]*");
	
	public static final String ENTITY_NAME = "trial";

	public GetTrialOperation(BMS_DalDatabase db, 
			EntityProvider<Trial> provider) 
	{
		super(db, ENTITY_NAME, "trial/_program", Trial.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		//String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		String filterClause = methodParms.get("programId");
		System.out.println("filterclause: " + filterClause);
		
		EntityIterator<? extends Trial> iter = null;
		try {
			iter = entityProvider.createIterator(0, 0, filterClause);
			entityProvider.prepareDetailsSearch();
			
			responseBuilder.addResponseMeta(entityTagName);
			
			Trial entity;
			iter.readLine();
			while (null != (entity = iter.nextEntity())) {
				entityProvider.getDetails(entity);
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