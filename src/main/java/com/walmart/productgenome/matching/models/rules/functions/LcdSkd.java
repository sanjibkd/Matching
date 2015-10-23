package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class LcdSkd extends Function {
	public static final String NAME = "LCD_SKD";
	public static final String DESCRIPTION = "Module for matching laptop compartment dimensions";
	public static final int NUM_ARGS = 2;

	public LcdSkd() {
		super(NAME, DESCRIPTION);
	}

	public LcdSkd(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.STRING;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.SINGLE_WORD_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_MEDIUM_STRING);
		return recommendedTypes;
	}

	@Override
	public Object compute(String[] args) throws IllegalArgumentException {
		if (args.length != NUM_ARGS) {
			throw new IllegalArgumentException("Expected number of arguments: " + NUM_ARGS);
		}

		String arg1 = args[0];
		String arg2 = args[1];
		if (arg1 == null
				|| arg2 == null
				|| arg1.toLowerCase().equals("null")
				|| arg2.toLowerCase().equals("null")
				|| arg1.isEmpty() || arg2.isEmpty()
				|| !arg1.trim().equalsIgnoreCase(arg2.trim())) {
			return 0.0f;
		}

		return 1.0f;
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
		LcdSkd cs = new LcdSkd();
		String[] args2 = {"4.5 height x 14.5 width x 18.5 depth", "16.5' x 12.5' x 2.3'"};
		String[] args1 = {"14.5' x 11' x 2'", "14.5' x 11' x 2'"};
		String[] args3 = {"", ""};
		String[] args4 = {"", "16.5' x 12.5' x 2.3'"};
		String[] args5 = {"16.5' x 12.5' x 2.3'", ""};
		System.out.println("Matching lcds: " + cs.compute(args1));
		System.out.println("Non-matching lcds: " + cs.compute(args2));
		System.out.println("Missing both lcds: " + cs.compute(args3));
		System.out.println("Missing lcd 1: " + cs.compute(args4));
		System.out.println("Missing lcd 2: " + cs.compute(args5));
	}
}
