package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Genotype;
import com.diversityarrays.dal.entity.Specimen;
import com.diversityarrays.dal.entity.TrialTrait;
import com.diversityarrays.dal.entity.TrialUnit;

/**
 * 
 * @author Raul Hernandez T.
 * @date   17-DEC-2015
 * 
 * @note At the moment to write this class, it is not possible to know
 *       which record from the "names: section correspond to selection history
 *       and which one correspond to germplasm name.
 *       
 *       As a desing decision, the first row found is going to be assigned
 *       to selection history and the next one to germplasm name.
 *       
 *       When the BMS API team fix this issue, it is necessary to modify
 *       the mapping 
 *       
 * @note At the moment to write this class, it is not possible to get the 
 *       breeging_method_id. As a design decision, it was set to 1.        
 *
 */

public class SpecimenFactory implements SqlEntityFactory<Specimen> {
	
	
	private static final String   BREEDING_METHOD_NAME = "breedingMethod";
    private static final String   IS_ACTIVE            = ""; 
    private static final String   BREEDING_METHOD_ID   = "";
    private static final String   SPECIMEN_BARCODE     = "";
    private static final String   SPECIMEN_ID          = "germplasmId";
    private static final String   FILIAL_GENERATION    = "";
    private static final String   PEDIGREE             = "pedigreeString";
    private static final String   genotype             = "";

    private static final String   UPDATE               = "";
    private static final String   delete               = "";
    private static final String   addGenotype          = "";
    

    private static final String   selectionHistory     = "";
    private static final String   specimenName         = "";
    
	public String getURL(String filterClause){
		return BMSApiDataConnection.getSpecimenCall(filterClause);
	}    

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Specimen createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @author Raul Hernandez T.
	 * @date   12/22/2015
	 * @note   La API BMS no identifica cual es la historia de seleccion o el nombre en
	 *         la respuesta del servicio. Se asigna el primer registro encontrado a la historia
	 *         de seleccion y el siguiente al nombre.
	 *         
	 *         Segun el equipo de LeafNode, la nueva version del API tendra dichos identificadores
	 *         por lo que es necesario cambiar este metodo para asignar el correcto.
	 */
	@Override
	public Specimen createEntity(JsonMap jsonMap) throws DalDbException {
		
		Specimen specimen = new Specimen();
		
		specimen.setAddGenotype("");
		specimen.setBreedingMethodId(1);
		specimen.setBreedingMethodName((String)jsonMap.get(BREEDING_METHOD_NAME));
		specimen.setDelete("");
		specimen.setFilialGeneration(0);
		specimen.setGenotype(new Genotype());
		specimen.setIsActive(1);
		specimen.setPedigree((String)jsonMap.get(PEDIGREE));
		specimen.setSpecimenBarcode("");
		specimen.setSpecimenId((String)jsonMap.get(SPECIMEN_ID));
		specimen.setUpdate("");
		
		List<String> nec = (ArrayList<String>) jsonMap.get("names");
		
		if(nec.size() > 1){
			specimen.setSelectionHistory(nec.get(0));
			specimen.setSpecimenName(nec.get(1));			
		}else 
		if(nec.size() < 2){
			specimen.setSelectionHistory(nec.get(0));
			specimen.setSpecimenName("");
		}else if (nec.size()==0){
			specimen.setSelectionHistory("");
			specimen.setSpecimenName("");
		}
		
		return specimen;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause) throws DalDbException {
		
		BMSApiDataConnection.getSpecimenCall(filterClause);
		return null;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void createEntity(TrialUnit trialUnit, JsonMap jsonMap,SampleMeasurementFactory sampleMeasurementFactory, List<TrialTrait> trialTraits, String sampleMeasurementURL)
			throws DalDbException {        
		
		List<Object> observations = sampleMeasurementFactory.getObservationsMap(sampleMeasurementURL);

	    List<Object> germplasm = (List) jsonMap.get("germplasm");
	    for (Object map : germplasm) {
	    	Specimen result = new Specimen();
            if (Integer.valueOf((String) ((JsonMap) map).get("entryNumber")) == Integer.valueOf(trialUnit.getUnitPositionText())) {
                result.setSpecimenName((String) ((JsonMap) map).get("designation"));
                result.setSpecimenId((String) ((JsonMap) map).get("gid"));
                trialUnit.setSpecimen(result);
                
                if(observations!=null&&observations.size()>0){
                    for(Object observationsMap : observations)
                        if(Integer.valueOf((String)((JsonMap)observationsMap).get("entryNumber")) == Integer.valueOf(trialUnit.getUnitPositionText())){
                            sampleMeasurementFactory.createEntity(trialUnit,(JsonMap)observationsMap, trialTraits);
                            break;
                        }
                }
                
                break;
            }
        }
	        
}	


}