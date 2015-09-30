package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;

public class SampleMeasurement extends DalEntity {
	
	@Id
	@Column(name="TrialUnitId", length=(6000))
	private Integer trialUnitId;
	
	
	@Column(name="SampleTypeId", nullable=false)
	private Integer sampleTypeId;
	
	@Column(name="Trait", nullable=false)
	private DAL_Trait trait;
	
	@Column(name="TraitValue", nullable=false)
	private String traitValue;
	
	static public final EntityColumn TRIAL_UNIT_ID = createEntityColumn(SampleMeasurement.class, "trialUnitId");
	static public final EntityColumn SAMPLE_TYPE_ID = createEntityColumn(SampleMeasurement.class, "sampleTypeId");
	static public final EntityColumn TRAIT_VALUE = createEntityColumn(SampleMeasurement.class, "traitValue");

	public String toString(){
		return " { TrialUnitId : \""+ trialUnitId + "\", SampleTypeId : \"" + sampleTypeId + "\", Trait : \"" + trait.toString() + "\", TraitValue : \"" + traitValue;
	}
	
	/**
	 * @return the trialUnitId
	 */
	public int getTrialUnitId() {
		return trialUnitId;
	}

	/**
	 * @param trialUnitId the trialUnitId to set
	 */
	public void setTrialUnitId(int trialUnitId) {
		this.trialUnitId = trialUnitId;
	}

	/**
	 * @return the sampleTypeId
	 */
	public int getSampleTypeId() {
		return sampleTypeId;
	}

	/**
	 * @param sampleTypeId the sampleTypeId to set
	 */
	public void setSampleTypeId(int sampleTypeId) {
		this.sampleTypeId = sampleTypeId;
	}

	/**
	 * @return the trait
	 */
	public DAL_Trait getTrait() {
		return trait;
	}

	/**
	 * @param trait the trait to set
	 */
	public void setTrait(DAL_Trait trait) {
		this.trait = trait;
	}

	/**
	 * @return the traitValue
	 */
	public String getTraitValue() {
		return traitValue;
	}

	/**
	 * @param traitValue the traitValue to set
	 */
	public void setTraitValue(String traitValue) {
		this.traitValue = traitValue;
	}
	
	

}
