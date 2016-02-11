package com.diversityarrays.dal.db.bms;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.Season;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;


public class GetSeasonOperation extends EntityOperation<Season,BMS_DalDatabase>{

	public static final Pattern PATTERN = Pattern.compile("pending");
	public static final String  ENTITY_NAME = "Season";	
	
	public GetSeasonOperation(BMS_DalDatabase db, EntityProvider<Season> provider){
		super(db, ENTITY_NAME, "pending", Season.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
