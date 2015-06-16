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

		SoftTFIDF metric = new SoftTFIDF();
		return (float)metric.score(newArg0, newArg1);
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
}
