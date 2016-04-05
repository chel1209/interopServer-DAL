package com.diversityarrays.dal.db.bms;


import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;

import net.pearcan.json.JsonMap;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.OutputFile;

/**
 * 
 * @author Raul Hernandez T.
 * @date 03-16-2016
 *
 */
public class TrialExportFactory implements SqlEntityFactory<OutputFile> {

	
	/**
	 * TODO: Acompletar el mapeo cuando se tenga la info 
	 */
	
	private static final String CSV = "csv";
	
	@Override
	public boolean isPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OutputFile createEntity(ResultSet rs) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputFile createEntity(JsonMap jsonMap) throws DalDbException {
		
		
		OutputFile outputFile = new OutputFile();
		List<Object> outputList = (List) jsonMap.get("OutputFile");
		String strValue="";
		
		if(outputList != null){
			for(Object map : outputList){
				strValue = ((JsonMap) map).get(CSV).toString();
				outputFile.setCsv(strValue);		
			}
		}
		
		return outputFile;
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
