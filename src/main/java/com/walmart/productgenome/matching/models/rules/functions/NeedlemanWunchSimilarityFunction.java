package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

public class NeedlemanWunchSimilarityFunction extends Function {
	
	public static final String NAME = "NEEDLEMAN_WUNCH";
	public static final String DESCRIPTION = "The Needleman-Wunch similarity " +
			"function generalizes the Levenshtein similarity function. " +
			"Specifically it is computed by assigning a score to each alignment " +
			"between the two input strings and choosing the maximal score. The " +
			"score of an alignment is computed using a score matrix and a gap " +
			"penalty. The score matrix generalizes the edit costs (e.g., o and " +
			"0 may have a higher score than that between a and 0). The gap " +
			"penalty is used to generalize the cost of insertion and deletion " +
			"operations into gaps between the two strings. Typically used in " +
			"bioinformatics to align protein or nucleotide sequences.";
	public static final int NUM_ARGS = 2;
	
	public NeedlemanWunchSimilarityFunction() {
		super(NAME, DESCRIPTION);
	}

	public NeedlemanWunchSimilarityFunction(String name, String description){
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
		AbstractStringMetric metric = new NeedlemanWunch();
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
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
		return recommendedTypes;
	}
}
