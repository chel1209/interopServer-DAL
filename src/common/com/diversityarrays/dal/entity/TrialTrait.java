package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;

public class TrialTrait extends DalEntity{
	
	@Column(name="TraitName", length=(6000))
	private String traitName;
	
	@Id
	@Column(name="TraitID", nullable=false)	
	private int traitId;

	@Column(name="TrialID", nullable=false)
	private int trialId;
	
	public String toString(){
		return "{TraitID: \"" + traitId + "\", TrialID: \"" + trialId + "\", TraitName: \"" + traitName + "\"}"; 
	}
	
	/**
	 * @return the traitName
	 */
	public String getTraitName() {
		return traitName;
	}
	/**
	 * @param traitName the traitName to set
	 */
	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}
	/**
	 * @return the traitId
	 */
	public int getTraitId() {
		return traitId;
	}
	/**
	 * @param traitId the traitId to set
	 */
	public void setTraitId(int traitId) {
		this.traitId = traitId;
	}
	/**
	 * @return the trialId
	 */
	public int getTrialId() {
		return trialId;
	}
	/**
	 * @param trialId the trialId to set
	 */
	public void setTrialId(int trialId) {
		this.trialId = trialId;
	}
	
	

}
