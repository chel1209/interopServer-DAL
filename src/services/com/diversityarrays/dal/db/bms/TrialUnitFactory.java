package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.TrialUnitSpecimen;
import com.diversityarrays.dal.entity.TrialUnits;

/**
 * @author Raul Hernandez T.
 * @date 03-10-2016
 */
public class TrialUnitFactory implements SqlEntityFactory<TrialUnits> {

	/**
	 * TODO: Acompletar el mapeo cuando se tenga la info de BMS
	 */
	
	private String ULTIMATEPERM        = "UltimatePerm";
	private String ADDSPECIMEN         = "addSpecimen";
	private String ULTIMATEPERMISSION  = "UltimatePermission";
	private String TRIALUNITBARCODE    = "TrialUnitBarcode";
	private String UPDATE              = "update";
	private String SOURCETRIALUNITID   = "SourceTrialUnitId";
	private String TRIALUNITID         = "TrialUnitId";
	private String LONGITUDE           = "Longitude";
	private String REPLICATENUMBER     = "ReplicateNumber";
	private String LISTSPECIMEN        = "listSpecimen";
	private String TRIALUNITNOTE       = "TrialUnitNote";
	private String TREATMENTTEXT       = "TreatmentText";
	private String SITEID              = "SiteId";
	private String TREATMENTID         = "TreatmentId";
	private String UNITPOSITIONID      = "UnitPositionId";
	private String SAMPLESUPPLIERID    = "SampleSupplierId";
	private String TRIALID             = "TrialId";
	private String LATITUDE            = "Latitude";
	private String UNITPOSITIONTEXT    = "UnitPositionText";
    private String SITENAME            = "SiteName";	
	//SPECIMEN
	private String SPECIMENNAME        = "SpecimenName";
	private String PLANTDATE           = "PlantDate";
	private String HASDIED             = "HasDied";
	private String ITEMID              = "ItemId";
	private String SPECIMENID          = "SpecimenId";
	private String NOTES               = "Notes";
	private String HARVESTDATE         = "HarvestDate";
	private String TRIALUNITSPECIMENID = "TrialUnitSpecimenId";
	//
	private String SPECIMEN            = "Specimen";
	
	
	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TrialUnits createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrialUnits createEntity(JsonMap jsonMap) throws DalDbException {

		
		TrialUnits trialUnit = new TrialUnits();
		
		//no mapping yet
		trialUnit.setUltimatePerm((String) jsonMap.get(ULTIMATEPERM));
		//no mapping yet
		trialUnit.setAddSpecimen((String) jsonMap.get(ADDSPECIMEN));
		//no mapping yet
		trialUnit.setUltimatePermission((String) jsonMap.get(ULTIMATEPERMISSION));
		//no mapping yet
		trialUnit.setTrialUnitBarcode((String) jsonMap.get(TRIALUNITBARCODE));
		//no mapping yet
		trialUnit.setUpdate((String) jsonMap.get(UPDATE));
		//no mapping yet
		trialUnit.setSourceTrialUnitId((String) jsonMap.get(SOURCETRIALUNITID));
		//no mapping yet
		trialUnit.setTrialUnitId((String) jsonMap.get(TRIALUNITID));
		//no mapping yet
		trialUnit.setLongitude((String) jsonMap.get(LONGITUDE));
		//no mapping yet
		trialUnit.setReplicateNumber((String) jsonMap.get(REPLICATENUMBER));
		//no mapping yet
		trialUnit.setListSpecimen((String) jsonMap.get(LISTSPECIMEN));
		//no mapping yet
		trialUnit.setTrialUnitNote((String) jsonMap.get(TRIALUNITNOTE));
		//no mapping yet
		trialUnit.setTreatmentText((String) jsonMap.get(TREATMENTTEXT));
		//no mapping yet
		trialUnit.setSiteId((String) jsonMap.get(SITEID));
		//no mapping yet
		trialUnit.setTreatmentId((String) jsonMap.get(TREATMENTID));
		//no mapping yet
		trialUnit.setUnitPositionId((String) jsonMap.get(UNITPOSITIONID));
		//no mapping yet
		trialUnit.setSampleSupplierId((String) jsonMap.get(SAMPLESUPPLIERID));
		//no mapping yet
		trialUnit.setTrialId((String) jsonMap.get(TRIALID));
		//no mapping yet
		trialUnit.setLatitude((String) jsonMap.get(LATITUDE));
		//no mapping yet
		trialUnit.setUnitPositionText((String) jsonMap.get(UNITPOSITIONTEXT));
		//no mapping yet
		trialUnit.setSiteName((String) jsonMap.get(SITENAME));
		
		TrialUnitSpecimen tUnitSpecimen = new TrialUnitSpecimen();
		
		
		List<Object> specimenList = (List) jsonMap.get(SPECIMEN);
		String strValue="";
		if(specimenList!=null){
		   for(Object map : specimenList){
			   
               strValue = ((JsonMap) map).get(SPECIMENNAME).toString();
               tUnitSpecimen.setSpecimenName(strValue);
               
               strValue = ((JsonMap) map).get(PLANTDATE).toString();
               tUnitSpecimen.setPlantDate(strValue);

               strValue = ((JsonMap) map).get(TRIALUNITID).toString();
               tUnitSpecimen.setTrialUnitId(strValue);
               
               strValue = ((JsonMap) map).get(HASDIED).toString();
               tUnitSpecimen.setHasDied(strValue);
               
               strValue = ((JsonMap) map).get(ITEMID).toString();
               tUnitSpecimen.setItemId(strValue);
			   
               strValue = ((JsonMap) map).get(SPECIMENID).toString();
               tUnitSpecimen.setSpecimenId(strValue);
               
               strValue = ((JsonMap) map).get(NOTES).toString();
               tUnitSpecimen.setNotes(strValue);
               
               strValue = ((JsonMap) map).get(HARVESTDATE).toString();
               tUnitSpecimen.setHarvestDate(strValue);
               
               strValue = ((JsonMap) map).get(TRIALUNITSPECIMENID).toString();
               tUnitSpecimen.setTrialUnitSpecimenId(strValue);
               
		   }	
			
		}
		
		List<TrialUnitSpecimen> lSpecimens = new ArrayList<TrialUnitSpecimen>();
		lSpecimens.add(tUnitSpecimen);
		trialUnit.setSpecimen(lSpecimens);
		
		
		return trialUnit;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createGetQuery(String id, String filterClause)
			throws DalDbException {

		return BMSApiDataConnection.getTrialUnit();
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	
		
}