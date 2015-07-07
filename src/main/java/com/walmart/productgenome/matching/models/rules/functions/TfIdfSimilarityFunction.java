package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.wcohen.ss.TFIDF;
import com.wcohen.ss.api.StringWrapper;

public class TfIdfSimilarityFunction extends Function {

	public static final String NAME = "TF_IDF_SIMILARITY";
	public static final String DESCRIPTION = "TF-IDF similarity metric. " +
			"Typically used to consider the relevance of " +
			"pages for web page searches. A vector based approach whereby " +
			"weights are applied to terms in respect to their frequency within " +
			"the predefined corpus, (typically the internet) and the inverse " +
			"frequency within the test string or document. Good " +
			"for long strings (e.g., product descriptions, product reviews).";
	public static final int NUM_ARGS = 2;

	public TfIdfSimilarityFunction() {
		super(NAME, DESCRIPTION);
	}

	public TfIdfSimilarityFunction(String name, String description) {
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

		TFIDF metric = new TFIDF();
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
		Function f = new TfIdfSimilarityFunction("Tf_Idf", "TF/IDF similarity");
		String s1 = "";//"What is your name? How old are you? What do you love?";
		String s2 = "null";//"My nam is Sanjib. I am thirty years old. I luve playing.";
		String[] args1 = {s1, s2};
		//System.out.println(f.compute(args1));
		TFIDF metric = new TFIDF();
		//SoftTokenFelligiSunter metric = new SoftTokenFelligiSunter();
		//SoftTFIDF metric = new SoftTFIDF();
		//JaroWinklerTFIDF metric = new JaroWinklerTFIDF();
		StringWrapper sw1 = metric.prepare(s1);
		StringWrapper sw2 = metric.prepare(s2);
		System.out.println(metric.explainScore(sw1, sw2));
		System.out.println(metric.toString());
		//System.out.println(new CosineSimilarityFunction().compute(args1));
		AbstractStringMetric metric1 = new QGramsDistance();
		System.out.println(metric1.getSimilarity("", ""));
	}
}
