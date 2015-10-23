package com.walmart.productgenome.matching.models.rules.functions;

import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ProductTypeSkd extends Function {

	public static final String NAME = "PRODUCT_TYPE_SKD";
	public static final String DESCRIPTION = "Module for matching product types";
	public static final int NUM_ARGS = 2;

	public ProductTypeSkd() {
		super(NAME, DESCRIPTION);
	}

	public ProductTypeSkd(String name, String description) {
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
		ProductTypeSkd cs = new ProductTypeSkd();
		String[] args1 = {"Laptop Bags & Cases", "Laptop Bags & Cases"};
		String[] args2 = {"Televisions", "Laptop Bags & Cases"};
		String[] args3 = {"", ""};
		String[] args4 = {"", "Laptop Bags & Cases"};
		String[] args5 = {"Laptop Bags & Cases", ""};
		System.out.println("Matching product types: " + cs.compute(args1));
		System.out.println("Non-matching product types: " + cs.compute(args2));
		System.out.println("Missing both product types: " + cs.compute(args3));
		System.out.println("Missing product type 1: " + cs.compute(args4));
		System.out.println("Missing product type 2: " + cs.compute(args5));
	}
}
