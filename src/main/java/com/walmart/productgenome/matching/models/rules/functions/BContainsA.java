package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class BContainsA extends Function {

	public static final String NAME = "B_CONTAINS_A";
	public static final String DESCRIPTION = "Returns 1 if A is in B, otherwise 0";
	public static final int NUM_ARGS = 2;

	public BContainsA() {
		super(NAME, DESCRIPTION);
	}

	public BContainsA(String name, String description) {
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

		String arg1 = args[0].replaceAll("[^\\w]", "").toLowerCase();
		//System.out.println("arg1: " + arg1);
		
		String[] arg2Vals = args[1].split("\\s");

		Set<String> arg2Words = new HashSet<String>();
		for (String s: arg2Vals) {
			s = s.replaceAll("[^\\w]", "").toLowerCase();
			//System.out.println(s);
			arg2Words.add(s);
		}
		
		if (arg2Words.contains(arg1)) {
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
		Function nmmpn = new BContainsA();
		String[] s1 = {"[\"4705A018\"]", "[\"BCI-6 Color Ink Cartridges. Brand: Canon USA. 4705A018. InkBci-6Six Pack Clamshell. UPC: 750845815542\"]"};
		String[] s2 = {"[\"4460089\"]", "[\"THRUSTMASTER 4460089 Y-400XW GAMING HEADSET FOR XBOX 360(R)\"]"};
		String[] s3 = {"[\"C540H1YG\"]", "[\"C540H1YG High-Yield Toner 2000 Page-Yield Yellow\"]"};
		String[] s4 = {"[\"6R1046\"]", "[\"6R1046 Copy Cartridge 60000 Page-Yield 2/Carton Black\"]"};
		String[] s5 = {"[\"UG3350\"]", "[\"UG3350 Toner 7500 Page-Yield Black\"]"};
		String[] s6 = {null, "[\"UBN233-10\"]"};
		String[] s7 = {"[\"UBN23310\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"UBN233-10\"]"};
		String[] s10 = {"[\"UBN23310\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"UBN233-10\"]"};
		String[] s13 = {"[\"UBN23310\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("B contains A: " + nmmpn.compute(s1));
		System.out.println("B contains A:" + nmmpn.compute(s2));
		System.out.println("B contains A: " + nmmpn.compute(s3));
		System.out.println("B contains A: " + nmmpn.compute(s4));
		System.out.println("B contains A: " + nmmpn.compute(s5));
		System.out.println("NULL A: " + nmmpn.compute(s6));
		System.out.println("NULL B: " + nmmpn.compute(s7));
		System.out.println("NULL both A and B: " + nmmpn.compute(s8));
		System.out.println("Empty A: " + nmmpn.compute(s9));
		System.out.println("Empty B: " + nmmpn.compute(s10));
		System.out.println("Empty both A and B: " + nmmpn.compute(s11));
		System.out.println("Null string A: " + nmmpn.compute(s12));
		System.out.println("Null string B: " + nmmpn.compute(s13));
		System.out.println("Null string both A and B: " + nmmpn.compute(s14));
	}	

}
