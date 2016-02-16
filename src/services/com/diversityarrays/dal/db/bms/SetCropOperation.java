package com.diversityarrays.dal.db.bms;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.Crop;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;


/**
 * 
 * @author Raul Hernandez T.
 * @date 2/12/2016
 *
 */
public class SetCropOperation extends EntityOperation<Crop,BMS_DalDatabase>{

	public static final Pattern PATTERN = Pattern.compile("^set/group/_[a-z]*");
	public static final String  ENTITY_NAME = "Crop";	
	
	public SetCropOperation(BMS_DalDatabase db, EntityProvider<Crop> provider){
		super(db, ENTITY_NAME, "set/group/_id", Crop.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {

		System.out.println("SetCropOperation [BEGIN execute]: " + methodParms);
		entityProvider.sendDataUsingPut(methodParms,dalOpParameters,filePathByName);
	}	
}
