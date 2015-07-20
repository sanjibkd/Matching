package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class NormalizeAndMatchUPC extends Function {

	public static final String NAME = "NORM_EXACT_MATCH";
	public static final String DESCRIPTION = "Normalised Exact Match";
	public static final int NUM_ARGS = 2;
	
	public NormalizeAndMatchUPC() {
		super(NAME, DESCRIPTION);
	}
	
	public NormalizeAndMatchUPC(String name, String description) {
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
		return recommendedTypes;
	}
	
	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].replaceAll("[^\\d]", "");
		String arg2 = args[1].replaceAll("[^\\d]", "");
		
		if (arg1.length() != 12 || arg2.length() != 13) {
			// invalid UPC
			return -1.0f;
		}
		
		String normArg1 = arg1.substring(0, arg1.length() - 1);
		String normArg2 = arg2.substring(2);
		
		if (normArg1.equals(normArg2)) {
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

	public static void main(String[] args){
		Function nmupc = new NormalizeAndMatchUPC();
		String[] s1 = {"[\"884938218740\"]", "[\"0088493821874\"]"};
		String[] s2 = {"[\"602409316814\"]", "[\"0060240931680\"]"};
		String[] s3 = {"[\"57PBQKv30713\"]", "[\"0088493821874\"]"};
		String[] s4 = {"[\"602409316814\"]", "[\"MP10006905582\"]"};
		String[] s5 = {"[\"57PBQKv30713\"]", "[\"MP10006905582\"]"};
		String[] s6 = {null, "[\"0088493821874\"]"};
		String[] s7 = {"[\"602409316814\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"0088493821874\"]"};
		String[] s10 = {"[\"602409316814\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"0088493821874\"]"};
		String[] s13 = {"[\"602409316814\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("MATCHING UPCs: " + nmupc.compute(s1));
		System.out.println("NON-MATCHING UPCs:" + nmupc.compute(s2));
		System.out.println("INVALID UPC of item 1: " + nmupc.compute(s3));
		System.out.println("INVALID UPC of item 2: " + nmupc.compute(s4));
		System.out.println("INVALID UPC of both items: " + nmupc.compute(s5));
		System.out.println("NULL UPC of item 1: " + nmupc.compute(s6));
		System.out.println("NULL UPC of item 2: " + nmupc.compute(s7));
		System.out.println("NULL UPC of both items: " + nmupc.compute(s8));
		System.out.println("Empty UPC of item 1: " + nmupc.compute(s9));
		System.out.println("Empty UPC of item 2: " + nmupc.compute(s10));
		System.out.println("Empty UPC of both items: " + nmupc.compute(s11));
		System.out.println("Null string UPC of item 1: " + nmupc.compute(s12));
		System.out.println("Null string UPC of item 2: " + nmupc.compute(s13));
		System.out.println("Null string UPC of both items: " + nmupc.compute(s14));
	}
	
}
