package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

public class JaccardJing extends Function{

	public static final String NAME = "JACCARD_JING";
	public static final String DESCRIPTION = "Normalised Jaccard Similarity by Jing";
	public static final int NUM_ARGS = 2;
	
	public JaccardJing() {
		super(NAME, DESCRIPTION);
	}

	public JaccardJing(String name, String description){
		super(name, description);
	}
	
	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
		if(args.length != NUM_ARGS){
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
    // TODO: Sanjib review.
		if (args[0] == null || args[1] == null) {
      return 0.0f;
    }
		if (args[0].toLowerCase().equals("null") || args[1].toLowerCase().equals("null")) {
		  return 0.0f;
		}
		if (args[0].isEmpty() || args[1].isEmpty()) {
		  return 0.0f;
		}
		
		String newArg0 = args[0].toLowerCase();
		String newArg1 = args[1].toLowerCase();
		
    newArg0 = newArg0.replaceAll("[^\\dA-Za-z ]", "");
    newArg1 = newArg1.replaceAll("[^\\dA-Za-z ]", "");

	  newArg0 = newArg0.replaceAll("[\\.\\d]+[ ]*((oz)|(ounce))", " ");
	  newArg1 = newArg1.replaceAll("[\\.\\d]+[ ]*((oz)|(ounce))", " ");
  	
    newArg0 = newArg0.replaceAll("pack of \\d+", " ");
	  newArg1 = newArg1.replaceAll("pack of \\d+", " ");

	  newArg0 = newArg0.replaceAll("\\d+[ ]*((cnt)|(ct)|(count))", " ");
	  newArg1 = newArg1.replaceAll("\\d+[ ]*((cnt)|(ct)|(count))", " ");

    AbstractStringMetric metric = new JaccardSimilarity();
		return metric.getSimilarity(newArg0, newArg1);
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
		Function jac = new JaccardJing();
		System.out.println(jac.getSignature());
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		return new HashSet<AllTypes>();
	}

}
