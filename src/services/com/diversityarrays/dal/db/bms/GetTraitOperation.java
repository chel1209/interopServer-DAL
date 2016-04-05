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
import com.diversityarrays.dal.entity.Traits;
import com.diversityarrays.dal.entity.TrialUnits;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

/**
 * 
 * @author Raul Hernandez T.
 * @date 03-15-2016
 *
 */

public class GetTraitOperation extends EntityOperation<Traits,BMS_DalDatabase>{
	
	public static final Pattern PATTERN = Pattern.compile("^trial/_[a-z]*/list/trait");
	public static final String  ENTITY_NAME = "Traits";
	
	public GetTraitOperation(BMS_DalDatabase db, EntityProvider<Traits> provider){
		super(db, ENTITY_NAME, "trial/_id/list/trait", Traits.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		String id = dalOpParameters.get(0);
		
		EntityIterator<? extends Traits> iter = null;
		
		try{
			
			iter = entityProvider.createIdIterator(id, 0, 0, filterClause);
			responseBuilder.addResponseMeta(entityTagName);
			
			Traits entity;
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
