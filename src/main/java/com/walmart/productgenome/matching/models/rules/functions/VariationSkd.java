package com.walmart.productgenome.matching.models.rules.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class VariationSkd extends Function implements Module {

	public static final String NAME = "VARIATION_SKD";
	public static final String DESCRIPTION = "Module for checking variations";
	public static final int NUM_ARGS = 2;

	public VariationSkd() {
		super(NAME, DESCRIPTION);
	}

	public VariationSkd(String name, String description) {
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

	public Map<String, Integer> getWordCounts(Tuple tuple) {
		Map<Attribute, Object> data = tuple.getData();
		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
		for (Attribute a : data.keySet()) {
			String name = a.getName();
			if ("Product_Name".equalsIgnoreCase(name)
					|| "Product_Long_Description".equalsIgnoreCase(name)
					|| "COLOR_X".equalsIgnoreCase(name)
					|| "LENGTH_X".equalsIgnoreCase(name)
					|| "WIDTH_X".equalsIgnoreCase(name)
					|| "HEIGHT_X".equalsIgnoreCase(name)) {
				Object v = data.get(a);
				if (null != v ) {
					switch(a.getType()) {
					case TEXT:
						String s = (String) v;
						if ("COLOR_X".equalsIgnoreCase(name) && "other".equalsIgnoreCase(s)) {
							continue;
						}
						s = s.toLowerCase().replaceAll(",","");
						String[] vals = s.split("\\s");
						for (String val: vals) {
							String val1 = val.toLowerCase();
							int count = 0;
							if (wordCounts.containsKey(val1)) {
								count = wordCounts.get(val1);
							}
							wordCounts.put(val1, count + 1);
						}
						break;
					case FLOAT:
						String val1 = String.valueOf(v);
						int count = 0;
						if (wordCounts.containsKey(val1)) {
							count = wordCounts.get(val1);
						}
						wordCounts.put(val1, count + 1);
						break;
					default:
						break;
					}
				}
			}
		}
		return wordCounts;
	}
	
	public Set<String> getWords(Tuple tuple) {
		Map<Attribute, Object> data = tuple.getData();
		Set<String> words = new HashSet<String>();
		for (Attribute a : data.keySet()) {
			String name = a.getName();
			if ("Product_Name".equalsIgnoreCase(name)
					|| "Product_Long_Description".equalsIgnoreCase(name)
					|| "COLOR_X".equalsIgnoreCase(name)
					|| "LENGTH_X".equalsIgnoreCase(name)
					|| "WIDTH_X".equalsIgnoreCase(name)
					|| "HEIGHT_X".equalsIgnoreCase(name)) {
				Object v = data.get(a);
				if (null != v ) {
					switch(a.getType()) {
					case TEXT:
						String s = (String) v;
						if ("COLOR_X".equalsIgnoreCase(name) && "other".equalsIgnoreCase(s)) {
							continue;
						}
						s = s.toLowerCase().replaceAll(",","");
						String[] vals = s.split("\\s");
						for (String val: vals) {
							words.add(val.toLowerCase());
						}
						break;
					case FLOAT:
						words.add(String.valueOf(v));
						break;
					default:
						break;
					}
				}
			}
		}
		return words;
	}
	
	/*
	public float compute(Tuple tuple1, Tuple tuple2) {
		Set<String> s1 = getWords(tuple1);
		Set<String> s2 = getWords(tuple2);
		Set<String> s1Ands2 = new HashSet<String>(s1);
		s1Ands2.retainAll(s2);
		Set<String> s1Ors2 = new HashSet<String>(s1);
		s1Ors2.addAll(s2);
		s1Ors2.removeAll(s1Ands2);
		Attribute idAttribute = new Attribute("id", Attribute.Type.TEXT);
		String id1 = (String) tuple1.getAttributeValue(idAttribute);
		String id2 = (String) tuple2.getAttributeValue(idAttribute);
		if ("9193948".equals(id1) && "9193948#eBags".equals(id2)
				|| "9447294".equals(id1) && "9447294#eBags".equals(id2)) {
			System.out.println("Different words for (" + id1 + ", " + id2 + "): ");
			for (String s: s1Ors2) {
				System.out.print(s + ", ");
			}
			System.out.println();
		}
		return (float) s1Ors2.size();
	}
	*/
	
	public float compute(Tuple tuple1, Tuple tuple2) {
		Map<String, Integer> s1 = getWordCounts(tuple1);
		Map<String, Integer> s2 = getWordCounts(tuple2);
		Set<String> differentWords = new HashSet<String>();
		int sum = 0;
		for (String s: s1.keySet()) {
			int count1 = s1.get(s);
			int count2 = 0;
			if (s2.containsKey(s)) {
				count2 = s2.get(s);
				s2.remove(s);
			}
			int diff = Math.abs(count1 - count2);
			if (diff != 0) {
				sum += diff;
				differentWords.add(s);
			}
		}
		for (String s: s2.keySet()) {
			sum += s2.get(s);
			differentWords.add(s);
		}
		
		Attribute idAttribute = new Attribute("id", Attribute.Type.TEXT);
		String id1 = (String) tuple1.getAttributeValue(idAttribute);
		String id2 = (String) tuple2.getAttributeValue(idAttribute);
		if ("9193948".equals(id1) && "9193948#eBags".equals(id2)
				|| "9447294".equals(id1) && "9447294#eBags".equals(id2)
				|| sum != differentWords.size()) {
			System.out.println("Different words for (" + id1 + ", " + id2 + "): ");
			for (String s: differentWords) {
				System.out.print(s + ", ");
			}
			System.out.println();
			System.out.println("sum: " + sum + ", #diff: " + differentWords.size());
		}
		return (float) sum;
	}
	
	public static void main(String[] args){
		VariationSkd vs = new VariationSkd();
		Map<Attribute, Object> data1 = new HashMap<Attribute, Object>();
		Map<Attribute, Object> data2 = new HashMap<Attribute, Object>();
		
		String pn1 = "ATV Logic Hi-Capacity ATV Pack - Mossy Oak";
		String pn2 = "ATV Logic Hi-Capacity ATV Pack - Mossy Oak";
		String pld1 = "This Hi-Capacity ATV Pack fits most ATV front and rear racks perfectly. "
				+ "The spacious compartment is padded and lined with a heavy duty RF-welded liner "
				+ "to protect your gear or keep food and drinks cold. Equipped with storm flaps to "
				+ "keep moisture from seeping through the zippers and deluxe quiet-riding zipper pulls. "
				+ "Stays secured to ATV racks with 7 pair of durable straps and ladder lock buckles. "
				+ "Constructed of rugged 600-denier polyester water-resistant pack material. "
				+ "Product Material: 600 Denier Polyester - Water Resistant "
				+ "Product Weight: 4.40 lbs. "
				+ "Hi-capacity design fits most ATV rear racks perfectly. "
				+ "The spacious compartment is padded and lined with a heavy duty RF-welded liner to "
				+ "protect your gear or keep food and drinks cold. "
				+ "Large 4,300 cubic inch storage. "
				+ "The zippers are covered by storm flaps and have deluxe quiet-riding zipper pulls. "
				+ "Attaches to ATV with 7 pairs of durable straps and ladder lock buckles. "
				+ "Rugged water-resistant 600-denier pack material.";
		String pld2 = "This Hi-Capacity ATV Pack fits most ATV front and rear racks perfectly. "
				+ "The spacious compartment is padded and lined with a heavy duty RF-welded liner "
				+ "to protect your gear or keep food and drinks cold. Equipped with storm flaps to "
				+ "keep moisture from seeping through the zippers and deluxe quiet-riding zipper pulls. "
				+ "Stays secured to ATV racks with 7 pair of durable straps and ladder lock buckles. "
				+ "Constructed of rugged 600-denier polyester water-resistant pack material. "
				+ "Product Material: 600 Denier Polyester - Water Resistant "
				+ "Product Weight: 4.40 lbs. "
				+ "Hi-capacity design fits most ATV rear racks perfectly. "
				+ "The spacious compartment is padded and lined with a heavy duty RF-welded liner to "
				+ "protect your gear or keep food and drinks cold. "
				+ "Large 4300 cubic inch storage. "
				+ "The zippers are covered by storm flaps and have deluxe quiet-riding zipper pulls. "
				+ "Attaches to ATV with 7 pairs of durable straps and ladder lock buckles. "
				+ "Rugged water-resistant 600-denier pack material.";
		String color1 = "Mossy Oak";
		String color2 = "Mossy Oak";
		String l1 = "15.0";
		String l2 = "15.0";
		String w1 = "9.5";
		String w2 = "9.5";
		String h1 = "17.5";
		String h2 = "17.5";
		
		Attribute productNameAttribute = new Attribute("Product_Name", Attribute.Type.TEXT);
		Attribute pldAttribute = new Attribute("Product_Long_Description", Attribute.Type.TEXT);
		Attribute colorAttribute = new Attribute("COLOR_X", Attribute.Type.TEXT);
		Attribute lengthAttribute = new Attribute("LENGTH_X", Attribute.Type.FLOAT);
		Attribute widthAttribute = new Attribute("WIDTH_X", Attribute.Type.FLOAT);
		Attribute heightAttribute = new Attribute("HEIGHT_X", Attribute.Type.FLOAT);
		
		data1.put(productNameAttribute, pn1);
		data1.put(pldAttribute, pld1);
		data1.put(colorAttribute, color1);
		data1.put(lengthAttribute, Float.parseFloat(l1));
		data1.put(widthAttribute, Float.parseFloat(w1));
		data1.put(heightAttribute, Float.parseFloat(h1));
		
		data2.put(productNameAttribute, pn2);
		data2.put(pldAttribute, pld2);
		data2.put(colorAttribute, color2);
		data2.put(lengthAttribute, Float.parseFloat(l2));
		data2.put(widthAttribute, Float.parseFloat(w2));
		data2.put(heightAttribute, Float.parseFloat(h2));
		
		if (vs instanceof Module) {
			Module m = (Module) vs;
			Tuple tuple1 = new Tuple(data1);
			Tuple tuple2 = new Tuple(data2);
			System.out.println("Variations: " + m.compute(tuple1, tuple2));
		}
	}
}
