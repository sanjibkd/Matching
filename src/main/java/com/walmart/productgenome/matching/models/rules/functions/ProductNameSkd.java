package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ProductNameSkd extends Function {
	public static final String NAME = "PRODUCT_NAME_SKD";
	public static final String DESCRIPTION = "Modules for matching product names";
	public static final int NUM_ARGS = 2;
	
	public ProductNameSkd() {
		super(NAME, DESCRIPTION);
	}

	public ProductNameSkd(String name, String description) {
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
		
		arg1 = arg1.toLowerCase().replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
		arg2 = arg2.toLowerCase().replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
		
		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		AbstractStringMetric metric = new JaccardSimilarity();
		float jacScore = metric.getSimilarity(arg1, arg2);
		if (jacScore > 0.8) {
			return 1.0f;
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
		Function productNameSkd = new MaterialSkd();
		String[] args1 = {"Back-UPS 500 System", "Back-UPS 500 System"};
		String[] args2 = {"Targus Gravity 15.4\" Laptop Backpack", "Back-UPS 500 System"};
		System.out.println("Matching product names: " + productNameSkd.compute(args1));
		System.out.println("Matching product names: " + productNameSkd.compute(args2));
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
