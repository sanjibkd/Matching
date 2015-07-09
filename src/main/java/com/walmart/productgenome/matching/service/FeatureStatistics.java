package com.walmart.productgenome.matching.service;

public class FeatureStatistics {

	private long count0;
	private long count1;
	private long countNull;
	private long[] bucketCounts;
	
	private long avgFeatureCostPerTuple; // in nanoseconds
	private long avgFeatureCostPerTupleExcludingNull; // in nanoseconds
	
	public FeatureStatistics(long count0, long count1, long countNull,
			long[] bucketCounts, long avgFeatureCostPerTuple, long avgFeatureCostPerTupleExcludingNull) {
		this.count0 = count0;
		this.count1 = count1;
		this.countNull = countNull;
		this.bucketCounts = bucketCounts;
		this.avgFeatureCostPerTuple = avgFeatureCostPerTuple;
		this.avgFeatureCostPerTupleExcludingNull = avgFeatureCostPerTupleExcludingNull;
	}

	public long getCount0() {
		return count0;
	}

	public long getCount1() {
		return count1;
	}

	public long getCountNull() {
		return countNull;
	}

	public long[] getBucketCounts() {
		return bucketCounts;
	}

	public long getAvgFeatureCostPerTuple() {
		return avgFeatureCostPerTuple;
	}

	public long getAvgFeatureCostPerTupleExcludingNull() {
		return avgFeatureCostPerTupleExcludingNull;
	}
	
}
