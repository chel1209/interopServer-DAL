package com.diversityarrays.dal.db.bms;

import java.util.List;

public class BMSApiDataConnection {
	
	
	public  static final String IP                 = "bms-v4b5t-11";
	public  static final String PORT               = "48080";
	public  static final String CROP               = "wheat";
	private static final String LOCATION_TYPE_ID   = "410";
	public  static final int BMS_MAX_PAGE_SIZE      = 1000;
	public  static final String PAGE_DOES_NOT_EXIST = "The page number you requested is too big.";
	public  static final String BMS_USER            = "user1";
	public  static final String BMS_PASSWORD        = "cimmyt";
	public  static final String TOKEN_HEADER        = "X-Auth-Token";
	public  static final String CONTENT_TYPE        = "application/x-www-form-urlencoded"; 

	
	/* 
	  Template: /study/{cropname}/{studyId}/observations/{observationId} 
	*/
	 
	public static String getObservationCall(List<String> dalOpParameters) {
		return "http://" + IP + ":"+ PORT +"/bmsapi/study/" + dalOpParameters.get(0) + "/" + dalOpParameters.get(1) + "/observations/" + dalOpParameters.get(2);
	}
	
	/*
	 * TODO Change the programId
	 */
	public static String getGeneralTypeCall(String Id){
		return "http://" + IP + ":" + PORT + "/bmsapi/ontology/" + CROP + "/variables/" + Id + "?programId=fd628e26-9016-4f71-84bb-df00b38f3ee7";
	}
	
    public static String getListStudiesCall(String filterClause){
    	return "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/list?programUniqueId=" + filterClause;
    	
    }
    
    public static String getListStudiesDetails(String id){
    	return "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/" + id;
    }
    
    public static String getObservationsCall(String id){
    	return "http://" + IP +":" + PORT + "/bmsapi/study/" + CROP + "/" + id + "/observations";
    }
    
    public static String getScalesCall(String id){
        return "http://" + IP +":" + PORT + "/bmsapi/ontology/" + CROP + "/scales/" + id;
    }
    
    public static String getProjectCall(String filterClause){
    	return "http://" + IP +":" + PORT + "/bmsapi/program/list";
    	
    }

    public static String getLocationsCall(int pageNumber, int pageSize){
        return "http://" + IP +":" + PORT + "/bmsapi/location/" + CROP + "?locationTypeId=" + LOCATION_TYPE_ID + "&pageNumber=" + pageNumber + "&pageSize=" + pageSize;
    }
    
    public static String getTrialTypeCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getSampleCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getItemCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getItemParentCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getUnitTypeCall(String filterClause){
    	return "No implemented yet";
    }    
    
    public static String getStateCall(String filterClause){
    	return "No implemented yet";
    }    
    
    public static String getContainerCall(String filterClause){
    	return "No implemented yet";
    }    
    
    public static String getSpecimenGroupCall(String filterClause){
    	return "No implemented yet";
    }    
    
    public static String getParentCall(String filterClause){
    	return "No implemented yet";
    }    
    
    public static String getGenotypeAliasCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getGenotypeAliasStatusCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getGenParenStatusCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getGenotypeSpecimenCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getTrialEventCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getWorkflowCall(String filterClause){
    	return "No implemented yet";
    }    

    public static String getTraitGroupCall(String filterClause){
    	return "No implemented yet";
    }
    
    public static String getSpecimenCall(String id){
       return "http://" + IP + ":" + PORT + "/bmsapi/germplasm/" + CROP + "/" + id;	
    }
    
    public static String getTrialSearchCall(String programUniqueId,String principalInvestigator,String location, String season){
    	String call = null;
    	if(programUniqueId != null && principalInvestigator != null && location != null && season != null){
    		call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?programUniqueId="+ programUniqueId + "&principalInvestigator=" + principalInvestigator + "&location=" + location + "&season=" + season;
    	}else{
    		if(programUniqueId == null && principalInvestigator != null && location != null && season != null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?principalInvestigator=" + principalInvestigator + "&location=" + location + "&season=" + season;
    		}
    		if(programUniqueId != null && principalInvestigator == null && location != null && season != null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?programUniqueId="+ programUniqueId + "&location=" + location + "&season=" + season;
    		}
    		if(programUniqueId != null && principalInvestigator == null && location == null && season != null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?programUniqueId="+ programUniqueId + "&season=" + season;
    		}
    		if(programUniqueId != null && principalInvestigator == null && location == null && season == null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?programUniqueId="+ programUniqueId;
    		}    		
    		if(programUniqueId != null && principalInvestigator == null && location != null && season == null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?programUniqueId="+ programUniqueId + "&location=" + location;
    		}    		
    		if(programUniqueId == null && principalInvestigator == null && location != null && season != null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?location=" + location + "&season=" + season;
    		}
    		if(programUniqueId == null && principalInvestigator == null && location == null && season != null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search?season=" + season;
    		}
    		if(programUniqueId == null && principalInvestigator == null && location == null && season == null){
    			call = "http://" + IP + ":" + PORT + "/bmsapi/study/" + CROP + "/search";
    		}
    	}
    	
    	return call;
    }
    
    public static String getLoginURL(){
    	return "http://" + IP + ":" + PORT + "/bmsapi/authenticate";
    }
    
    public static String getCropURL(){
    	return "http://" + IP + ":" + PORT + "/bmsapi/crop/list";
    }
    
    public static String getGeneralUnit(){
    	return "No implemented yet";
    }
    
    public static String getTrialUnit(){
    	return "No implemented yet";
    }
    
}
