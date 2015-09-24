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
import com.diversityarrays.dal.entity.ItemUnit;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

public class ItemUnitGetOperation extends EntityOperation<ItemUnit, BMS_DalDatabase> {
	
	public static final Pattern PATTERN = Pattern.compile("^itemunit/_[a-z]*");
	
	public static final String ENTITY_NAME = "itemunit";

	public ItemUnitGetOperation(BMS_DalDatabase db, 
			EntityProvider<ItemUnit> provider) 
	{
		super(db, ENTITY_NAME, "itemunit/_itemunitid", ItemUnit.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
	throws DalDbException {
		
		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		EntityIterator<? extends ItemUnit> iter = null;
		try {
			String id = dalOpParameters.get(0);
			iter = entityProvider.createIdIterator(id, 0, 0, filterClause);

			responseBuilder.addResponseMeta(entityTagName);
			
			ItemUnit entity;
			iter.readLine();
			while (null != (entity = iter.nextEntity())) {
				appendEntity(responseBuilder, entity);
				iter.readLine();
			}
		} finally {
			if (iter != null) {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}
	}
}
