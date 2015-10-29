package com.walmart.productgenome.matching.models.rules.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class LengthSkd extends Function implements Module {
	
	public static final String NAME = "LENGTH_SKD";
	public static final String DESCRIPTION = "Module for matching lengths";
	public static final int NUM_ARGS = 2;
	
	private static Set<String> dictionary = new HashSet<String>();

	static {
		// load the predictions from the brand module
		try {
			BufferedReader br1 = new BufferedReader(new FileReader("/Users/patron/Downloads/784_IS/all_apl_dic.txt"));
			String line;
			while ((line = br1.readLine()) != null) {
				String s = line.split("\\t")[0];
				s = s.trim().toLowerCase();
				dictionary.add(s);
			}
			br1.close();
			System.out.println("Loaded values from length dictionary: " + dictionary.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LengthSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public LengthSkd(String name, String description) {
		super(name, description);
	}

	@Override
	public ArgType getArgType() {
		return ArgType.NUMERIC;
	}

	@Override
	public Set<AllTypes> getAllRecommendedTypes() {
		Set<AllTypes> recommendedTypes = new HashSet<AllTypes>();
		recommendedTypes.add(AllTypes.NUMERIC);
		recommendedTypes.add(AllTypes.SINGLE_WORD_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
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
				|| arg1.isEmpty() || arg2.isEmpty()) {
			return 0.0f;
		}
		
		arg1 = arg1.trim();
		arg2 = arg2.trim();
		try {
			Float f1 = Float.parseFloat(arg1);
			Float f2 = Float.parseFloat(arg2);
			//System.out.println("f1: " + f1 + ",  f2: " + f2);
			if (Float.compare(f1, f2) == 0) {
				return 1.0f;
			}
			else {
				return 0.0f;
			}
		}
		catch (NumberFormatException e) {
			//System.out.println("Number format exception");
			return 0.0f;
		}
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

	private Set<String> getCandidates(String[] attributeVals) {
		Set<String> candidates = new HashSet<String>();
		for (String s: attributeVals) {
			if (s == null || s.equalsIgnoreCase("null") || s.isEmpty()) {
				continue;
			}
			String[] vals = s.split("\\s");
			for (String v: vals) {
				v = v.toLowerCase();
				if (dictionary.contains(v)) {
					candidates.add(v);
				}
			}
		}
		return candidates;
	}
	
	public float compute(Tuple tuple1, Tuple tuple2) {
		Attribute attribute = new Attribute("LENGTH_X", Attribute.Type.FLOAT);
		Attribute attribute1 = new Attribute("WIDTH_X", Attribute.Type.FLOAT);
		Attribute attribute2 = new Attribute("HEIGHT_X", Attribute.Type.FLOAT);
		Attribute productNameAttribute = new Attribute("Product_Name", Attribute.Type.TEXT);
		Attribute psdAttribute = new Attribute("Product_Short_Description", Attribute.Type.TEXT);
		Attribute pldAttribute = new Attribute("Product_Long_Description", Attribute.Type.TEXT);
		Float val1 = (Float) tuple1.getAttributeValue(attribute);
		Float val2 = (Float) tuple2.getAttributeValue(attribute);
		String l1 = "";
		if (null != val1) {
			l1 = String.valueOf(val1);
		}
		String l2 = "";
		if (null != val2) {
			l2 = String.valueOf(val2);
		}
		Float wval2 = (Float) tuple2.getAttributeValue(attribute1);
		String w2 = "";
		if (null != wval2) {
			w2 = String.valueOf(wval2);
		}
		Float hval2 = (Float) tuple2.getAttributeValue(attribute2);
		String h2 = "";
		if (null != hval2) {
			h2 = String.valueOf(hval2);
		}
		String[] otherAttributeValues1 = {(String) tuple1.getAttributeValue(productNameAttribute),
				(String) tuple1.getAttributeValue(psdAttribute), (String) tuple1.getAttributeValue(pldAttribute)};
		String[] otherAttributeValues2 = {(String) tuple2.getAttributeValue(productNameAttribute),
				(String) tuple2.getAttributeValue(pldAttribute)};
		return computeValue(l1, l2, w2, h2, otherAttributeValues1, otherAttributeValues2);
	}
	
	public float computeValue(String l1, String l2, String w2, String h2,
			String[] otherAttributeValues1, String[] otherAttributeValues2) {
		if (l1 != null
				&& !l1.toLowerCase().equals("null")
				&& !l1.isEmpty()
				&& l2 != null
				&& !l2.toLowerCase().equals("null")
				&& !l2.isEmpty()) {
			l1 = l1.trim();
			l2 = l2.trim();
			try {
				Float f1 = Float.parseFloat(l1);
				Float f2 = Float.parseFloat(l2);
				//System.out.println("f1: " + f1 + ",  f2: " + f2);
				if (Float.compare(f1, f2) == 0) {
					return 1.0f;
				}
//				else {
//					return 0.0f;
//				}
			}
			catch (NumberFormatException e) {
				//System.out.println("Number format exception");
				//return 0.0f;
			}
		}

		if (l1 != null
				&& !l1.toLowerCase().equals("null")
				&& !l1.isEmpty()
				&& w2 != null
				&& !w2.toLowerCase().equals("null")
				&& !w2.isEmpty()) {
			l1 = l1.trim();
			w2 = w2.trim();
			try {
				Float f1 = Float.parseFloat(l1);
				Float f2 = Float.parseFloat(w2);
				//System.out.println("f1: " + f1 + ",  f2: " + f2);
				if (Float.compare(f1, f2) == 0) {
					return 1.0f;
				}
//				else {
//					return 0.0f;
//				}
			}
			catch (NumberFormatException e) {
				//System.out.println("Number format exception");
				//return 0.0f;
			}
		}
		
		if (l1 != null
				&& !l1.toLowerCase().equals("null")
				&& !l1.isEmpty()
				&& h2 != null
				&& !h2.toLowerCase().equals("null")
				&& !h2.isEmpty()) {
			l1 = l1.trim();
			h2 = h2.trim();
			try {
				Float f1 = Float.parseFloat(l1);
				Float f2 = Float.parseFloat(h2);
				//System.out.println("f1: " + f1 + ",  f2: " + f2);
				if (Float.compare(f1, f2) == 0) {
					return 1.0f;
				}
//				else {
//					return 0.0f;
//				}
			}
			catch (NumberFormatException e) {
				//System.out.println("Number format exception");
				//return 0.0f;
			}
		}
		
		if (l1 != null
				&& !l1.toLowerCase().equals("null")
				&& !l1.isEmpty()) {
			// val2 does not exist, probe val1 into other attributes 2
			// System.out.println("Color 2 does not exist, color1 found in text of 2");
			for (String s: otherAttributeValues2) {
				if (s.toLowerCase().contains(l1.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		if (l2 != null
				&& !l2.toLowerCase().equals("null")
				&& !l2.isEmpty()) {
			// color1 does not exist, probe color2 into other attributes 1
			for (String s: otherAttributeValues1) {
				if (s.toLowerCase().contains(l2.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		// both val1 and val2 do not exist
		// probe the dictionary
		Set<String> candidateVals1 = getCandidates(otherAttributeValues1);
		Set<String> candidateVals2 = getCandidates(otherAttributeValues2);

		for (String s: candidateVals1) {
			if (candidateVals2.contains(s)) {
				return 1.0f;
			}
		}
		return 0.0f;
	}
	
	public static void main(String[] args){
		LengthSkd ls = new LengthSkd();
		String[] args1 = {"14.5", "14.5"};
		String[] args2 = {"14", "14.0"};
		String[] args3 = {"5.1", "5.2"};
		String[] args4 = {"", ""};
		String[] args5 = {"", "6.5"};
		String[] args6 = {"3.5", ""};
		String[] args7 = {"1.6\"", "1.6 inch"};
		System.out.println("Matching lengths: " + ls.compute(args1));
		System.out.println("Matching lengths: " + ls.compute(args2));
		System.out.println("Non-matching lengths: " + ls.compute(args3));
		System.out.println("Both lengths missing: " + ls.compute(args4));
		System.out.println("Length 1 missing: " + ls.compute(args5));
		System.out.println("Length 2 missing: " + ls.compute(args6));
		System.out.println("Not a number: " + ls.compute(args7));
	}
}
