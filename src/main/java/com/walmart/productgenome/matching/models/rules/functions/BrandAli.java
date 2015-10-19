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

public class BrandAli extends Function {
	public static final String NAME = "BRAND_ALI";
	public static final String DESCRIPTION = "Module for matching brands";
	public static final int NUM_ARGS = 2;
	
	private static Map<String, Float> brandPredictions = new HashMap<String, Float>();
	
	static {
		// load the predictions from the brand module
		try {
			CSVParser p = new CSVParser(new FileReader("/Users/patron/Downloads/784_IS/brand_predictions.csv"));
			List<CSVRecord> records = p.getRecords();
			for (CSVRecord r: records) {
				String pairId = r.get(0).trim();
				String label = r.get(1).trim();
				Float prediction = 0.0f;
				if ("Y".equalsIgnoreCase(label)) {
					prediction = 1.0f;
				}
				else if ("D".equalsIgnoreCase(label)) {
					prediction = -1.0f;
				}
				brandPredictions.put(pairId, prediction);
			}
			System.out.println("Loaded predictions from brand module: " + brandPredictions.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public BrandAli() {
		super(NAME, DESCRIPTION);
	}
	
	public BrandAli(String name, String description) {
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
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].trim();
		String arg2 = args[1].trim();
		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		
		String key = arg1 + "-" + arg2;
		if (brandPredictions.containsKey(key)) {
			//System.out.println("Found in map");
			simValue = brandPredictions.get(key);
		}
		else {
			//System.out.println("Not found in map");
			simValue = 0.0f;
		}
		return simValue;
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
		BrandAli ba = new BrandAli();
		String[] args1 = {"16539484", "16539487#eBags"};
		String[] args2 = {"12181620", "12181620#eBags"};
		String[] args3 = {"1", "2"};
		System.out.println("Y: " + ba.compute(args1));
		System.out.println("D: " + ba.compute(args2));
		System.out.println("N: " + ba.compute(args3));
	}
}
