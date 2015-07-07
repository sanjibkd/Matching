package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.wcohen.ss.SoftTFIDF;

public class SoftTfIdfSimilarityFunction extends Function {

	public static final String NAME = "SOFT_TF_IDF_SIMILARITY";
	public static final String DESCRIPTION = "Soft TF-IDF similarity metric. " +
			"A variant of TF-IDF similarity metric that " +
			"uses soft token-matching. Specifically, tokens are considered a " +
			"partial match if they get a good score using an inner string " +
			"comparator. The inner string comparator is the Jaro-Winkler " +
			"similarity metric with a threshold of 0.9. Good for long strings.";
	public static final int NUM_ARGS = 2;

	public SoftTfIdfSimilarityFunction() {
		super(NAME, DESCRIPTION);
	}

	public SoftTfIdfSimilarityFunction(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.STRING;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.MULTI_WORD_LONG_STRING);
		recommendedTypes.add(AllTypes.SET_VALUED);
		return recommendedTypes;
	}

	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments " +
					"for TF/IDF function is " + NUM_ARGS);
		}
		float simValue = handleMissingValue(args[0], args[1]);
		if (simValue != 0.0f)
			return simValue;
		
		String arg1 = args[0].toLowerCase();
		String arg2 = args[1].toLowerCase();

		SoftTFIDF metric = new SoftTFIDF();
		return (float)metric.score(arg1, arg2);
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
	
	public static void main(String[] args) {
		String[] s = {"Casio Mens G-Shock Classic Digital Watch",
				"Casio G7301B-3V G-Shock Classic Digital Sports Watch"};
		Function stfidf = new SoftTfIdfSimilarityFunction();
		System.out.println(stfidf.compute(s));
	}
}
