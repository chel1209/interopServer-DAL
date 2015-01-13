package com.diversityarrays.dal.db;

public class RecordCountCacheEntry {
	
	public final String filterClause;
	public final int count;
	
	public RecordCountCacheEntry(String fc, int n) {
		this.filterClause = fc;
		this.count = n;
	}
	
	public boolean isFor(String fc) {
		if (this.filterClause==null) {
			return fc==null;
		}
		return this.filterClause.equals(fc);
	}
}