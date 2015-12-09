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
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class GetSampleTypeOperation extends EntityOperation<GeneralType, BMS_DalDatabase> {

	public static final Pattern PATTERN = Pattern.compile("^list/type/sample/1");
	
	public static final String ENTITY_NAME = "SampleType";

	public GetSampleTypeOperation(BMS_DalDatabase db, 
			EntityProvider<GeneralType> provider) 
	{
		super(db, ENTITY_NAME, "list/type/sample/1", GeneralType.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		EntityIterator<? extends GeneralType> iter = null;
		try {
			iter = entityProvider.createIdIterator("0", 0, 0, filterClause);

			responseBuilder.addResponseMeta(entityTagName);
			
			GeneralType entity;
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
