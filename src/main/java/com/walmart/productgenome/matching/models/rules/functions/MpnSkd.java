package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class MpnSkd extends Function {
	public static final String NAME = "MPN_SKD";
	public static final String DESCRIPTION = "Module for matching MPNs";
	public static final int NUM_ARGS = 2;
	
	public MpnSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public MpnSkd(String name, String description) {
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
		
		arg1 = args[0].replaceAll("[^\\w]", "");
		arg2 = args[1].replaceAll("[^\\w]", "");
		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		try {
			int numArg1 = Integer.parseInt(arg1);
			int numArg2 = Integer.parseInt(arg2);
			//System.out.println("numArg1: " + numArg1 + ", numArg2: " + numArg2);
			if (numArg1 == numArg2) {
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		catch(NumberFormatException e) {
			// do nothing
		}
		
		if (arg1.equalsIgnoreCase(arg2) || arg1.contains(arg2) || arg2.contains(arg1)) {
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
		Function mpnSkd = new MpnSkd();
		String[] s1 = {"UBN23310", "UBN233-10"};
		String[] s2 = {"96650", "9665-0"};
		String[] s3 = {"JWS111 ALOHA", "JWS-111 ALOHA"};
		String[] s4 = {"08803", "8803"};
		String[] s5 = {"08768", "8768"};
		String[] s6 = {null, "UBN233-10"};
		String[] s7 = {"UBN23310", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "UBN233-10"};
		String[] s10 = {"UBN23310", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "UBN233-10"};
		String[] s13 = {"UBN23310", "null"};
		String[] s14 = {"null", "null"};
		String[] s15 = {"F9H71012", "BKNF9H71012"};
		System.out.println("MATCHING MPNs: " + mpnSkd.compute(s1));
		System.out.println("MATCHING MPNs:" + mpnSkd.compute(s2));
		System.out.println("MATCHING MPNs: " + mpnSkd.compute(s3));
		System.out.println("MATCHING MPNs: " + mpnSkd.compute(s4));
		System.out.println("MATCHING MPNs: " + mpnSkd.compute(s5));
		System.out.println("NULL MPN of item 1: " + mpnSkd.compute(s6));
		System.out.println("NULL MPN of item 2: " + mpnSkd.compute(s7));
		System.out.println("NULL MPN of both items: " + mpnSkd.compute(s8));
		System.out.println("Empty MPN of item 1: " + mpnSkd.compute(s9));
		System.out.println("Empty MPN of item 2: " + mpnSkd.compute(s10));
		System.out.println("Empty MPN of both items: " + mpnSkd.compute(s11));
		System.out.println("Null string MPN of item 1: " + mpnSkd.compute(s12));
		System.out.println("Null string MPN of item 2: " + mpnSkd.compute(s13));
		System.out.println("Null string MPN of both items: " + mpnSkd.compute(s14));
		System.out.println("MATCHING MPNs: " + mpnSkd.compute(s15));
	}	
}
