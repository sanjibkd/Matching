package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

public class JaroWinklerSimilarityFunction extends Function {
	
	public static final String NAME = "JARO_WINKLER_SIMILARITY";
	public static final String DESCRIPTION = "Jaro Winkler similarity";
	public static final int NUM_ARGS = 2;
	
	public JaroWinklerSimilarityFunction() {
		super(NAME, DESCRIPTION);
	}

	public JaroWinklerSimilarityFunction(String name, String description){
		super(name, description);
	}
	
	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].toLowerCase();
		String arg2 = args[1].toLowerCase();
    
		AbstractStringMetric metric = new JaroWinkler();
		return metric.getSimilarity(arg1, arg2);
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
		return recommendedTypes;
	}
}
