package com.diversityarrays.dal.entity;

import com.diversityarrays.dal.entity.MeasurementIdentifier;

/*
 * author: Raul Hernandez T.
 * date:   08-31-2015
 */

public class Measurements {
    
	private MeasurementIdentifier measurementIdentifier;
	private String                measurementValue;
    
    public Measurements(){}
    
	public Measurements(MeasurementIdentifier measurementIdentifier,String measurementValue) {
		this.measurementIdentifier = measurementIdentifier;
		this.measurementValue = measurementValue;
	}

	public MeasurementIdentifier getMeasurementIdentifier() {
		return measurementIdentifier;
	}

	public void setMeasurementIdentifier(MeasurementIdentifier measurementIdentifier) {
		this.measurementIdentifier = measurementIdentifier;
	}

	public String getMeasurementValue() {
		return measurementValue;
	}

	public void setMeasurementValue(String measurementValue) {
		this.measurementValue = measurementValue;
	}
}