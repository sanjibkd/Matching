package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class UpcSkd extends Function {

	public static final String NAME = "UPC_SKD";
	public static final String DESCRIPTION = "Module for matching UPCs";
	public static final int NUM_ARGS = 2;
	
	public UpcSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public UpcSkd(String name, String description) {
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
		
		String arg1 = args[0];
		String arg2 = args[1];
		if (arg1 == null
				|| arg2 == null
				|| arg1.toLowerCase().equals("null")
				|| arg2.toLowerCase().equals("null")
				|| arg1.isEmpty() || arg2.isEmpty()) {
			return 0.0f;
		}
		
		arg1 = args[0].replaceAll("[^\\d]", "");
		arg2 = args[1].replaceAll("[^\\d]", "");
		if (arg1.length() == 12 && arg2.length() == 12 && arg1.equals(arg2)) {
			return 1.0f;
		}
		else if (arg1.length() == 12 && arg2.length() == 11 && arg2.equals(arg1.substring(0, 11))) {
			return 1.0f;
		}
		else if (arg1.length() == 11 && arg2.length() == 12 && arg1.equals(arg2.substring(0, 11))) {
			return 1.0f;
		}
		else if (arg1.length() == 11 && arg2.length() == 11 && arg1.equals(arg2)) {
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
		Function upcSkd = new UpcSkd();
		String[] s1 = {"729305001290", "729305001290"};
		String[] s2 = {"28377903830", "28377903830"};
		String[] s3 = {"60OJWNeR6333", "42005159505"};
		String[] s4 = {"602409316814", "MP10006905582"};
		String[] s5 = {"57PBQKv30713", "MP10006905582"};
		String[] s6 = {null, "0088493821874"};
		String[] s7 = {"602409316814", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "0088493821874"};
		String[] s10 = {"602409316814", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "0088493821874"};
		String[] s13 = {"602409316814", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("MATCHING 12-digit UPCs: " + upcSkd.compute(s1));
		System.out.println("MATCHING 11-digit UPCs:" + upcSkd.compute(s2));
		System.out.println("INVALID UPC of item 1: " + upcSkd.compute(s3));
		System.out.println("INVALID UPC of item 2: " + upcSkd.compute(s4));
		System.out.println("INVALID UPC of both items: " + upcSkd.compute(s5));
		System.out.println("NULL UPC of item 1: " + upcSkd.compute(s6));
		System.out.println("NULL UPC of item 2: " + upcSkd.compute(s7));
		System.out.println("NULL UPC of both items: " + upcSkd.compute(s8));
		System.out.println("Empty UPC of item 1: " + upcSkd.compute(s9));
		System.out.println("Empty UPC of item 2: " + upcSkd.compute(s10));
		System.out.println("Empty UPC of both items: " + upcSkd.compute(s11));
		System.out.println("Null string UPC of item 1: " + upcSkd.compute(s12));
		System.out.println("Null string UPC of item 2: " + upcSkd.compute(s13));
		System.out.println("Null string UPC of both items: " + upcSkd.compute(s14));
	}
}
