package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class Refurbished extends Function {

	public static final String NAME = "REFURBISHED";
	public static final String DESCRIPTION = "Returns 1 if both A and B have "
			+ "refurbished, 0 if any one has refurbished, and -1 if none have "
			+ "refurbished";
	public static final int NUM_ARGS = 2;

	public Refurbished() {
		super(NAME, DESCRIPTION);
	}
	
	public Refurbished(String name, String description) {
		super(name, description);
	}

	public static void main(String[] args) {
		Function ref = new Refurbished();
		String[] s1 = {"Sceptre 22 LED 1080p Full HD Monitor (E225W-1920 Black)",
				"Sceptre E275W-1920 27 LED Monitor - 1920 x 1080 (Refurbished)"};
		String[] s2 = {"Sceptre 22 LED 1080p Full HD Monitor (E225W-1920 Black)",
		"Sceptre E275W-1920 27 LED Monitor - 1920 x 1080"};
		String[] s3 = {"Sceptre 22 LED 1080p Full HD Monitor Refurbished (E225W-1920 Black)",
		"Sceptre E275W-1920 27 LED Monitor - 1920 x 1080 (Refurbished)"};
		System.out.println("One refurbished: " + ref.compute(s1));
		System.out.println("None refurbished: " + ref.compute(s2));
		System.out.println("Both refurbished: " + ref.compute(s3));
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
		
		String arg1 = removeNonAsciiCharacters(args[0].toLowerCase());
		String arg2 = removeNonAsciiCharacters(args[1].toLowerCase());
		
		if ((arg1.contains("refurbished") || arg1.contains("remanufactured")) &&
				(arg2.contains("refurbished") || arg2.contains("remanufactured"))) {
			return 1.0f;
		}
		else if (!arg1.contains("refurbished") && !arg1.contains("remanufactured") &&
				!arg2.contains("refurbished") && !arg2.contains("remanufactured")) {
			return -1.0f;
		}
		return 0.0f;
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

}
