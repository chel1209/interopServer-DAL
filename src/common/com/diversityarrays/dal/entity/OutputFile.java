package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="OutputFile")
@EntityTag("OutputFile")
public class OutputFile extends DalEntity {

	@Column(name="FileIdentifier", nullable=false, length=(50))
	private String fileIdentifier;
	
	@Column(name="csv", nullable=false, length=(50))
	private String csv;
	
	@Column(name="FileDescription", nullable=false, length=(50))
	private String fileDescription;
	
	@Column(name="FileType", nullable=false, length=(50))
	private String fileType;

	/**
	 * @return the fileIdentifier
	 */
	public String getFileIdentifier() {
		return fileIdentifier;
	}

	/**
	 * @param fileIdentifier the fileIdentifier to set
	 */
	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}

	/**
	 * @return the csv
	 */
	public String getCsv() {
		return csv;
	}

	/**
	 * @param csv the csv to set
	 */
	public void setCsv(String csv) {
		this.csv = csv;
	}

	/**
	 * @return the fileDescription
	 */
	public String getFileDescription() {
		return fileDescription;
	}

	/**
	 * @param fileDescription the fileDescription to set
	 */
	public void setFileDescription(String fileDescription) {
		this.fileDescription = fileDescription;
	}

	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
}
