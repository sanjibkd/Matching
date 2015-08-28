package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class AbsoluteDifferenceExtractNumeric extends Function {

	public static final String NAME = "ABS_DIFF_NUM";
	public static final String DESCRIPTION = "Absolute difference between two " +
			"numeric values extracted from string.";
	public static final int NUM_ARGS = 2;
	
	public AbsoluteDifferenceExtractNumeric() {
		super(NAME, DESCRIPTION);
	}
	
	public AbsoluteDifferenceExtractNumeric(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.STRING;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.NUMERIC);
		return recommendedTypes;
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
		sb.append(String.class.getName());
		sb.append(",");
		sb.append(String.class.getName());
		return sb.toString();
	}
	
	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		int l1 = args[0].length();
		int l2 = args[1].length();
		String arg1 = args[0].substring(2, l1 - 2); // [".."]
		String arg2 = args[1].substring(2, l2 - 2); // [".."]		
		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		try {
			Float f1 = Float.parseFloat(arg1);
			Float f2 = Float.parseFloat(arg2);
			return Math.abs(f1 - f2);
		}
		catch (NumberFormatException e) {
			return -1.0f;
		}
	}
	
}