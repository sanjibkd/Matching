package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public class ExtractMemoryCapacityAndMatch extends Function {
	
	public static final String NAME = "EXTRACT_MEMORY_CAPACITY_MATCH";
	public static final String DESCRIPTION = "Returns 1 if memory capacities match, 0 otherwise";
	public static final int NUM_ARGS = 2;

	public ExtractMemoryCapacityAndMatch() {
		super(NAME, DESCRIPTION);
	}

	public ExtractMemoryCapacityAndMatch(String name, String description) {
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
		recommendedTypes.add(AllTypes.MULTI_WORD_LONG_STRING);
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

		
		String firstWord1 = args[0].split("\\s")[0];
		firstWord1 = firstWord1.replaceAll("[^\\w]", "").toLowerCase();
		//System.out.println("first word 1: " + firstWord1);
		
		String firstWord2 = args[1].split("\\s")[0];
		firstWord2 = firstWord2.replaceAll("[^\\w]", "").toLowerCase();
		//System.out.println("first word 2: " + firstWord2);
		
		if (firstWord1.equals(firstWord2)) {
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
		Function nmmpn = new FirstWordsMatch();
		String[] s1 = {"[\"Targus 15.4 Gravity Backpack (Pewter), RG0318\"]", "[\"Targus Gravity 15.4\" Laptop Backpack\"]"};
		String[] s2 = {"[\"TOSHIBA AMERICA CONSUMER Toner Cartridge, Black\"]", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s3 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", "[\"rooCASE Deluxe Carrying Bag for iPad 2 10\" and 11.6\" Netbook\"]"};
		String[] s4 = {"[\"Belkin AV20500-25 Blue Series Subwoofer Cable (25 ft; Retail Packaging)\"]", "[\"Blue Series Subwoofer Cable (15 Ft; Retail Packaging)\"]"};
		String[] s5 = {"[\"Morning Industry Inc RF-01AQ Remote Control Electronic Deadbolt, Antique Brass\"]", "[\"Remote Control Electronic Dead Bolt (Polished Brass)\"]"};
		String[] s6 = {null, "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s7 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", null};
		String[] s8 = {null, null};
		String[] s9 = {"", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s10 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", ""};
		String[] s11 = {"", ""};
		String[] s12 = {"null", "[\"Toshiba America Consumer Toner Cartridge Black\"]"};
		String[] s13 = {"[\"rooCASE Deluxe Carrying Bag for 11.6 Netbook, Pink\"]", "null"};
		String[] s14 = {"null", "null"};
		System.out.println("First Words Match: " + nmmpn.compute(s1));
		System.out.println("First Words Match: " + nmmpn.compute(s2));
		System.out.println("First Words Match: " + nmmpn.compute(s3));
		System.out.println("First Words Do Not Match: " + nmmpn.compute(s4));
		System.out.println("First Words Do Not Match: " + nmmpn.compute(s5));
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
