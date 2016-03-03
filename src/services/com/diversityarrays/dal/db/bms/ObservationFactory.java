package com.diversityarrays.dal.db.bms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diversityarrays.dal.entity.CsvFile;
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
	
	/** 
	 * Read the csv file and store in a matrix 
	 */
	public String[][] getDataFromCSVFile(String uri){
		
		BufferedReader brReader = null;
		String line             = "";
		ArrayList<String> data  = null;
		String[][] dataCsv      = null;
		int row=0;
		int col=0;
		int numHeaders = 0;
		try{
			brReader = new BufferedReader(new FileReader(uri));
			data = new ArrayList<String>();
			while((line= brReader.readLine()) != null){
				   data.add(line);	
			}
			
			if(!data.isEmpty()){
				
				String[] headers = data.get(0).split(" ");
				numHeaders = headers.length;
				dataCsv = new String[data.size()][numHeaders];
				
				col=0;
				for(String head : headers){
					dataCsv[0][col++] = head;
				}
				
				if(data.size()>1){
				   
				   for(row=1;row<data.size();row++){
					   col=0;
					   for(String det : data.get(row).split(",")){
						   dataCsv[row][col++] = det;
					   }
				   }
				}
			}
			
			setCsvFileObj(dataCsv);

			
			
		}catch(ArrayIndexOutOfBoundsException ae){
			System.out.println("The CSV file is not correct in line " + (row+1));
			System.out.println("Number of headers " + numHeaders + " Number of data columns " + col);
		}
		catch(Exception e){
			System.out.println("Error al leer el archivo: " + e.toString());
		}
		
		return dataCsv;
		
		
	}
	
	public List<CsvFile> setCsvFileObj(String[][] values){
		
		Map<String,String> headersValues = new HashMap<String,String>();
		Map<String,String> traitValues   = new HashMap<String,String>();
		
		List<CsvFile> lCFiles = new ArrayList<CsvFile>();
		CsvFile cFile = new CsvFile();
		
		for(int z=1;z<values.length;z++){
			for(int y=0;y<values[z].length;y++){
				String header = values[0][y];
				String value  = values[z][y];
				if(cFile.getColNumHeaders() > y){				
			 	   headersValues.put(header, value);
				}else{
				   traitValues.put(header, value);
				}
				
			}
			cFile = new CsvFile();
			cFile.setHeaders(headersValues);
			cFile.setTraitValues(traitValues);
			lCFiles.add(cFile);
		}
		System.out.println();
		System.out.println("Total " + lCFiles.size());
		
		
		return lCFiles;
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
