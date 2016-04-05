package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 
 * @author Raul Hernandez T.
 * @date 3-10-2016
 *
 */

@Table(name="Specimen")
@EntityTag("Specimen")
public class TrialUnitSpecimen {
	
	@Column(name="SpecimenName")
	private String specimenName;
	
	@Column(name="PlantDate")
	private String plantDate;
	
	@Column(name="TrialUnitId")
	private String trialUnitId;
	
	@Column(name="HasDied")
	private String hasDied;
	
	@Column(name="ItemId")
	private String itemId;
	
	@Column(name="SpecimenId")
	private String specimenId;
	
	@Column(name="Notes")
	private String notes;
	
	@Column(name="HarvestDate")
	private String harvestDate;
	
	@Column(name="TrialUnitSpecimenId")
	private String trialUnitSpecimenId;

	public String getSpecimenName() {
		return specimenName;
	}

	public void setSpecimenName(String specimenName) {
		this.specimenName = specimenName;
	}

	public String getPlantDate() {
		return plantDate;
	}

	public void setPlantDate(String plantDate) {
		this.plantDate = plantDate;
	}

	public String getTrialUnitId() {
		return trialUnitId;
	}

	public void setTrialUnitId(String trialUnitId) {
		this.trialUnitId = trialUnitId;
	}

	public String getHasDied() {
		return hasDied;
	}

	public void setHasDied(String hasDied) {
		this.hasDied = hasDied;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getSpecimenId() {
		return specimenId;
	}

	public void setSpecimenId(String specimenId) {
		this.specimenId = specimenId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getHarvestDate() {
		return harvestDate;
	}

	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}

	public String getTrialUnitSpecimenId() {
		return trialUnitSpecimenId;
	}

	public void setTrialUnitSpecimenId(String trialUnitSpecimenId) {
		this.trialUnitSpecimenId = trialUnitSpecimenId;
	}
		
}
