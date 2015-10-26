package com.walmart.productgenome.matching.models.rules.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class ColorSkd extends Function implements Module {

	public static final String NAME = "COLOR_SKD";
	public static final String DESCRIPTION = "Module for matching colors";
	public static final int NUM_ARGS = 2;
	
	private static Set<String> dictionary = new HashSet<String>();

	static {
		// load the predictions from the brand module
		try {
			BufferedReader br1 = new BufferedReader(new FileReader("/Users/patron/Downloads/784_IS/elec_color_dic.txt"));
			BufferedReader br2 = new BufferedReader(new FileReader("/Users/patron/Downloads/784_IS/elec_actual_color_dic.txt"));
			String line;
			while ((line = br1.readLine()) != null) {
				String s = line.split("\\t")[0];
				s = s.trim().toLowerCase();
				if ("other".equalsIgnoreCase(s)) {
					continue;
				}
				dictionary.add(s);
			}
			br1.close();
			while ((line = br2.readLine()) != null) {
				String s = line.split("\\t")[0];
				s = s.trim().toLowerCase();
				if ("other".equalsIgnoreCase(s)) {
					continue;
				}
				dictionary.add(s);
			}
			br2.close();
			System.out.println("Loaded values from color and actual color dictionaries: " + dictionary.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ColorSkd() {
		super(NAME, DESCRIPTION);
	}
	
	public ColorSkd(String name, String description) {
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
				|| "other".equalsIgnoreCase(arg1.trim())
				|| "other".equalsIgnoreCase(arg2.trim())
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
		Attribute materialAttribute = new Attribute("COLOR_X", Attribute.Type.TEXT);
		Attribute productNameAttribute = new Attribute("Product_Name", Attribute.Type.TEXT);
		Attribute psdAttribute = new Attribute("Product_Short_Description", Attribute.Type.TEXT);
		Attribute pldAttribute = new Attribute("Product_Long_Description", Attribute.Type.TEXT);
		String material1 = (String) tuple1.getAttributeValue(materialAttribute);
		String material2 = (String) tuple2.getAttributeValue(materialAttribute);
		String[] otherAttributeValues1 = {(String) tuple1.getAttributeValue(productNameAttribute),
				(String) tuple1.getAttributeValue(psdAttribute), (String) tuple1.getAttributeValue(pldAttribute)};
		String[] otherAttributeValues2 = {(String) tuple2.getAttributeValue(productNameAttribute),
				(String) tuple2.getAttributeValue(pldAttribute)};
		return computeValue(material1, material2, otherAttributeValues1, otherAttributeValues2);
	}
	
	public float computeValue(String color1, String color2,
			String[] otherAttributeValues1, String[] otherAttributeValues2) {
		if (color1 != null
				&& !color1.toLowerCase().equals("null")
				&& !color1.isEmpty()
				&& !color1.equalsIgnoreCase("other")
				&& color2 != null
				&& !color2.toLowerCase().equals("null")
				&& !color2.isEmpty()
				&& !color2.equalsIgnoreCase("other")) {
			color1 = color1.toLowerCase().replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
			color2 = color2.toLowerCase().replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
			if (color1.equalsIgnoreCase(color2)) {
				return 1.0f;
			}
//			else {
//				return 0.0f;
//			}
		}

		if (color1 != null
				&& !color1.toLowerCase().equals("null")
				&& !color1.isEmpty()
				&& !color1.equalsIgnoreCase("other")) {
			// color2 does not exist, probe color1 into other attributes 2
			// System.out.println("Color 2 does not exist, color1 found in text of 2");
			for (String s: otherAttributeValues2) {
				if (s.toLowerCase().contains(color1.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		if (color2 != null
				&& !color2.toLowerCase().equals("null")
				&& !color2.isEmpty()
				&& !color2.equalsIgnoreCase("other")) {
			// color1 does not exist, probe color2 into other attributes 1
			for (String s: otherAttributeValues1) {
				if (s.toLowerCase().contains(color2.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		// both color1 and color2 do not exist
		// probe the dictionary
		Set<String> candidateColors1 = getCandidates(otherAttributeValues1);
		Set<String> candidateColors2 = getCandidates(otherAttributeValues2);

		for (String s: candidateColors1) {
			if (candidateColors2.contains(s)) {
				System.out.println("Dictionary match: " + s);
				return 1.0f;
			}
		}
		return 0.0f;
	}
	
	public static void main(String[] args){
		ColorSkd cs = new ColorSkd();
		String[] args1 = {"Green", "Green"};
		String[] args2 = {"Ballistic Green", "Summer Green"};
		String[] args3 = {"Other", "Other"};
		String[] args4 = {"Other", "Pink"};
		String[] args5 = {"Magenta", "Other"};
		System.out.println("Matching colors: " + cs.compute(args1));
		System.out.println("Non-matching colors: " + cs.compute(args2));
		System.out.println("Other in both colors: " + cs.compute(args3));
		System.out.println("Other in color 1: " + cs.compute(args4));
		System.out.println("Other in color 2: " + cs.compute(args5));
		
		Attribute attribute = new Attribute("COLOR_X", Attribute.Type.TEXT);
		Attribute productNameAttribute = new Attribute("Product_Name", Attribute.Type.TEXT);
		Attribute psdAttribute = new Attribute("Product_Short_Description", Attribute.Type.TEXT);
		Attribute pldAttribute = new Attribute("Product_Long_Description", Attribute.Type.TEXT);
		
		Map<Attribute, Object> data1 = new HashMap<Attribute, Object>();
		Map<Attribute, Object> data2 = new HashMap<Attribute, Object>();
		
		String material1 = "Navy/Golden Brown";
		String material2 = "White/Pink Flower";
		String[] others1 = {"Looptworks Upcycled 13\" Hoptu Laptop Sleeve",
				"Looptworks Upcycled 13\" Hoptu Laptop Sleeve Made to fit 13\" laptops, "
				+ "with a brushed nylon liner, this wet-suit warrior will keep your web-surfer "
				+ "protected from the elements. 100% Upcycled. Made from pre-consumer excess. "
				+ "Sized to fit 13 inch laptops. Two front pockets for thumb drives or keys. "
				+ "Padded sleeve, that will keep your \"precious\" cozy and protected. The material "
				+ "for the Device Sleeve was rescued from a wetsuit factory. Internal zipper guards "
				+ "protects the laptop against abrasion. Featured on in Oprah Magazine",
				"The Designer Sleeve 13\" laptop case is a fun yet functional way to protect your "
				+ "laptop computer. The thick, durable neoprene rubber will cushion your laptop for "
				+ "maximum moisture, shock and scratch protection. The sleek design safely slides in "
				+ "and out of your backpack, briefcase or luggage with ease. In addition, the "
				+ "external zippered pocket allows you to securely carry other items. Product Material: Neoprene "
				+ "Product Weight: 0.69 lbs. Laptop Compartment Dimensions: 13\" x 10.3\" x 1.5\" Thick, durable "
				+ "neoprene cushions your laptop for maximum moisture, shock and scratch protection Zippered "
				+ "external pocket provides secure storage for powercords, memory and more! Sleek design "
				+ "slides easily in and out of your backpack, briefcase or luggage with ease. Many designs to choose from Washable"};
		String[] others2 = {"Looptworks Upcycled 13\" Hoptu Laptop Sleeve",
				"The Designer Sleeve 13\" laptop case is a fun yet functional way to protect your "
				+ "laptop computer. The thick, durable neoprene rubber will cushion your laptop for "
				+ "maximum moisture, shock and scratch protection. The sleek design safely slides in "
				+ "and out of your backpack, briefcase or luggage with ease. In addition, the "
				+ "external zippered pocket allows you to securely carry other items. Product Material: Neoprene "
				+ "Product Weight: 0.69 lbs. Laptop Compartment Dimensions: 13\" x 10.3\" x 1.5\" Thick, durable "
				+ "neoprene cushions your laptop for maximum moisture, shock and scratch protection Zippered "
				+ "external pocket provides secure storage for powercords, memory and more! Sleek design "
				+ "slides easily in and out of your backpack, briefcase or luggage with ease. Many designs to choose from Washable"};
		
		data1.put(productNameAttribute, others1[0]);
		data1.put(psdAttribute, others1[1]);
		data1.put(pldAttribute, others1[2]);
		data1.put(attribute, material1);
		
		data2.put(productNameAttribute, others2[0]);
		data2.put(pldAttribute, others2[1]);
		data2.put(attribute, material2);
		
		if (cs instanceof Module) {
			Module m = (Module) cs;
			Tuple tuple1 = new Tuple(data1);
			Tuple tuple2 = new Tuple(data2);
			System.out.println("Matching colors: " + m.compute(tuple1, tuple2));
		}
	}
}
