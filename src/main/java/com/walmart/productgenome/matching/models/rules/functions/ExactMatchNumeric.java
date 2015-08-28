package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class ExactMatchNumeric extends Function {

	public static final String NAME = "EXACT_MATCH_NUM";
	public static final String DESCRIPTION = "Returns 1 if two strings match otherwise 0";
	public static final int NUM_ARGS = 2;
	
	public ExactMatchNumeric() {
		super(NAME, DESCRIPTION);
	}

	public ExactMatchNumeric(String name, String description) {
		super(name, description);
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
			double d1 = Double.parseDouble(arg1);
			double d2 = Double.parseDouble(arg2);
			if (d1 == d2) {
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		catch (NumberFormatException e) {
			return -1.0f;
		}
	}

  @Override
  public ArgType getArgType() {
    return ArgType.STRING;
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
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.SINGLE_WORD_STRING);
		recommendedTypes.add(AllTypes.NUMERIC);
		recommendedTypes.add(AllTypes.CATEGORICAL);
		return recommendedTypes;
	}
	
	public static void main(String[] args){
		Function emn = new ExactMatchNumeric();
		String[] s1 = {"[\"8.0\"]", "[\"8.0\"]"};
		String[] s2 = {"[\"8.0\"]", "[\"8\"]"};
		String[] s3 = {"[\"2.31\"]", "[\"2.3\"]"};
		String[] s4 = {"[\"Regular (M)\"]", "[\"2.3\"]"};
		String[] s5 = {"[\"2.3\"]", "[\"Regular (M)\"]"};
		String[] s6 = {null, "[\"8.0\"]"};
		String[] s7 = {"[\"8\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"8.0\"]"};
		String[] s10 = {"[\"8\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"8\"]"};
		String[] s13 = {"[\"8.0\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("Numeric values match: " + emn.compute(s1));
		System.out.println("Numeric values match:" + emn.compute(s2));
		System.out.println("Numeric values do not match: " + emn.compute(s3));
		System.out.println("Non-numeric value: " + emn.compute(s4));
		System.out.println("Non-numeric value: " + emn.compute(s5));
		System.out.println("NULL A: " + emn.compute(s6));
		System.out.println("NULL B: " + emn.compute(s7));
		System.out.println("NULL both A and B: " + emn.compute(s8));
		System.out.println("Empty A: " + emn.compute(s9));
		System.out.println("Empty B: " + emn.compute(s10));
		System.out.println("Empty both A and B: " + emn.compute(s11));
		System.out.println("Null string A: " + emn.compute(s12));
		System.out.println("Null string B: " + emn.compute(s13));
		System.out.println("Null string both A and B: " + emn.compute(s14));
	}	
}
