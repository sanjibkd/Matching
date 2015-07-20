package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class NormalizeAndMatchMPN extends Function {

	public static final String NAME = "NORM_EXACT_MATCH_MPN";
	public static final String DESCRIPTION = "Normalised Exact Match for MPN";
	public static final int NUM_ARGS = 2;
	
	public NormalizeAndMatchMPN() {
		super(NAME, DESCRIPTION);
	}
	
	public NormalizeAndMatchMPN(String name, String description) {
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
		
		String arg1 = args[0].replaceAll("[^\\w]", "");
		String arg2 = args[1].replaceAll("[^\\w]", "");
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
		
		if (arg1.equals(arg2)) {
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
		Function nmmpn = new NormalizeAndMatchMPN();
		String[] s1 = {"[\"UBN23310\"]", "[\"UBN233-10\"]"};
		String[] s2 = {"[\"96650\"]", "[\"9665-0\"]"};
		String[] s3 = {"[\"JWS111 ALOHA\"]", "[\"JWS-111 ALOHA\"]"};
		String[] s4 = {"[\"08803\"]", "[\"8803\"]"};
		String[] s5 = {"[\"08768\"]", "[\"8768\"]"};
		String[] s6 = {null, "[\"UBN233-10\"]"};
		String[] s7 = {"[\"UBN23310\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"UBN233-10\"]"};
		String[] s10 = {"[\"UBN23310\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"UBN233-10\"]"};
		String[] s13 = {"[\"UBN23310\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("MATCHING MPNs: " + nmmpn.compute(s1));
		System.out.println("MATCHING MPNs:" + nmmpn.compute(s2));
		System.out.println("MATCHING MPNs: " + nmmpn.compute(s3));
		System.out.println("MATCHING MPNs: " + nmmpn.compute(s4));
		System.out.println("MATCHING MPNs: " + nmmpn.compute(s5));
		System.out.println("NULL UPC of item 1: " + nmmpn.compute(s6));
		System.out.println("NULL UPC of item 2: " + nmmpn.compute(s7));
		System.out.println("NULL UPC of both items: " + nmmpn.compute(s8));
		System.out.println("Empty UPC of item 1: " + nmmpn.compute(s9));
		System.out.println("Empty UPC of item 2: " + nmmpn.compute(s10));
		System.out.println("Empty UPC of both items: " + nmmpn.compute(s11));
		System.out.println("Null string UPC of item 1: " + nmmpn.compute(s12));
		System.out.println("Null string UPC of item 2: " + nmmpn.compute(s13));
		System.out.println("Null string UPC of both items: " + nmmpn.compute(s14));
	}	
}

