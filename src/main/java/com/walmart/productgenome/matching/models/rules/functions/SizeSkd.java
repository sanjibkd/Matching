package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class SizeSkd extends Function {

	public static final String NAME = "SIZE_SKD";
	public static final String DESCRIPTION = "Module for matching sizes";
	public static final int NUM_ARGS = 2;
	
	public SizeSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public SizeSkd(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.STRING;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.SINGLE_WORD_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_MEDIUM_STRING);
		return recommendedTypes;
	}
	
	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		String arg1 = args[0];
		String arg2 = args[1];
		if (arg1 == null
				|| arg2 == null
				|| arg1.toLowerCase().equals("null")
				|| arg2.toLowerCase().equals("null")
				|| arg1.isEmpty() || arg2.isEmpty()) {
			return 0.0f;
		}
		
		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		try {
			float numArg1 = Float.parseFloat(arg1);
			float numArg2 = Float.parseFloat(arg2);
			//System.out.println("numArg1: " + numArg1 + ", numArg2: " + numArg2);
			if (Float.compare(numArg1, numArg2) == 0) {
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		catch(NumberFormatException e) {
			// do nothing
		}
		
		if (arg1.equalsIgnoreCase(arg2)) {
			return 1.0f;
		}
		else {
			return 0.0f;
		}
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

	public static void main(String[] args) {
	
	}
}
