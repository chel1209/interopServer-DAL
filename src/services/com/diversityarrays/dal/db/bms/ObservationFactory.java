package com.diversityarrays.dal.db.bms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diversityarrays.dal.entity.MeasurementIdentifier;
import com.diversityarrays.dal.entity.Measurements;
import com.diversityarrays.dal.entity.Observation;
import com.diversityarrays.dal.entity.Trait;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;

import com.google.gson.Gson;


/*
 * @author Raul Hernandez T.
 * @date   09/02/2015
 */

public class ObservationFactory {
	
	private String strURL;
	
	private static final String UNIQUE_IDENTIFIER      = "TrialUnitId";
	private static final String GERMPLASM_ID           = "SpecimenId";
	private static final String GERMPLASM_DESIGNATION  = "SpecimenName";
	private static final String ENTRY_NUMBER           = "UnitPositionText";
	private static final String ENTRY_TYPE             = "entryType";
	private static final String PLOT_NUMBER            = "UnitPositionId";
	private static final String REPLICATION_NUMBER     = "ReplicateNumber";
	private static final String ENVIRONMENT_NUMBER     = "TreatmentId";
	private static final String SEED_SOURCE            = "TrialUnitNote";
	
	private static final String MEASUREMENT_IDENTIFIER = "measurementIdentifier";
	private static final String MEASUREMENT_VALUE      = "TraitValue";
	
	private static final String MEASUREMENT_ID         = "TrialTraitId";
	
	private static final String TRAIT                  = "trait"; 
	private static final String TRAIT_ID               = "TraitId";
	private static final String TRAIT_NAME             = "TraitName";
	
	private MeasurementIdentifier mIdentifier;
	private List<Measurements>    measureList;
	private Trait                 trait;
	private Measurements          measurements;

	
	
	public String getURL(List<String> dalOpParameters) {
		return BMSApiDataConnection.getObservationCall(dalOpParameters);
 	}
	
	public String getJsonMapped(Map<String, String> filePathByName){
		String strJsonParsed = filePathByName.get("postData");
		System.out.println("BODY " + strJsonParsed);
		String response = "";
		if(strJsonParsed != null){
		   try{	
		      Observation obj = parseJson(new JsonParser(strJsonParsed));
		      Gson gson = new Gson();
		      response = gson.toJson(obj).toString(); 
		   }catch(Exception w){
			   System.out.println("Error en getJsonMapped: " + w.toString());
		   }
		}
		
		return response;
		
	}
	
	public Observation parseJson(JsonParser parser) throws Exception{
		
	 	  Observation observation = new Observation();

		  observation.setUniqueIdentifier(Integer.valueOf(parser.getMapResult().get(UNIQUE_IDENTIFIER).toString()));
		  observation.setGermplasmId(Integer.valueOf(parser.getMapResult().get(GERMPLASM_ID).toString()));
		  observation.setGermplasmDesignation(parser.getMapResult().get(GERMPLASM_DESIGNATION).toString());
		  observation.setEntryNumber(Integer.valueOf(parser.getMapResult().get(ENTRY_NUMBER).toString()));
		  observation.setEntryType(parser.getMapResult().get(ENTRY_TYPE).toString());
		  observation.setPlotNumber(Integer.valueOf(parser.getMapResult().get(PLOT_NUMBER).toString()));
		  observation.setReplicationNumber(Integer.valueOf(parser.getMapResult().get(REPLICATION_NUMBER).toString()));
		  observation.setEnvironmentNumber(Integer.valueOf(parser.getMapResult().get(ENVIRONMENT_NUMBER).toString()));
		  observation.setSeedSource(parser.getMapResult().get(SEED_SOURCE).toString());
		  
		  List<Object> measurementList =  (List)parser.getMapResult().get("measurements");
		  measureList = new ArrayList<Measurements>();
		  
		  for(Object obj : measurementList){
			  measurements = new Measurements();
			  for(String key : ((JsonMap)obj).getKeysInOrder()){
				  
				  mIdentifier = new MeasurementIdentifier();
				  
				  if(key.equals(MEASUREMENT_IDENTIFIER)){
					 
					 Object meId = ((JsonMap)obj).get(MEASUREMENT_IDENTIFIER);
					 
					 for(String keyMID : ((JsonMap)meId).getKeysInOrder()){
						 
						 if(keyMID.equals(MEASUREMENT_ID)){
							try{ 
						        mIdentifier.setMeasurementId(Integer.valueOf(((JsonMap)meId).get(MEASUREMENT_ID).toString()));
							}catch(Exception w){
								System.out.println("Error " + w.toString());
								mIdentifier.setMeasurementId(null);
							}
						 }							 
						 
						 if(keyMID.equals(TRAIT)){
							 trait = new Trait();
							 Object traitObj = ((JsonMap)meId).get(TRAIT);
							 
							 for(String keyTrait : ((JsonMap)traitObj).getKeysInOrder()){
								 if(keyTrait.equals(TRAIT_ID)){
								    trait.setTraitId(Integer.valueOf(((JsonMap)traitObj).get(TRAIT_ID).toString()));
								 }
								 
								 if(keyTrait.equals(TRAIT_NAME)){
								    trait.setTraitName(((JsonMap)traitObj).get(TRAIT_NAME).toString());												 
								 }									 
							 }
							 mIdentifier.setTrait(trait);
						 }
					 }

					 measurements.setMeasurementIdentifier(mIdentifier);
				  }
				  
				  if(key.equals(MEASUREMENT_VALUE)){
					 measurements.setMeasurementValue(((JsonMap)obj).get(MEASUREMENT_VALUE).toString());
				  }
			  }
			  measureList.add(measurements);
		  }
		  
	    observation.setMeasurements(measureList);
		System.out.println("observation: \n" + observation.toString());
		return observation;
	}
}
