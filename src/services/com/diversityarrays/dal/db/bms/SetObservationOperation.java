package com.diversityarrays.dal.db.bms;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.entity.Observation;
import com.diversityarrays.dal.server.DalSession;

import fi.iki.elonen.NanoHTTPD.Method;


/**
 * @author: Raul Hernandez T.
 * @date:   08-31-2015
 * 
 * The call was changed to user csv files 
 */

public class SetObservationOperation extends EntityOperation<Observation, BMS_DalDatabase> {
	
	public static final Pattern PATTERN = Pattern.compile("^trial/_[a-z]*/import/datakapturetemplate/csv");

	public static final String ENTITY_NAME = "observation";

	public SetObservationOperation(BMS_DalDatabase db, EntityProvider<Observation> provider) 
	{
		super(db, ENTITY_NAME, "trial/_id/import/datakapturetemplate/csv", Observation.class, provider);
	}

	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String dalcmd, List<String> dalOpParameters,
			Map<String, String> methodParms, Map<String, String> filePathByName)
			throws DalDbException {

		System.out.println("SetObservationOperation [BEGIN execute]: " + methodParms);
		entityProvider.sendDataUsingPut(methodParms,dalOpParameters,filePathByName);

	}
	
	

}
