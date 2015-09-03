package com.diversityarrays.dal.entity;

import com.diversityarrays.dal.entity.Trait;

/*
 * Raul Hernandez T.
 * @date 09/02/2015
 */

public class MeasurementIdentifier {
	private Integer measurementId;
	private Trait   trait;
	
	public MeasurementIdentifier() {}
	
	public MeasurementIdentifier(Integer measurementId, Trait trait) {
		this.measurementId = measurementId;
		this.trait = trait;
	}

	public Integer getMeasurementId() {
		return measurementId;
	}
	public void setMeasurementId(Integer measurementId) {
		this.measurementId = measurementId;
	}
	public Trait getTrait() {
		return trait;
	}
	public void setTrait(Trait trait) {
		this.trait = trait;
	}

}