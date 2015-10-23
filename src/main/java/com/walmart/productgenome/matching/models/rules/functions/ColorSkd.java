package com.walmart.productgenome.matching.models.rules.functions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ColorSkd extends Function {

	public static final String NAME = "COLOR_SKD";
	public static final String DESCRIPTION = "Module for matching colors";
	public static final int NUM_ARGS = 2;
	
	public ColorSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public ColorSkd(String name, String description) {
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
				|| arg1.isEmpty() || arg2.isEmpty()
				|| "other".equalsIgnoreCase(arg1.trim())
				|| "other".equalsIgnoreCase(arg2.trim())
				|| !arg1.trim().equalsIgnoreCase(arg2.trim())) {
			return 0.0f;
		}
		
		return 1.0f;
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
		ColorSkd cs = new ColorSkd();
		String[] args1 = {"Green", "Green"};
		String[] args2 = {"Ballistic Green", "Summer Green"};
		String[] args3 = {"Other", "Other"};
		String[] args4 = {"Other", "Pink"};
		String[] args5 = {"Magenta", "Other"};
		System.out.println("Matching colors: " + cs.compute(args1));
		System.out.println("Non-matching colors: " + cs.compute(args2));
		System.out.println("Other in both colors: " + cs.compute(args3));
		System.out.println("Other in color 1: " + cs.compute(args4));
		System.out.println("Other in color 2: " + cs.compute(args5));
	}
}
