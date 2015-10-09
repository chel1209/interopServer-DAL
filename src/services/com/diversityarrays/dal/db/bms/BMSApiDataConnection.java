package com.diversityarrays.dal.db.bms;

import java.util.List;

public class BMSApiDataConnection {
	
	
	private static String IP   = "172.17.60.83";
	private static String PORT  = "18080";
	private static String CROP = "wheat";

	
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


}
