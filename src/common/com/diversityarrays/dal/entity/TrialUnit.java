package com.diversityarrays.dal.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="TrialUnit")
@EntityTag("TrialUnit")
public class TrialUnit extends DalEntity{

	@Column(name="TrialUnitNote", length=(6000))
	private String trialUnitNote;
	
	@Column(name="SiteName", nullable=false, length=(30))
	private String siteName;
	
	@Column(name="SiteId", nullable=false)
	private int siteId;	
	
	@Column(name="UnitPositionText", length=(6000))
	private String unitPositionText;
	
	@Column(name="TreatmentId")
	private Integer treatmentId;
	
	@Column(name="TrialId")
	private Integer trialId;
	
	private Specimen specimen;
	
	public String toString(){
		return "{ TrialId: \"" + trialId + "\", TrialUnitNote: \"" + trialUnitNote + "\", SiteName: \"" + siteName + "\", SiteId: \""+ siteId +  "\", UnitPositionText: \"" + unitPositionText + "\", Specimen: \"" + specimen.toString() + "\"}";
	}

	/**
	 * @return the trialUnitNote
	 */
	public String getTrialUnitNote() {
		return trialUnitNote;
	}

	/**
	 * @param trialUnitNote the trialUnitNote to set
	 */
	public void setTrialUnitNote(String trialUnitNote) {
		this.trialUnitNote = trialUnitNote;
	}

	/**
	 * @return the siteName
	 */
	public String getSiteName() {
		return siteName;
	}

	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	/**
	 * @return the siteId
	 */
	public int getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the unitPositionText
	 */
	public String getUnitPositionText() {
		return unitPositionText;
	}

	/**
	 * @param unitPositionText the unitPositionText to set
	 */
	public void setUnitPositionText(String unitPositionText) {
		this.unitPositionText = unitPositionText;
	}

	/**
	 * @return the treatmentId
	 */
	public Integer getTreatmentId() {
		return treatmentId;
	}

	/**
	 * @param treatmentId the treatmentId to set
	 */
	public void setTreatmentId(Integer treatmentId) {
		this.treatmentId = treatmentId;
	}

	/**
	 * @return the trialId
	 */
	public Integer getTrialId() {
		return trialId;
	}

	/**
	 * @param trialId the trialId to set
	 */
	public void setTrialId(Integer trialId) {
		this.trialId = trialId;
	}

	/**
	 * @return the specimens
	 */
	public Specimen getSpecimens() {
		return specimen;
	}

	/**
	 * @param specimens the specimens to set
	 */
	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}
			
}

