package com.diversityarrays.dal.db;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;

import com.diversityarrays.dal.entity.DalEntity;

public class BufferedReaderEntityIterator<T extends DalEntity> implements EntityIterator<T>,
		Closeable {

	private final BufferedReader webserviceResultReader;
	private final EntityFactory<T> tFactory;
	private int index = 0;
	private String line = null;
	
	/**
	 * 
	 * @param rd
	 * @param tfactory
	 */
	public BufferedReaderEntityIterator(BufferedReader rd, EntityFactory<T> tfactory) {
		webserviceResultReader = rd;
		this.tFactory = tfactory;
	}	
	
	@Override
	public void close() throws IOException {
		if(webserviceResultReader != null){
			webserviceResultReader.close();
		}

	}
	
	public void readLine() throws DalDbException {
		try{
			line = webserviceResultReader.readLine();
		}catch(IOException ioex){
			throw new DalDbException("Error reading line: " + ioex);
		}catch(Exception ioex){
			throw new DalDbException("Error reading line: " + ioex);
		}
	}

	@Override
	public T nextEntity() throws DalDbException {
		T result = null;
		
		try{
			if(line != null){
				JsonParser parser = new JsonParser(line);
				if(parser.getMapResult()!=null){
					result = tFactory.createEntity(parser.getMapResult());
				}else{
					List<Object> list = parser.getListResult();
					if(index < list.size()){
						result = tFactory.createEntity((JsonMap)list.get(index));
						index++;
						return result;
					}else{
						index = 0;
						return result;
					}
				}
			}
		}catch(ParseException peex){
			throw new DalDbException("Error parsing json: " + peex);
		}catch(Exception ex){
			throw new DalDbException("Error iterating buffered reader: " + ex);
		}
		return result;
	}

}