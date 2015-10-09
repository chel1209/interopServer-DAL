package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="Site")
@EntityTag("Site")
public class Site extends DalEntity {
	
	@Column(name="Longitude")
	private Double longitude;
	
	@Id
	@Column(name="SiteId")
	private Integer siteId;
	
	@Column(name="SiteTypeName")
	private String siteTypeName;
	
	@Column(name="SiteName")
	private String siteName;
	
	@Column(name="Latitude")
	private Double latitude;
	
	@Column(name="SiteTypeId")
	private Integer siteTypeId;
	
	@Column(name="SiteAcronym")
	private String siteAcronym;
		
	public final static EntityColumn SITE_ID = createEntityColumn(Site.class, "siteId");
	public final static EntityColumn SITE_NAME = createEntityColumn(Site.class, "siteName");
	public final static EntityColumn SITE_ACRONYM  = createEntityColumn(Site.class, "siteAcronym");
	public final static EntityColumn SITE_TYPE_ID  = createEntityColumn(Site.class, "siteTypeId");
	public final static EntityColumn SITE_TYPE_NAME  = createEntityColumn(Site.class, "siteTypeName");
	public final static EntityColumn LATITUDE  = createEntityColumn(Site.class, "latitude");
	public final static EntityColumn LONGITUDE  = createEntityColumn(Site.class, "longitude");
	
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
	 * @return the siteTypeName
	 */
	public String getSiteTypeName() {
		return siteTypeName;
	}
	/**
	 * @param siteTypeName the siteTypeName to set
	 */
	public void setSiteTypeName(String siteTypeName) {
		this.siteTypeName = siteTypeName;
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
	 * @return the siteTypeId
	 */
	public Integer getSiteTypeId() {
		return siteTypeId;
	}
	/**
	 * @param siteTypeId the siteTypeId to set
	 */
	public void setSiteTypeId(Integer siteTypeId) {
		this.siteTypeId = siteTypeId;
	}
	/**
	 * @return the siteAcronym
	 */
	public String getSiteAcronym() {
		return siteAcronym;
	}
	/**
	 * @param siteAcronym the siteAcronym to set
	 */
	public void setSiteAcronym(String siteAcronym) {
		this.siteAcronym = siteAcronym;
	}
	
	

}
