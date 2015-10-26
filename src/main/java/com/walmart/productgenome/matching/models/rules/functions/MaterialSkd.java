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
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.functions.Function.ArgType;

public class MaterialSkd extends Function implements Module {

	public static final String NAME = "MATERIAL_SKD";
	public static final String DESCRIPTION = "Modules for matching materials";
	public static final int NUM_ARGS = 2;

	private static Set<String> dictionary = new HashSet<String>();

	static {
		// load the predictions from the brand module
		try {
			BufferedReader br = new BufferedReader(new FileReader("/Users/patron/Downloads/784_IS/all_material_dic.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				String s = line.split("\\t")[0];
				dictionary.add(s.trim().toLowerCase());
			}
			br.close();
			System.out.println("Loaded values from material dictionary: " + dictionary.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MaterialSkd() {
		super(NAME, DESCRIPTION);
	}

	public MaterialSkd(String name, String description) {
		super(name, description);
	}

	@Override
	public Float compute(String[] args) throws IllegalArgumentException{
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

		arg1 = arg1.toLowerCase().replaceAll("[/,-]", " ").replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
		arg2 = arg2.toLowerCase().replaceAll("[/,-]", " ").replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");

		//System.out.println("arg1: " + arg1 + ", arg2: " + arg2);
		AbstractStringMetric metric = new JaccardSimilarity();
		float jacScore = metric.getSimilarity(arg1, arg2);
		if (jacScore > 0) {
			return 1.0f;
		}
		return 0.0f;
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
		recommendedTypes.add(AllTypes.MULTI_WORD_SHORT_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_MEDIUM_STRING);
		recommendedTypes.add(AllTypes.MULTI_WORD_LONG_STRING);
		recommendedTypes.add(AllTypes.SET_VALUED);
		return recommendedTypes;
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
		Attribute materialAttribute = new Attribute("MATERIAL_X", Attribute.Type.TEXT);
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
	
	public float computeValue(String material1, String material2,
			String[] otherAttributeValues1, String[] otherAttributeValues2) {
		if (material1 != null
				&& !material1.toLowerCase().equals("null")
				&& !material1.isEmpty()
				&& material2 != null
				&& !material2.toLowerCase().equals("null")
				&& !material2.isEmpty()) {
			material1 = material1.toLowerCase().replaceAll("[/,-]", " ").replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
			material2 = material2.toLowerCase().replaceAll("[/,-]", " ").replaceAll("[^\\da-z ]", "").replaceAll("\\s", " ");
			//System.out.println("material1: " + material1 + ", material2: " + material2);
			AbstractStringMetric metric = new JaccardSimilarity();
			float jacScore = metric.getSimilarity(material1, material2);
			if (jacScore > 0) {
				return 1.0f;
			}
			//return 0.0f;
		}

		if (material1 != null
				&& !material1.toLowerCase().equals("null")
				&& !material1.isEmpty()) {
			// material2 does not exist, probe material1 into other attributes 2
			// System.out.println("Material 2 does not exist, material1 found in text of 2");
			for (String s: otherAttributeValues2) {
				if (s.toLowerCase().contains(material1.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		if (material2 != null
				&& !material2.toLowerCase().equals("null")
				&& !material2.isEmpty()) {
			// material1 does not exist, probe material2 into other attributes 1
			for (String s: otherAttributeValues1) {
				if (s.toLowerCase().contains(material2.toLowerCase())) {
					return 1.0f;
				}
			}
			//return 0.0f;
		}

		// both material1 and material2 do not exist
		// probe the dictionary
		Set<String> candidateMaterials1 = getCandidates(otherAttributeValues1);
		Set<String> candidateMaterials2 = getCandidates(otherAttributeValues2);

		for (String s: candidateMaterials1) {
			if (candidateMaterials2.contains(s)) {
				return 1.0f;
			}
		}
		return 0.0f;
	}
	
	public static void main(String[] args){
		Function materialSkd = new MaterialSkd();
		String[] args1 = {"Nylon - 3360D - Ballistic Dupont Cordura", "2520D Ballistic Nylon"};
		String[] args2 = {"1680 denier ballistic nylon / Nylon", "1680 denier ballistic nylon"};
		String[] args3 = {"Leather", "Microfiber/Leather"};
		System.out.println("Matching materials: " + materialSkd.compute(args1));
		System.out.println("Matching materials: " + materialSkd.compute(args2));
		System.out.println("Matching materials: " + materialSkd.compute(args3));
		
		Attribute materialAttribute = new Attribute("MATERIAL_X", Attribute.Type.TEXT);
		Attribute productNameAttribute = new Attribute("Product_Name", Attribute.Type.TEXT);
		Attribute psdAttribute = new Attribute("Product_Short_Description", Attribute.Type.TEXT);
		Attribute pldAttribute = new Attribute("Product_Long_Description", Attribute.Type.TEXT);
		
		Map<Attribute, Object> data1 = new HashMap<Attribute, Object>();
		Map<Attribute, Object> data2 = new HashMap<Attribute, Object>();
		
		String material1 = "Wood";
		String material2 = "";
		String[] others1 = {"Designs 2 Go Swivel TV Stand for TV or Monitor, for TVs up to 20 by Convenience Concepts",
				"Single swivel board for TV or monitor Heavy-duty construction Black wood-grained finish ",
				"Designs 2 Go Swivel TV Stand for TV or Monitor, for TVs up to 20 by Convenience Concepts:"
				+ "Single swivel board for TV or monitor "
				+ "Heavy-duty construction "
				+ "Black wood-grained finish "
				+ "Can be used on TV stands, tables or desks "
				+ "Comes fully assembled "
				+ "Model: 191020 "
				+ "See all TV stands on Walmart.com. Save money. Live better. "
				+ "Televisions sold separately. See all televisions."};
		String[] others2 = {"Single Tier Swivel TV Turntable", "Sturdy 16 mm thick particle board platform. "
				+ "Durable strong ball bearing steel swivel. Sits on foam rubber feet to protect desk. Limited "
				+ "warranty. Made from steel and wood grain melamine veneer. No assembly required. Swivel "
				+ "diameter: 12 in.. 23.63 in. W x 15.75 in. D x 1.38 in. H (12.3 lbs)"};
		
		data1.put(productNameAttribute, others1[0]);
		data1.put(psdAttribute, others1[1]);
		data1.put(pldAttribute, others1[2]);
		data1.put(materialAttribute, material1);
		
		data2.put(productNameAttribute, others2[0]);
		data2.put(pldAttribute, others2[1]);
		data2.put(materialAttribute, material2);
		
		if (materialSkd instanceof Module) {
			Module m = (Module) materialSkd;
			Tuple tuple1 = new Tuple(data1);
			Tuple tuple2 = new Tuple(data2);
			System.out.println("Matching materials: " + m.compute(tuple1, tuple2));
		}
	}
}
