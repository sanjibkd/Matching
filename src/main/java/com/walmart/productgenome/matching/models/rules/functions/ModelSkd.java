package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ModelSkd extends Function {

	public static final String NAME = "MATERIAL_SKD";
	public static final String DESCRIPTION = "Modules for matching materials";
	public static final int NUM_ARGS = 2;

	public ModelSkd() {
		super(NAME, DESCRIPTION);
	}

	public ModelSkd(String name, String description) {
		super(name, description);
	}

	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
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

		arg1 = arg1.toLowerCase();
		arg2 = arg2.toLowerCase();
		
		if (arg1.startsWith("{") && arg1.endsWith("}")) {
			arg1 = arg1.substring(1, arg1.length() - 1);
		}
		if (arg2.startsWith("{") && arg2.endsWith("}")) {
			arg2 = arg2.substring(1, arg2.length() - 1);
		}

		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		Set<String> models1 = new HashSet<String>();
		Set<String> models2 = new HashSet<String>();
		
		String[] vals1 = arg1.split(",");
		String[] vals2 = arg2.split(",");
		
		for (String s: vals1) {
			models1.add(s.trim());
		}
		for (String s: vals2) {
			models2.add(s.trim());
		}
		for (String s: models1) {
			if (models2.contains(s)) {
				return 1.0f;
			}
		}
		return 0.0f;
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

	public static void main(String[] args){
		Function modelSkd = new ModelSkd();
		String[] args1 = {"CVR600", "{CVR600}"};
		String[] args2 = {"{Z1R, Phantom Monsoon}", "Z1R"};
		System.out.println("Matching models: " + modelSkd.compute(args1));
		System.out.println("Matching models: " + modelSkd.compute(args2));
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_MEDIUM_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_LONG_STRING);
		recommendedTypes.add(AllTypes.SET_VALUED);
		return recommendedTypes;
	}
}
