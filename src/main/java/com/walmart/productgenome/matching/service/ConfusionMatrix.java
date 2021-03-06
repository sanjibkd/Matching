package com.walmart.productgenome.matching.service;

public class ConfusionMatrix {

	/**
	 * 				Predicted	+		- 
	 * Actual	+				TP		FN
	 * 			-				FP		TN
	 * Metrics like accuracy, precision, recall, selectivity, etc. can be
	 * easily computed from this matrix
	 */
	private long truePositives;
	private long falseNegatives;
	private long falsePositives;
	private long trueNegatives;
	private long timeMillis;
	private String modelInfo;
	private String modelOptions;
	
	public long getTruePositives() {
		return truePositives;
	}
	
	public void setTruePositives(long truePositives) {
		this.truePositives = truePositives;
	}
	
	public long getFalseNegatives() {
		return falseNegatives;
	}
	
	public void setFalseNegatives(long falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	
	public long getFalsePositives() {
		return falsePositives;
	}
	
	public void setFalsePositives(long falsePositives) {
		this.falsePositives = falsePositives;
	}
	
	public long getTrueNegatives() {
		return trueNegatives;
	}
	
	public void setTrueNegatives(long trueNegatives) {
		this.trueNegatives = trueNegatives;
	}
	
	public long getTimeMillis() {
		return timeMillis;
	}
	
	public void setTimeMillis(long timeMillis) {
		this.timeMillis = timeMillis;
	}
	
	public String getModelInfo() {
		return modelInfo;
	}
	
	public void setModelInfo(String modelInfo) {
		this.modelInfo = modelInfo;
	}
	
	public String getModelOptions() {
		return modelOptions;
	}
	
	public void setModelOptions(String modelOptions) {
		this.modelOptions = modelOptions;
	}
	
	@Override
	public String toString() {
		return "ConfusionMatrix [truePositives=" + truePositives
				+ ", falseNegatives=" + falseNegatives + ", falsePositives="
				+ falsePositives + ", trueNegatives=" + trueNegatives
				+ ", time taken (ms): " + timeMillis
				+ ", modelInfo: " + modelInfo
				+ ", modelOptions: " + modelOptions +"]";
	}
}
