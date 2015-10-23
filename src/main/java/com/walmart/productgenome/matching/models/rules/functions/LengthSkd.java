package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class LengthSkd extends Function {
	
	public static final String NAME = "LENGTH_SKD";
	public static final String DESCRIPTION = "Module for matching lengths";
	public static final int NUM_ARGS = 2;
	
	public LengthSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public LengthSkd(String name, String description) {
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
		recommendedTypes.add(AllTypes.SINGLE_WORD_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
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
		
		arg1 = arg1.trim();
		arg2 = arg2.trim();
		try {
			Float f1 = Float.parseFloat(arg1);
			Float f2 = Float.parseFloat(arg2);
			//System.out.println("f1: " + f1 + ",  f2: " + f2);
			if (Float.compare(f1, f2) == 0) {
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		catch (NumberFormatException e) {
			//System.out.println("Number format exception");
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

	public static void main(String[] args){
		LengthSkd ls = new LengthSkd();
		String[] args1 = {"14.5", "14.5"};
		String[] args2 = {"14", "14.0"};
		String[] args3 = {"5.1", "5.2"};
		String[] args4 = {"", ""};
		String[] args5 = {"", "6.5"};
		String[] args6 = {"3.5", ""};
		String[] args7 = {"1.6\"", "1.6 inch"};
		System.out.println("Matching lengths: " + ls.compute(args1));
		System.out.println("Matching lengths: " + ls.compute(args2));
		System.out.println("Non-matching lengths: " + ls.compute(args3));
		System.out.println("Both lengths missing: " + ls.compute(args4));
		System.out.println("Length 1 missing: " + ls.compute(args5));
		System.out.println("Length 2 missing: " + ls.compute(args6));
		System.out.println("Not a number: " + ls.compute(args7));
	}
}
