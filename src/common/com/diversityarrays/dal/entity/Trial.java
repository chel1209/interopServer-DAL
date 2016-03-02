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
	private Integer trialID;

	@Column(name="TrialManagerId", nullable=false)		
	private Integer trialManagerId;
	
	@Column(name="TrialManagerName", nullable=false, length=(100))
	private String trialManagerName;

	@Column(name="DesignTypeName", nullable=false, length=(100))
	private String designTypeName;
	
	@Column(name="SiteName", nullable=false, length=(100))
	private String siteName;
	
	@Column(name="SiteId", nullable=false, length=(100))
	private Integer siteId;
	
	@Column(name="TrialTypeName", nullable=false, length=(100))
	private String trialTypeName;
	
	@Column(name="DesignTypeId", nullable=false, length=(100))
	private Integer designTypeId;
	
	@Column(name="CurrentWorkflowId", nullable=false, length=(100))
	private Integer currentWorkflowId;

	@Column(name="OwnGroupPermission", nullable=false, length=(100))
	private String ownGroupPermission;
	
	@Column(name="listTrialUnit", nullable=false, length=(100))
	private String listTrialUnit;
	
	@Column(name="AccessGroupId", nullable=false, length=(100))
	private Integer accessGroupId;
	
	@Column(name="AcessGroupName", nullable=false, length=(100))
	private String accessGroupName;
	
	@Column(name="AccessGroupPermission", nullable=false, length=(100))
	private String accessGroupPermission;
	
	@Column(name="AccessGroupPerm", nullable=false, length=(100))
	private Integer accessGroupPerm;
	
	@Column(name="map", nullable=false, length=(100))
	private String map;
	
	@Column(name="addTrait", nullable=false, length=(100))
	private String addTrait;
	
	@Column(name="OwnGroupId", nullable=false, length=(100))
	private Integer ownGroupId;
	
	@Column(name="triallocation", nullable=false, length=(100))
	private String trialLocation;
	
	@Column(name="OwnGroupPerm", nullable=false, length=(100))
	private Integer ownGroupPerm;
	
	@Column(name="OwnGroupName", nullable=false, length=(100))
	private String ownGroupName;
	
	@Column(name="Longitude", nullable=false, length=(100))
	private Double longitude;
	
	@Column(name="update", nullable=false, length=(100))
	private String update;
	
	@Column(name="TrialNumber", nullable=false, length=(100))
	private Integer trialNumber;
	
	@Column(name="UltimatePerm", nullable=false, length=(100))
	private Integer ultimatePerm;
	
	@Column(name="chgPerm", nullable=false, length=(100))
	private String chgPerm;
	
	@Column(name="UltimatePermission", nullable=false, length=(100))
	private String ultimatePermission;
	
	@Column(name="OtherPermission", nullable=false, length=(100))
	private String otherPermission;
	
	@Column(name="ProjectName", nullable=true, length=(100))
	private String projectName;
	
	@Column(name="Latitude", nullable=false, length=(100))
	private Double latitude;
	
	@Column(name="chgOwner", nullable=false, length=(100))
	private String chgOwner;
	
	@Column(name="ProjectId", nullable=true, length=(100))
	private Integer projectId;
	
	@Column(name="OtherPerm", nullable=false, length=(100))
	private Integer otherPerm;
	
	/*@Column(name="TrialTraits", nullable=false, length=(100))
	private List<TrialTrait> trialTraits;*/
	
	/*@Column(name="TrialUnits", nullable=false, length=(100))
	private List<TrialUnit> trialUnits;*/	
	
	public static final EntityColumn TRIAL_NOTE = createEntityColumn(Trial.class, "trialNote");
	public static final EntityColumn TRIAL_NAME = createEntityColumn(Trial.class, "trialName");
	public static final EntityColumn TRIAL_ACRONYM = createEntityColumn(Trial.class, "trialAcronym");
	public static final EntityColumn TRIAL_ID = createEntityColumn(Trial.class, "trialID");
	public static final EntityColumn SITE_NAME_ID = createEntityColumn(Trial.class, "siteId");
	
	public Trial(){
		
		/*this.designTypeName        = trial.designTypeName;
		this.trialAcronym          = trial.trialAcronym;
		this.trialEndDate          = trial.trialEndDate;
		this.trialId               = trial.trialId;
		this.siteName              = trial.siteName;
		this.siteId                = trial.siteId;
		this.trialManagerId        = trial.trialManagerId;
		this.trialNote             = trial.trialNote;
		this.trialTypeId           = trial.trialTypeId;
		this.trialStartDate        = trial.trialStartDate;
		this.trialName             = trial.trialName;
		this.trialManagerName      = trial.trialManagerName;
		this.trialTypeName         = trial.trialTypeName;*/
		this.designTypeId          = 0;
		this.currentWorkflowId     = 0;
		this.ownGroupPermission    = "Read/Write/Link";
		this.listTrialUnit         = "trial/11/list/trialunit"; 
		this.accessGroupId         = 0;
		this.accessGroupName       = "admin";
		this.accessGroupPermission = "Read/Link";
		this.accessGroupPerm       = 5;
		this.map                   = "trial/11/on/map";
		this.addTrait              = "trial/11/add/trait";
		this.ownGroupId            = 0;
		this.trialLocation         = "POLYGON((149.092666904502 -35.3047819041308,149.092784921705 -35.3047819041308,149.09276882845 -35.3049745290585,149.092650811256 -35.3049745290585,149.092666904502 -35.3047819041308))";
		this.ownGroupPerm          = 7;
		this.ownGroupName          = "admin";
		this.longitude             = 149.092717866478;
		this.update                = "udpate/trial/11";
		this.trialNumber           = 1;
		this.ultimatePerm          = 7;
		this.chgPerm               = "trial/11/change/permission";
		this.ultimatePermission    = "Read/Write/Link";
		this.otherPermission       = "Read/Link";
		this.projectName           = null;
		this.latitude              = -35.3048782165934;
		this.chgOwner              = "trial/11/change/owner";
		this.projectId             = null;
		this.otherPerm             = 5;
		
	}
	
	public Trial(Trial trial){
		this.designTypeName        = trial.designTypeName;
		this.trialAcronym          = trial.trialAcronym;
		this.trialEndDate          = trial.trialEndDate;
		this.trialID               = trial.trialID;
		this.siteName              = trial.siteName;
		this.siteId                = trial.siteId;
		this.trialManagerId        = trial.trialManagerId;
		this.trialNote             = trial.trialNote;
		this.trialTypeId           = trial.trialTypeId;
		this.trialStartDate        = trial.trialStartDate;
		this.trialName             = trial.trialName;
		this.trialManagerName      = trial.trialManagerName;
		this.trialTypeName         = trial.trialTypeName;
		this.designTypeId          = trial.designTypeId;
		this.currentWorkflowId     = 0;
		this.ownGroupPermission    = "Read/Write/Link";
		this.listTrialUnit         = "trial/11/list/trialunit"; 
		this.accessGroupId         = 0;
		this.accessGroupName       = "admin";
		this.accessGroupPermission = "Read/Link";
		this.accessGroupPerm       = 5;
		this.map                   = "trial/11/on/map";
		this.addTrait              = "trial/11/add/trait";
		this.ownGroupId            = 0;
		this.trialLocation         = "POLYGON((149.092666904502 -35.3047819041308,149.092784921705 -35.3047819041308,149.09276882845 -35.3049745290585,149.092650811256 -35.3049745290585,149.092666904502 -35.3047819041308))";
		this.ownGroupPerm          = 7;
		this.ownGroupName          = "admin";
		this.longitude             = 149.092717866478;
		this.update                = "udpate/trial/11";
		this.trialNumber           = 1;
		this.ultimatePerm          = 7;
		this.chgPerm               = "trial/11/change/permission";
		this.ultimatePermission    = "Read/Write/Link";
		this.otherPermission       = "Read/Link";
		this.projectName           = null;
		this.latitude              = -35.3048782165934;
		this.chgOwner              = "trial/11/change/owner";
		this.projectId             = null;
		this.otherPerm             = 5;
		
	}
		
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
	public Integer getTrialTypeId() {
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
	public Integer getTrialID() {
		return trialID;
	}
	/**
	 * @param trialId the trialId to set
	 */
	public void setTrialID(int trialID) {
		this.trialID = trialID;
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
	/*public List<TrialTrait> getTrialTraits() {
		return trialTraits;
	}*/
	/**
	 * @param trialTratis the trialTratis to set
	 */
	/*public void setTrialTraits(List<TrialTrait> trialTraits) {
		this.trialTraits = trialTraits;
	}*/
	/**
	 * @return the trialUnits
	 */
	/*public List<TrialUnit> getTrialUnits() {
		return trialUnits;
	}*/
	/**
	 * @param trialUnits the trialUnits to set
	 */
	/*public void setTrialUnits(List<TrialUnit> trialUnits) {
		this.trialUnits = trialUnits;
	}*/
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
	 * @return the siteID
	 */
	public Integer getSiteID() {
		return siteId;
	}
	/**
	 * @param siteID the siteID to set
	 */
	public void setSiteID(Integer siteID) {
		this.siteId = siteID;
	}

	/**
	 * @return the siteId
	 */
	public Integer getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the designTypeId
	 */
	public Integer getDesignTypeId() {
		return designTypeId;
	}

	/**
	 * @param designTypeId the designTypeId to set
	 */
	public void setDesignTypeId(Integer designTypeId) {
		this.designTypeId = designTypeId;
	}

	/**
	 * @return the currentWorkflowId
	 */
	public Integer getCurrentWorkflowId() {
		return currentWorkflowId;
	}

	/**
	 * @param currentWorkflowId the currentWorkflowId to set
	 */
	public void setCurrentWorkflowId(Integer currentWorkflowId) {
		this.currentWorkflowId = currentWorkflowId;
	}

	/**
	 * @return the ownGroupPermission
	 */
	public String getOwnGroupPermission() {
		return ownGroupPermission;
	}

	/**
	 * @param ownGroupPermission the ownGroupPermission to set
	 */
	public void setOwnGroupPermission(String ownGroupPermission) {
		this.ownGroupPermission = ownGroupPermission;
	}

	/**
	 * @return the listTrialUnit
	 */
	public String getListTrialUnit() {
		return listTrialUnit;
	}

	/**
	 * @param listTrialUnit the listTrialUnit to set
	 */
	public void setListTrialUnit(String listTrialUnit) {
		this.listTrialUnit = listTrialUnit;
	}

	/**
	 * @return the accessGroupId
	 */
	public Integer getAccessGroupId() {
		return accessGroupId;
	}

	/**
	 * @param accessGroupId the accessGroupId to set
	 */
	public void setAccessGroupId(Integer accessGroupId) {
		this.accessGroupId = accessGroupId;
	}

	/**
	 * @return the accessGroupName
	 */
	public String getAccessGroupName() {
		return accessGroupName;
	}

	/**
	 * @param accessGroupName the accessGroupName to set
	 */
	public void setAccessGroupName(String accessGroupName) {
		this.accessGroupName = accessGroupName;
	}

	/**
	 * @return the accessGroupPermission
	 */
	public String getAccessGroupPermission() {
		return accessGroupPermission;
	}

	/**
	 * @param accessGroupPermission the accessGroupPermission to set
	 */
	public void setAccessGroupPermission(String accessGroupPermission) {
		this.accessGroupPermission = accessGroupPermission;
	}

	/**
	 * @return the accessGroupPerm
	 */
	public Integer getAccessGroupPerm() {
		return accessGroupPerm;
	}

	/**
	 * @param accessGroupPerm the accessGroupPerm to set
	 */
	public void setAccessGroupPerm(Integer accessGroupPerm) {
		this.accessGroupPerm = accessGroupPerm;
	}

	/**
	 * @return the map
	 */
	public String getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(String map) {
		this.map = map;
	}

	/**
	 * @return the addTrait
	 */
	public String getAddTrait() {
		return addTrait;
	}

	/**
	 * @param addTrait the addTrait to set
	 */
	public void setAddTrait(String addTrait) {
		this.addTrait = addTrait;
	}

	/**
	 * @return the ownGroupId
	 */
	public Integer getOwnGroupId() {
		return ownGroupId;
	}

	/**
	 * @param ownGroupId the ownGroupId to set
	 */
	public void setOwnGroupId(Integer ownGroupId) {
		this.ownGroupId = ownGroupId;
	}

	/**
	 * @return the trialLocation
	 */
	public String getTrialLocation() {
		return trialLocation;
	}

	/**
	 * @param trialLocation the trialLocation to set
	 */
	public void setTrialLocation(String trialLocation) {
		this.trialLocation = trialLocation;
	}

	/**
	 * @return the ownPermGroup
	 */
	public Integer getOwnGroupPerm() {
		return ownGroupPerm;
	}

	/**
	 * @param ownPermGroup the ownPermGroup to set
	 */
	public void setOwnGroupPerm(Integer ownGroupPerm) {
		this.ownGroupPerm = ownGroupPerm;
	}

	/**
	 * @return the ownGroupName
	 */
	public String getOwnGroupName() {
		return ownGroupName;
	}

	/**
	 * @param ownGroupName the ownGroupName to set
	 */
	public void setOwnGroupName(String ownGroupName) {
		this.ownGroupName = ownGroupName;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the update
	 */
	public String getUpdate() {
		return update;
	}

	/**
	 * @param update the update to set
	 */
	public void setUpdate(String update) {
		this.update = update;
	}

	/**
	 * @return the trialNumber
	 */
	public Integer getTrialNumber() {
		return trialNumber;
	}

	/**
	 * @param trialNumber the trialNumber to set
	 */
	public void setTrialNumber(Integer trialNumber) {
		this.trialNumber = trialNumber;
	}

	/**
	 * @return the ultimatePerm
	 */
	public Integer getUltimatePerm() {
		return ultimatePerm;
	}

	/**
	 * @param ultimatePerm the ultimatePerm to set
	 */
	public void setUltimatePerm(Integer ultimatePerm) {
		this.ultimatePerm = ultimatePerm;
	}

	/**
	 * @return the chgPerm
	 */
	public String getChgPerm() {
		return chgPerm;
	}

	/**
	 * @param chgPerm the chgPerm to set
	 */
	public void setChgPerm(String chgPerm) {
		this.chgPerm = chgPerm;
	}

	/**
	 * @return the ultimatePermission
	 */
	public String getUltimatePermission() {
		return ultimatePermission;
	}

	/**
	 * @param ultimatePermission the ultimatePermission to set
	 */
	public void setUltimatePermission(String ultimatePermission) {
		this.ultimatePermission = ultimatePermission;
	}

	/**
	 * @return the otherPermission
	 */
	public String getOtherPermission() {
		return otherPermission;
	}

	/**
	 * @param otherPermission the otherPermission to set
	 */
	public void setOtherPermission(String otherPermission) {
		this.otherPermission = otherPermission;
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the chgOwner
	 */
	public String getChgOwner() {
		return chgOwner;
	}

	/**
	 * @param chgOwner the chgOwner to set
	 */
	public void setChgOwner(String chgOwner) {
		this.chgOwner = chgOwner;
	}

	/**
	 * @return the projectId
	 */
	public Integer getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the otherPerm
	 */
	public Integer getOtherPerm() {
		return otherPerm;
	}

	/**
	 * @param otherPerm the otherPerm to set
	 */
	public void setOtherPerm(Integer otherPerm) {
		this.otherPerm = otherPerm;
	}

	/**
	 * @return the siteNameId
	 */
	public static EntityColumn getSiteNameId() {
		return SITE_NAME_ID;
	}

	/**
	 * @param trialId the trialId to set
	 */
	public void setTrialId(Integer trialId) {
		this.trialID = trialID;
	}
		
}

