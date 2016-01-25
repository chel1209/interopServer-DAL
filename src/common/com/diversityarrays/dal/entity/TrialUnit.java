package com.diversityarrays.dal.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="TrialUnit")
@EntityTag("TrialUnit")
public class TrialUnit extends DalEntity{

	@Id
	@Column(name="TrialUnitId", length=(6000))
	private int trialUnitId;
	
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
	
	@Column(name="Specimen")
	private Specimen specimen;
	
	@Column(name="ReplicateNumber")
	private Integer replicateNumber;
	
	@Column(name="SampleMeasurement")
	private List<SampleMeasurement> sampleMeasurements;
	
	@Column(name="UnitPositionId")
	private Integer unitPositionId;
	
	public String toString(){
		String result = null;
		if(sampleMeasurements!=null && sampleMeasurements.size()>0){
			for(SampleMeasurement sampleMeasurement:sampleMeasurements){
				result = "{ TrialUnitId: \""+ trialUnitId + "\"  TrialId: \"" + trialId + "\", TrialUnitNote: \"" + trialUnitNote + "\", SiteName: \"" + siteName + "\", SiteId: \""+ siteId +  "\", UnitPositionText: \"" + unitPositionText + "\", Specimen: \"" + specimen.toString() + "\", SampleMeasurement : \""+ sampleMeasurement.toString() +"\", ReplicateNumber :\""+ replicateNumber + "\" }";
			}
		}else {
			if(specimen!=null){
				result = "{ TrialUnitId: \""+ trialUnitId + "\" TrialId: \"" + trialId + "\", TrialUnitNote: \"" + trialUnitNote + "\", SiteName: \"" + siteName + "\", SiteId: \""+ siteId +  "\", UnitPositionText: \"" + unitPositionText + "\", Specimen: \"" + specimen.toString() + "\", ReplicateNumber: \""+ replicateNumber + "\"}";
			}else{
				result = "{ TrialUnitId: \""+ trialUnitId + "\" TrialId: \"" + trialId + "\", TrialUnitNote: \"" + trialUnitNote + "\", SiteName: \"" + siteName + "\", SiteId: \""+ siteId +  "\", UnitPositionText: \"" + unitPositionText + "\", ReplicateNumber: \""+ replicateNumber + "\"}";
			}
		}
		return result;
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

	/**
	 * @return the sampleMeasurements
	 */
	public List<SampleMeasurement> getSampleMeasurements() {
		return sampleMeasurements;
	}

	/**
	 * @param sampleMeasurements the sampleMeasurements to set
	 */
	public void setSampleMeasurements(List<SampleMeasurement> sampleMeasurements) {
		this.sampleMeasurements = sampleMeasurements;
	}

	/**
	 * @return the specimen
	 */
	public Specimen getSpecimen() {
		return specimen;
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
	 * @return the replicateNumber
	 */
	public Integer getReplicateNumber() {
		return replicateNumber;
	}

	/**
	 * @param replicateNumber the replicateNumber to set
	 */
	public void setReplicateNumber(Integer replicateNumber) {
		this.replicateNumber = replicateNumber;
	}

	/**
	 * @return the unitPositionId
	 */
	public Integer getUnitPositionId() {
		return unitPositionId;
	}

	/**
	 * @param unitPositionId the unitPositionId to set
	 */
	public void setUnitPositionId(Integer unitPositionId) {
		this.unitPositionId = unitPositionId;
	}
			
}

