package com.walmart.productgenome.matching.models.data;

public class AttributePair {
	
	private Attribute attribute1;
	private Attribute attribute2;
	
	public AttributePair(Attribute attribute1, Attribute attribute2) {
		this.attribute1 = attribute1;
		this.attribute2 = attribute2;
	}
	
	public Attribute getFirst() {
		return attribute1;
	}
	
	public Attribute getSecond() {
		return attribute2;
	}
}
