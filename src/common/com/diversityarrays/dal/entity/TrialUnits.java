package com.diversityarrays.dal.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 
 * @author Raul Hernandez T.
 * @date 3-10-2016
 *
 */


@Table(name="TrialUnit")
@EntityTag("TrialUnit")
public class TrialUnits extends DalEntity{


	    @Column(name="UltimatePerm")
		private String ultimatePerm;
	    
	    @Column(name="addSpecimen")
		private String addSpecimen;
	    
	    @Column(name="UltimatePermission")
		private String ultimatePermission;
	    
	    @Column(name="TrialUnitBarcode")
		private String trialUnitBarcode;
	    
	    @Column(name="update")
		private String update;
	    
	    @Column(name="SourceTrialUnitId")
		private String sourceTrialUnitId;
	    
	    @Column(name="TrialUnitId")
		private String trialUnitId;
	    
	    @Column(name="Longitude")
		private String longitude;
	    
	    @Column(name="ReplicateNumber")
		private String replicateNumber;
	    
	    @Column(name="listSpecimen")
		private String listSpecimen;
	    
	    @Column(name="TrialUnitNote")
		private String trialUnitNote;
	    
	    @Column(name="TreatmentText")
		private String treatmentText;
	    
	    @Column(name="SiteId")
		private String siteId;
	    
	    @Column(name="TreatmentId")
		private String treatmentId;
	    
	    @Column(name="UnitPositionId")
		private String unitPositionId;
	    
	    @Column(name="SampleSupplierId")
		private String sampleSupplierId;
	    
	    @Column(name="TrialId")
		private String trialId;
	    
	    @Column(name="Latitude")
		private String latitude;
	    
	    @Column(name="UnitPositionText")
		private String unitPositionText;
	    
	    @Column(name="Specimen")
		List<TrialUnitSpecimen> specimen;
	    
	    @Column(name="SiteName")
	    private String siteName;

		public String getUltimatePerm() {
			return ultimatePerm;
		}

		public void setUltimatePerm(String ultimatePerm) {
			this.ultimatePerm = ultimatePerm;
		}

		public String getAddSpecimen() {
			return addSpecimen;
		}

		public void setAddSpecimen(String addSpecimen) {
			this.addSpecimen = addSpecimen;
		}

		public String getUltimatePermission() {
			return ultimatePermission;
		}

		public void setUltimatePermission(String ultimatePermission) {
			this.ultimatePermission = ultimatePermission;
		}

		public String getTrialUnitBarcode() {
			return trialUnitBarcode;
		}

		public void setTrialUnitBarcode(String trialUnitBarcode) {
			this.trialUnitBarcode = trialUnitBarcode;
		}

		public String getUpdate() {
			return update;
		}

		public void setUpdate(String update) {
			this.update = update;
		}

		public String getSourceTrialUnitId() {
			return sourceTrialUnitId;
		}

		public void setSourceTrialUnitId(String sourceTrialUnitId) {
			this.sourceTrialUnitId = sourceTrialUnitId;
		}

		public String getTrialUnitId() {
			return trialUnitId;
		}

		public void setTrialUnitId(String trialUnitId) {
			this.trialUnitId = trialUnitId;
		}

		public String getLongitude() {
			return longitude;
		}

		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}

		public String getReplicateNumber() {
			return replicateNumber;
		}

		public void setReplicateNumber(String replicateNumber) {
			this.replicateNumber = replicateNumber;
		}

		public String getListSpecimen() {
			return listSpecimen;
		}

		public void setListSpecimen(String listSpecimen) {
			this.listSpecimen = listSpecimen;
		}

		public String getTrialUnitNote() {
			return trialUnitNote;
		}

		public void setTrialUnitNote(String trialUnitNote) {
			this.trialUnitNote = trialUnitNote;
		}

		public String getTreatmentText() {
			return treatmentText;
		}

		public void setTreatmentText(String treatmentText) {
			this.treatmentText = treatmentText;
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public String getTreatmentId() {
			return treatmentId;
		}

		public void setTreatmentId(String treatmentId) {
			this.treatmentId = treatmentId;
		}

		public String getUnitPositionId() {
			return unitPositionId;
		}

		public void setUnitPositionId(String unitPositionId) {
			this.unitPositionId = unitPositionId;
		}

		public String getSampleSupplierId() {
			return sampleSupplierId;
		}

		public void setSampleSupplierId(String sampleSupplierId) {
			this.sampleSupplierId = sampleSupplierId;
		}

		public String getTrialId() {
			return trialId;
		}

		public void setTrialId(String trialId) {
			this.trialId = trialId;
		}

		public String getLatitude() {
			return latitude;
		}

		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}

		public String getUnitPositionText() {
			return unitPositionText;
		}

		public void setUnitPositionText(String unitPositionText) {
			this.unitPositionText = unitPositionText;
		}

		public List<TrialUnitSpecimen> getSpecimen() {
			return specimen;
		}

		public void setSpecimen(List<TrialUnitSpecimen> specimen) {
			this.specimen = specimen;
		}

		public String getSiteName() {
			return siteName;
		}

		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}
	    
}

