package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class PackageQuantitySkd extends Function {
	public static final String NAME = "PACKAGE_QUANTITY_SKD";
	public static final String DESCRIPTION = "Module for matching package quantities";
	public static final int NUM_ARGS = 2;
	
	public PackageQuantitySkd() {
		super(NAME, DESCRIPTION);
	}
	
	public PackageQuantitySkd(String name, String description) {
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
			int f1 = Integer.parseInt(arg1);
			int f2 = Integer.parseInt(arg2);
			//System.out.println("f1: " + f1 + ",  f2: " + f2);
			if (f1 == f2) {
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
		PackageQuantitySkd ws = new PackageQuantitySkd();
		String[] args1 = {"8", "8"};
		String[] args3 = {"1", "2"};
		String[] args4 = {"", ""};
		String[] args5 = {"", "2"};
		String[] args6 = {"1", ""};
		String[] args7 = {"Pack of 4", "4"};
		System.out.println("Matching widths: " + ws.compute(args1));
		System.out.println("Non-matching widths: " + ws.compute(args3));
		System.out.println("Both widths missing: " + ws.compute(args4));
		System.out.println("Width 1 missing: " + ws.compute(args5));
		System.out.println("Width 2 missing: " + ws.compute(args6));
		System.out.println("Not a number: " + ws.compute(args7));
	}
}
