package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class RelativeDifference extends Function {

	public static final String NAME = "REL_DIFF";
	public static final String DESCRIPTION = "Relative difference between two " +
			"numeric values. Mathematically, REL_DIFF(a,b) = |a-b| / (a+b)";
	public static final int NUM_ARGS = 2;
	
	public RelativeDifference() {
		super(NAME, DESCRIPTION);
	}
	
	public RelativeDifference(String name, String description) {
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
		
		// for missing values
		if (args[0] == null || args[1] == null) {
			return Float.NaN;
		}
		
		Float res = null;
		try {
			Float f1 = Float.parseFloat(args[0]);
			Float f2 = Float.parseFloat(args[1]);
			if (f1 == 0.0f && f2 == 0.0f) {
				res = 0.0f;
			}
			else {
				res = Math.abs(f1 - f2) / (f1 + f2);	
			}
			
		}
		catch(NumberFormatException nfe) {
			res = Float.NaN;
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
