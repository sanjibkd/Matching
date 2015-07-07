package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class AbsoluteDifference extends Function {

	public static final String NAME = "ABS_DIFF";
	public static final String DESCRIPTION = "Absolute difference of two numeric values";
	public static final int NUM_ARGS = 2;
	
	public AbsoluteDifference() {
		super(NAME, DESCRIPTION);
	}
	
	public AbsoluteDifference(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.NUMERIC;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.NUMERIC);
		return recommendedTypes;
	}

	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if(args.length != NUM_ARGS){
			throw new IllegalArgumentException("Expected number of arguments: " +
					NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		Float res = -1.0f;
		try{
			Float f1 = Float.parseFloat(args[0]);
			Float f2 = Float.parseFloat(args[1]);
			res = Math.abs(f1-f2);
		}
		catch(NumberFormatException nfe){
			res = -1.0f;
		}
		return res;
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append(",");
		sb.append(this.getDescription());
		sb.append(",");
		sb.append(this.getClass().getName());
		sb.append(",");
		sb.append(Float.class.getName());
		sb.append(",");
		sb.append(NUM_ARGS);
		sb.append(",");
		sb.append(Float.class.getName());
		sb.append(",");
		sb.append(Float.class.getName());
		return sb.toString();
	}

}
