package com.diversityarrays.dal.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;


/*
 * author: Raul Hernandez T.
 * date:   08-31-2015
 */

@Table(name="Observation")
@EntityTag("Observation")
public class Observation extends DalEntity{
	
	@Column(name="uniqueIdentifier")
	private Integer 		   uniqueIdentifier;
	@Column(name="germplasmId")
	private Integer 		   germplasmId;
	@Column(name="germplasmDesignation")
	private String  		   germplasmDesignation;
	@Column(name="entryNumber")
	private Integer 		   entryNumber;
	@Column(name="entryType")
	private String  		   entryType;
	@Column(name="plotNumber")
	private Integer 		   plotNumber;
	@Column(name="replicationNumber")
	private Integer 	   	   replicationNumber;
	@Column(name="environmentNumber")
	private Integer 		   environmentNumber;
	@Column(name="seedSource")
	private String  		   seedSource;
	@Column(name="measurements")
	private List<Measurements> measurements;
	
	public Observation(){}
	
	public Observation(Integer uniqueIdentifier,     Integer germplasmId,
			           String  germplasmDesignation, Integer entryNumber, String entryType,
			           Integer plotNumber,           Integer replicationNumber,
			           Integer environmentNumber,    String seedSource,
			           List<Measurements> measurements) {

		this.uniqueIdentifier = uniqueIdentifier;
		this.germplasmId = germplasmId;
		this.germplasmDesignation = germplasmDesignation;
		this.entryNumber = entryNumber;
		this.entryType = entryType;
		this.plotNumber = plotNumber;
		this.replicationNumber = replicationNumber;
		this.environmentNumber = environmentNumber;
		this.seedSource = seedSource;
		this.measurements = measurements;
	}
	
	public Integer getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	public void setUniqueIdentifier(Integer uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	public Integer getGermplasmId() {
		return germplasmId;
	}
	public void setGermplasmId(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}
	public String getGermplasmDesignation() {
		return germplasmDesignation;
	}
	public void setGermplasmDesignation(String germplasmDesignation) {
		this.germplasmDesignation = germplasmDesignation;
	}
	public Integer getEntryNumber() {
		return entryNumber;
	}
	public void setEntryNumber(Integer entryNumber) {
		this.entryNumber = entryNumber;
	}
	public String getEntryType() {
		return entryType;
	}
	public void setEntryType(String entryType) {
		this.entryType = entryType;
	}
	public Integer getPlotNumber() {
		return plotNumber;
	}
	public void setPlotNumber(Integer plotNumber) {
		this.plotNumber = plotNumber;
	}
	public Integer getReplicationNumber() {
		return replicationNumber;
	}
	public void setReplicationNumber(Integer replicationNumber) {
		this.replicationNumber = replicationNumber;
	}
	public Integer getEnvironmentNumber() {
		return environmentNumber;
	}
	public void setEnvironmentNumber(Integer environmentNumber) {
		this.environmentNumber = environmentNumber;
	}
	public String getSeedSource() {
		return seedSource;
	}
	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}
	public List<Measurements> getMeasurements() {
		return measurements;
	}
	public void setMeasurements(List<Measurements> measurements) {
		this.measurements = measurements;
	}
	
	@Override
	public String toString(){
		
		
		String print =  "uniqueIdentifier:     " + uniqueIdentifier     + "\n";
		       print += "germplasmId:          " + germplasmId          + "\n";
		       print += "germplasmDesignation: " + germplasmDesignation + "\n";
		       print += "entryNumber:          " + entryNumber          + "\n";
		       print += "entryType:            " + entryType            + "\n";
		       print += "plotNumber:           " + plotNumber           + "\n";
		       print += "replicationNumber:    " + replicationNumber    + "\n";
		       print += "environmentNumber:    " + environmentNumber    + "\n";
		       print += "seedSource:           " + seedSource           + "\n";
		       
		       for(Measurements m : this.measurements){
		    	   print += "MeasurementValue:     " + m.getMeasurementValue() + "\n";
		    	   
		    	   MeasurementIdentifier mIdentifier = m.getMeasurementIdentifier();
		    	   print += "MeasurementId:        " + mIdentifier.getMeasurementId() + "\n";
		    	   
		    	   Trait trait = mIdentifier.getTrait();
		    	   
		    	   print += "TraitId:              " + trait.getTraitId() + "\n";
		    	   print += "TraitName:            " + trait.getTraitName() + "\n";
		    	   
		       }
		       
		return print;
	}
	
	
}