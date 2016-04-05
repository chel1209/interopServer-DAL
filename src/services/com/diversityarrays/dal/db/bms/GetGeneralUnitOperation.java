package com.diversityarrays.dal.db.bms;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.GeneralUnit;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;

/**
 * 
 * @author Raul Hernandez T.
 * @date 03-08-2016
 *
 */
public class GetGeneralUnitOperation extends EntityOperation<GeneralUnit,BMS_DalDatabase>{
	
	public static final Pattern PATTERN = Pattern.compile("^list/generalunit/[a-z]*/page/[a-z]*");
	public static final String  ENTITY_NAME = "GeneralUnit";
	
	public GetGeneralUnitOperation(BMS_DalDatabase db, EntityProvider<GeneralUnit> provider){
		super(db, ENTITY_NAME, "list/generalunit/_nperpage/page/_num", GeneralUnit.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {
		// TODO Auto-generated method stub
		
	}	

}
