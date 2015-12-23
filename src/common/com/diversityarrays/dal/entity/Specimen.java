package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author Raul Hernandez T.
 * @date   17-DIC-2015
 * 
 * @note Re-define the class properties according to list/specimen/_nperpage/page/_num call.
 *       from kddart.org
 *
 */
@Table(name="Specimen")
@EntityTag("Specimen")
public class Specimen extends DalEntity{

	@Column(name="breedingMethodName")
	private String   breedingMethodName;
	
	@Column(name="isActive")
    private Integer  isActive;
	
	@Column(name="breedingMethodId")
    private Integer  breedingMethodId;
	
	@Column(name="specimenBarcode")
    private String   specimenBarcode;
	
	@Column(name="specimenId")
    private String   specimenId;
	
	@Column(name="filialGeneration")
    private Integer  filialGeneration;
	
	@Column(name="update")
    private String   update;
	
	@Column(name="pedigree")
    private String   pedigree;
	
	@Column(name="specimenName")
    private String   specimenName;
	
	@Column(name="genotype")
    private Genotype genotype;
	
	@Column(name="selectionHistory")
    private String   selectionHistory;
	
	@Column(name="delete")
    private String   delete;
	
	@Column(name="addGenotype")
    private String   addGenotype;
	
	public Specimen(){}
	
	public String getBreedingMethodName() {
		return breedingMethodName;
	}
	public void setBreedingMethodName(String breedingMethodName) {
		this.breedingMethodName = breedingMethodName;
	}
	public Integer getIsActive() {
		return isActive;
	}
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}
	public Integer getBreedingMethodId() {
		return breedingMethodId;
	}
	public void setBreedingMethodId(Integer breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}
	public String getSpecimenBarcode() {
		return specimenBarcode;
	}
	public void setSpecimenBarcode(String specimenBarcode) {
		this.specimenBarcode = specimenBarcode;
	}
	public String getSpecimenId() {
		return specimenId;
	}
	public void setSpecimenId(String specimenId) {
		this.specimenId = specimenId;
	}
	public Integer getFilialGeneration() {
		return filialGeneration;
	}
	public void setFilialGeneration(Integer filialGeneration) {
		this.filialGeneration = filialGeneration;
	}
	public String getUpdate() {
		return update;
	}
	public void setUpdate(String update) {
		this.update = update;
	}
	public String getPedigree() {
		return pedigree;
	}
	public void setPedigree(String pedigree) {
		this.pedigree = pedigree;
	}
	public String getSpecimenName() {
		return specimenName;
	}
	public void setSpecimenName(String specimenName) {
		this.specimenName = specimenName;
	}
	public Genotype getGenotype() {
		return genotype;
	}
	public void setGenotype(Genotype genotype) {
		this.genotype = genotype;
	}
	public String getSelectionHistory() {
		return selectionHistory;
	}
	public void setSelectionHistory(String selectionHistory) {
		this.selectionHistory = selectionHistory;
	}
	public String getDelete() {
		return delete;
	}
	public void setDelete(String delete) {
		this.delete = delete;
	}
	public String getAddGenotype() {
		return addGenotype;
	}
	public void setAddGenotype(String addGenotype) {
		this.addGenotype = addGenotype;
	}
    
}