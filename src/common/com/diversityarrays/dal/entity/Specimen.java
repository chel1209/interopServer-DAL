package com.diversityarrays.dal.entity;

import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="Specimen")
@EntityTag("Specimen")
public class Specimen extends DalEntity{

	@Column(name="SpecimenName")
	private String specimenName;
	
	@Column(name="SpecimenId")
	private int specimenId;	
	
	@Column(name="HarvestDate")
	private Date harvestDate;

	@Column(name="PlanttDate")
	private Date plantDate;
	
	public String toString(){
		if(specimenName == null){
			specimenName = "null ";
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String harvestDateString;
		String plantDateString; 
		
		if(harvestDate!=null){
			harvestDateString = format.format(harvestDate);
		}else{
			harvestDateString = "null";
		}
		
		if(plantDate!=null){
			plantDateString = format.format(plantDate);
		}else{
			plantDateString = "null";
		}
		String string = " { SpecimenName: \"" + specimenName + "\", SpecimenId: \"" + specimenId + "\", HarvestDate: \"" + harvestDateString + "\", PlantDate: " + plantDateString + "\"} ";
		return string;
	}

	/**
	 * @return the spcecimenName
	 */
	public String getSpecimenName() {
		return specimenName;
	}

	/**
	 * @param spcecimenName the spcecimenName to set
	 */
	public void setSpecimenName(String specimenName) {
		this.specimenName = specimenName;
	}

	/**
	 * @return the spcecimenId
	 */
	public int getSpecimenId() {
		return specimenId;
	}

	/**
	 * @param spcecimenId the spcecimenId to set
	 */
	public void setSpecimenId(int specimenId) {
		this.specimenId = specimenId;
	}

	/**
	 * @return the harvestDate
	 */
	public Date getHarvestDate() {
		return harvestDate;
	}

	/**
	 * @param harvestDate the harvestDate to set
	 */
	public void setHarvestDate(Date harvestDate) {
		this.harvestDate = harvestDate;
	}

	/**
	 * @return the plantDate
	 */
	public Date getPlantDate() {
		return plantDate;
	}

	/**
	 * @param plantDate the plantDate to set
	 */
	public void setPlantDate(Date plantDate) {
		this.plantDate = plantDate;
	}	

			
}