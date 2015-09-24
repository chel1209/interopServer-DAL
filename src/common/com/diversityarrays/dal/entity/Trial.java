package com.diversityarrays.dal.entity;

import java.sql.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="Trial")
@EntityTag("Trial")
public class Trial extends DalEntity{

	@Column(name="TrialNote", length=(6000))
	private String trialNote;
	
	@Column(name="TrialAcronym", nullable=false, length=(30))
	private String trialAcronym;
	
	@Column(name="TrialEndDate")
	private Date trialEndDate;
	
	@Column(name="TrialTypeId")
	private Integer trialTypeId;
	
	@Column(name="TrialStartDate")
	private Date trialStartDate;
	
	@Column(name="TrialName", nullable=false, length=(100))
	private String trialName;

	@Id
	@Column(name="TrialID", nullable=false)		
	private Integer trialId;

	@Column(name="TrialManagerId", nullable=false)		
	private Integer trialManagerId;
	
	@Column(name="TrialManagerName", nullable=false, length=(100))
	private String trialManagerName;

	@Column(name="DesignTypeName", nullable=false, length=(100))
	private String designTypeName;
	
	@Column(name="SiteName", nullable=false, length=(100))
	private String siteName;
	
	@Column(name="SiteNameId", nullable=false, length=(100))
	private Integer siteNameId;
	
	@Column(name="TrialTypeName", nullable=false, length=(100))
	private String trialTypeName;	
	
	@Column(name="TrialTraits", nullable=false, length=(100))
	private List<TrialTrait> trialTraits;
	
	@Column(name="TrialUnits", nullable=false, length=(100))
	private List<TrialUnit> trialUnits;	
	
	public static final EntityColumn TRIAL_NOTE = createEntityColumn(Trial.class, "trialNote");
	public static final EntityColumn TRIAL_NAME = createEntityColumn(Trial.class, "trialName");
	public static final EntityColumn TRIAL_ACRONYM = createEntityColumn(Trial.class, "trialAcronym");
	public static final EntityColumn TRIAL_ID = createEntityColumn(Trial.class, "trialId");
	public static final EntityColumn SITE_NAME_ID = createEntityColumn(Trial.class, "siteNameId");
		
	/**
	 * @return the trialNote
	 */
	public String getTrialNote() {
		return trialNote;
	}
	/**
	 * @param trialNote the trialNote to set
	 */
	public void setTrialNote(String trialNote) {
		this.trialNote = trialNote;
	}
	/**
	 * @return the trialAcronym
	 */
	public String getTrialAcronym() {
		return trialAcronym;
	}
	/**
	 * @param trialAcronym the trialAcronym to set
	 */
	public void setTrialAcronym(String trialAcronym) {
		this.trialAcronym = trialAcronym;
	}
	/**
	 * @return the trialEndDate
	 */
	public Date getTrialEndDate() {
		return trialEndDate;
	}
	/**
	 * @param trialEndDate the trialEndDate to set
	 */
	public void setTrialEndDate(Date trialEndDate) {
		this.trialEndDate = trialEndDate;
	}
	/**
	 * @return the trialTypeId
	 */
	public int getTrialTypeId() {
		return trialTypeId;
	}
	/**
	 * @param trialTypeId the trialTypeId to set
	 */
	public void setTrialTypeId(int trialTypeId) {
		this.trialTypeId = trialTypeId;
	}
	/**
	 * @return the trialStartDate
	 */
	public Date getTrialStartDate() {
		return trialStartDate;
	}
	/**
	 * @param trialStartDate the trialStartDate to set
	 */
	public void setTrialStartDate(Date trialStartDate) {
		this.trialStartDate = trialStartDate;
	}
	/**
	 * @return the trialName
	 */
	public String getTrialName() {
		return trialName;
	}
	/**
	 * @param trialName the trialName to set
	 */
	public void setTrialName(String trialName) {
		this.trialName = trialName;
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
	/**
	 * @return the trialManagerName
	 */
	public String getTrialManagerName() {
		return trialManagerName;
	}
	/**
	 * @param trialManagerName the trialManagerName to set
	 */
	public void setTrialManagerName(String trialManagerName) {
		this.trialManagerName = trialManagerName;
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
	 * @param trialTypeId the trialTypeId to set
	 */
	public void setTrialTypeId(Integer trialTypeId) {
		this.trialTypeId = trialTypeId;
	}
	/**
	 * @return the trialManagerId
	 */
	public Integer getTrialManagerId() {
		return trialManagerId;
	}
	/**
	 * @param trialManagerId the trialManagerId to set
	 */
	public void setTrialManagerId(Integer trialManagerId) {
		this.trialManagerId = trialManagerId;
	}
	/**
	 * @return the trialTypeName
	 */
	public String getTrialTypeName() {
		return trialTypeName;
	}
	/**
	 * @param trialTypeName the trialTypeName to set
	 */
	public void setTrialTypeName(String trialTypeName) {
		this.trialTypeName = trialTypeName;
	}
	/**
	 * @return the trialTratis
	 */
	public List<TrialTrait> getTrialTraits() {
		return trialTraits;
	}
	/**
	 * @param trialTratis the trialTratis to set
	 */
	public void setTrialTraits(List<TrialTrait> trialTraits) {
		this.trialTraits = trialTraits;
	}
	/**
	 * @return the trialUnits
	 */
	public List<TrialUnit> getTrialUnits() {
		return trialUnits;
	}
	/**
	 * @param trialUnits the trialUnits to set
	 */
	public void setTrialUnits(List<TrialUnit> trialUnits) {
		this.trialUnits = trialUnits;
	}
	/**
	 * @return the designTypeName
	 */
	public String getDesignTypeName() {
		return designTypeName;
	}
	/**
	 * @param designTypeName the designTypeName to set
	 */
	public void setDesignTypeName(String designTypeName) {
		this.designTypeName = designTypeName;
	}
	/**
	 * @return the siteNameID
	 */
	public Integer getSiteNameID() {
		return siteNameId;
	}
	/**
	 * @param siteNameID the siteNameID to set
	 */
	public void setSiteNameID(Integer siteNameID) {
		this.siteNameId = siteNameID;
	}
		
}

