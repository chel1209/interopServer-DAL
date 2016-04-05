package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Traits;


/**
 * 
 * @author Raul Hernandez T.
 * @date 03-14-2016
 *
 */

public class TraitsFactory implements SqlEntityFactory<Traits>{
	
	
	private static final String OTHERPERMISSION        = "OtherPermission";
	private static final String TRAITUNITNAME          = "TraitUnitName";
	private static final String TRAITVALRULEERRMSG     = "TraitValRuleErrMsg";
	private static final String ACCESSGROUPNAME        = "AccessGroupName";
	private static final String ACCESSGROUPID          = "AccessGroupId";
	private static final String TRAITGROUPTYPEID       = "TraitGroupTypeId";
	private static final String ULTIMATEPERMISSION     = "UltimatePermission";
	private static final String TRAITDESCRIPTION       = "TraitDescription";
	private static final String CHGPERM                = "chgPerm";
	private static final String ACCESSGROUPPERMISSION  = "AccessGroupPermission";
	private static final String ULTIMATEPERM           = "UltimatePerm";
	private static final String TRAITDATATYPE          = "TraitDataType";
	private static final String ACCESSGROUPPERM        = "AccessGroupPerm";
	private static final String OWNGROUPPERMISSION     = "OwnGroupPermission";
	private static final String UPDATE                 = "update";
	private static final String TRAITUNIT              = "TraitUnit";
	private static final String OTHERPERM              = "OtherPerm";
	private static final String OWNGROUPNAME           = "OwnGroupName";
	private static final String OWNGROUPPERM           = "OwnGroupPerm";
	private static final String TRAITID                = "TraitId";
	private static final String TRAITVALUEMAXLENGTH    = "TraitValueMaxLength";
	private static final String CHGOWNER               = "chgOwner";
	private static final String TRAITCAPTION           = "TraitCaption";
	private static final String TRAITVALRULE           = "TraitValRule";
	private static final String ADDALIAS               = "addAlias";
	private static final String TRAITNAME              = "TraitName";
	private static final String ISTRAITUSEDFORANALYSIS = "IsTraitUsedForAnalysis";
	private static final String OWNGROUPID             = "OwnGroupId";
	private static final String DELETE                 = "delete";
	

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Traits createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Traits createEntity(JsonMap jsonMap) throws DalDbException {

        Traits traits = new Traits();
		
        //no mapping yet
        traits.setOtherPermission((String) jsonMap.get(OTHERPERMISSION));
        //no mapping yet
        traits.setTraitUnitName((String) jsonMap.get(TRAITUNITNAME));
        //no mapping yet
        traits.setTraitValRuleErrMsg((String) jsonMap.get(TRAITVALRULEERRMSG));
        //no mapping yet
        traits.setAccessGroupName((String) jsonMap.get(ACCESSGROUPNAME));
        //no mapping yet
        traits.setAccessGroupId((String) jsonMap.get(ACCESSGROUPID));
        //no mapping yet
        traits.setTraitGroupTypeId((String) jsonMap.get(TRAITGROUPTYPEID));
        //no mapping yet
        traits.setUltimatePermission((String) jsonMap.get(ULTIMATEPERMISSION));
        //no mapping yet
        traits.setTraitDescription((String) jsonMap.get(TRAITDESCRIPTION));
        //no mapping yet
        traits.setChgPerm((String) jsonMap.get(CHGPERM));
        //no mapping yet
        traits.setAccessGroupPermission((String) jsonMap.get(ACCESSGROUPPERMISSION));
        //no mapping yet
        traits.setUltimatePerm((String) jsonMap.get(ULTIMATEPERM));
        //no mapping yet
        traits.setTraitDataType((String) jsonMap.get(TRAITDATATYPE));
        //no mapping yet
        traits.setAccessGroupPerm((String) jsonMap.get(ACCESSGROUPPERM));
        //no mapping yet
        traits.setOwnGroupPermission((String) jsonMap.get(OWNGROUPPERMISSION));
        //no mapping yet
        traits.setUpdate((String) jsonMap.get(UPDATE));
        //no mapping yet
        traits.setTraitUnit((String) jsonMap.get(TRAITUNIT));
        //no mapping yet
        traits.setOtherPerm((String) jsonMap.get(OTHERPERM));
        //no mapping yet
        traits.setOwnGroupName((String) jsonMap.get(OWNGROUPNAME));
        //no mapping yet
        traits.setOwnGroupPerm((String) jsonMap.get(OWNGROUPPERM));
        //no mapping yet
        traits.setTraitId((String) jsonMap.get(TRAITID));
        //no mapping yet
        traits.setTraitValueMaxLength((String) jsonMap.get(TRAITVALUEMAXLENGTH));
        //no mapping yet
        traits.setChgOwner((String) jsonMap.get(CHGOWNER));
        //no mapping yet
        traits.setTraitCaption((String) jsonMap.get(TRAITCAPTION));
        //no mapping yet
        traits.setTraitValRule((String) jsonMap.get(TRAITVALRULE));
        //no mapping yet
        traits.setAddAlias((String) jsonMap.get(ADDALIAS));
        //no mapping yet
        traits.setTraitName((String) jsonMap.get(TRAITNAME));
        //no mapping yet
        traits.setIsTraitUsedForAnalysis((String) jsonMap.get(ISTRAITUSEDFORANALYSIS));
        //no mapping yet
        traits.setOwnGroupId((String) jsonMap.get(OWNGROUPID));
        //no mapping yet
        traits.setDelete((String) jsonMap.get(DELETE));
        
		return traits;
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
