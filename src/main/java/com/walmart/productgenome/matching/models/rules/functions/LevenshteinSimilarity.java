package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class LevenshteinSimilarity extends Function {

	public static final String NAME = "LEVENSHTEIN";
	public static final String DESCRIPTION = "This is the similarity function " +
			"(s(x, y)) resulting from converting the basic edit distance " +
			"function (d(x, y)) as follows: s(x, y) = 1 - d(x, y)/max(len(x), " +
			"len(y)). The edit distance, d(x, y), is given as the minimum " +
			"number of edit operations (deleting a character, inserting a " +
			"character, and substituting one character for another) that " +
			"transform string x to string y. In Levenshtein similarity, each " +
			"edit operation has unit cost. There are other variants (e.g., " +
			"Needleman-Wunch, Smith-Waterman, Smith-Waterman-Gotoh) that may " +
			"assign different costs to different edit operations and/or discount "+
			"for gaps in the strings x and y. Levenshtein similarity is good for" +
			"comparing strings where typographical errors are common (e.g., " +
			"zip codes)";
	public static final int NUM_ARGS = 2;
	
	public LevenshteinSimilarity() {
		super(NAME, DESCRIPTION);
	}

	public LevenshteinSimilarity(String name, String description) {
		super(name, description);
	}

	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if(args.length != NUM_ARGS){
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		AbstractStringMetric metric = new Levenshtein();
		return metric.getSimilarity(args[0], args[1]);
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

}
