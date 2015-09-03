package com.diversityarrays.dal.db.bms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author Raul Hernandez T.
 * @date   09/02/2015
 */

public class ObservationFactory {
	
	private String strURL;
	
	public String getURL(List<String> dalOpParameters) {
  	   return "http://teamnz.leafnode.io:80/bmsapi/study/" + dalOpParameters.get(0) + "/" + dalOpParameters.get(1) + "/observations/" + dalOpParameters.get(2);
	}
}
