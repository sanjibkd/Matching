package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ExactMatch extends Function {

	public static final String NAME = "EXACT_MATCH";
	public static final String DESCRIPTION = "Returns 1 if two strings match otherwise 0";
	public static final int NUM_ARGS = 2;
	
	public ExactMatch() {
		super(NAME, DESCRIPTION);
	}

	public ExactMatch(String name, String description) {
		super(name, description);
	}

	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}
		
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].toLowerCase();
		String arg2 = args[1].toLowerCase();
		
		if (arg1.equals(arg2)) {
			return 1.0f;
		}
		else {
			return 0.0f;
		}
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
