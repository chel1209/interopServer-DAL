package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.GeneralUnit;

/**
 * 
 * @author Raul Hernandez T.
 * @date 03-08-2016
 *
 */
public class GeneralUnitFactory implements SqlEntityFactory<GeneralUnit>{
	
	/**
	 * TODO: Acompletar el mapeo cuando se tenga la info de BMS
	 */
	private static final String USEBYLAYERATTRIB = "";
	private static final String UnitTypeName     = "";
	private static final String USEBYTRAIT       = "";
	private static final String USEBYTRIALEVENT  = "";
	private static final String USEBYITEM        = "";
	private static final String UNITTYPEID       = "";
	private static final String UPDATE           = "";
	private static final String UNITNAME         = "";
	private static final String UNITNOTE         = "";
	private static final String UNITSOURCE       = "";
	private static final String UNITID           = "";

	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GeneralUnit createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralUnit createEntity(JsonMap jsonMap) throws DalDbException {

        GeneralUnit generalUnit = new GeneralUnit();
        
        generalUnit.setUseBylayerattrib("0");
        generalUnit.setUnitTypeName("null");
        generalUnit.setUseByTrait("0");
        generalUnit.setUseByTrialEvent("0");
        generalUnit.setUseByItem("0");
        generalUnit.setUnitTypeId("null");
        generalUnit.setUpdate("update/generalunit/24");
        generalUnit.setUnitName("U_5646096");
        generalUnit.setUnitNote("null");
        generalUnit.setUnitSource("null");
        generalUnit.setUnitId("24");
        
        
		return generalUnit;
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
		
		return BMSApiDataConnection.getGeneralUnit();
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
