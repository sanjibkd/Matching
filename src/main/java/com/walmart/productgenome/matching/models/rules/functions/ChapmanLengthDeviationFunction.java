package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanLengthDeviation;

public class ChapmanLengthDeviationFunction extends Function {
	
	public static final String NAME = "CHAPMAN_LENGTH_DEVIATION";
	public static final String DESCRIPTION = "Normalised Chapman length deviation";
	public static final int NUM_ARGS = 2;
	
	public ChapmanLengthDeviationFunction() {
		super(NAME, DESCRIPTION);
	}

	public ChapmanLengthDeviationFunction(String name, String description){
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
		AbstractStringMetric metric = new ChapmanLengthDeviation();
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
	
	public static void main(String[] args){
		Function cld = new ChapmanLengthDeviationFunction();
		System.out.println(cld.getSignature());
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		return new HashSet<AllTypes>();
	}
}
