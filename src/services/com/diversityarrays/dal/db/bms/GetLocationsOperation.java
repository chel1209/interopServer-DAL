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
import com.diversityarrays.dal.entity.Site;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class GetLocationsOperation extends EntityOperation<Site, BMS_DalDatabase> {

	public static final Pattern PATTERN = Pattern.compile("^list/site/[a-z]*/page/[a-z]*");
	
	public static final String ENTITY_NAME = "Site";

	public GetLocationsOperation(BMS_DalDatabase db, 
			EntityProvider<Site> provider) 
	{
		super(db, ENTITY_NAME, "list/site/_nperpage/page/_num", Site.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		EntityIterator<? extends Site> iter = null;
		try {
			iter = entityProvider.createIdIterator("0", 0, 0, filterClause);

			responseBuilder.addResponseMeta(entityTagName);
			
			Site entity;
			iter.readLine();
			do{
				if (null != (entity = iter.nextEntity())) {
					appendEntity(responseBuilder, entity);
				}
			}while(iter.isPending());
		} finally {
			if (iter != null) {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}
	}

}
