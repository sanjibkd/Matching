package com.walmart.productgenome.matching.models.rules.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class VariationSkd extends Function implements Module {

	public static final String NAME = "VARIATION_SKD";
	public static final String DESCRIPTION = "Module for checking variations";
	public static final int NUM_ARGS = 2;
	
	public VariationSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public VariationSkd(String name, String description) {
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
	
	public String getLongString(Tuple tuple) {
		Map<Attribute, Object> data = tuple.getData();
		StringBuilder sb = new StringBuilder();
		for (Attribute a : data.keySet()) {
			String name = a.getName();
			if ("UPC".equalsIgnoreCase(name)
					|| "Manufacturer_Part_Number".equalsIgnoreCase(name)
					|| "UPC_X".equalsIgnoreCase(name)
					|| "MPN_X".equalsIgnoreCase(name)
					|| "Product_Short_Description".equalsIgnoreCase(name)) {
				continue;
			}
			sb.append(data.get(a));
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public float compute(Tuple tuple1, Tuple tuple2) {
		String s1 = getLongString(tuple1).toLowerCase().trim();
		String s2 = getLongString(tuple2).toLowerCase().trim();
		AbstractStringMetric metric = new JaccardSimilarity();
		return metric.getSimilarity(s1, s2);
	}
}
