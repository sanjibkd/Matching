package com.walmart.productgenome.matching.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.models.audit.MatchStatus;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.loaders.CSVLoader;
import com.walmart.productgenome.matching.models.loaders.TableLoader;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.rules.Term;
import com.walmart.productgenome.matching.models.rules.functions.ExactMatch;
import com.walmart.productgenome.matching.models.rules.functions.Jaccard;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WekaUtils {

	public static Instances getInstancesFromArffFile(String arffFileName)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(arffFileName));
		Instances data = new Instances(br);
		br.close();
		return data;
	}
	
	public static Evaluation classify(Classifier model,
			Instances trainSet, Instances testSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainSet);
 
		model.buildClassifier(trainSet);
		evaluation.evaluateModel(model, testSet);
 
		return evaluation;
	}
	
	public static Instances getInstancesFromTable(Table table, boolean hasLabel, boolean dummyLabel) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("@relation " + table.getName() + "\n\n");
		List<Attribute> attributes = table.getAttributes();
		Attribute labelAttribute = null;
		if (hasLabel) {
			labelAttribute = attributes.get(attributes.size()-1);
		}
		for (Attribute attribute : attributes) {
			sb.append("@attribute " + attribute.getName());
			if (!attribute.equals(labelAttribute)) {
				switch (attribute.getType()) {
				case TEXT:
					sb.append(" string\n");
					break;
				case INTEGER:
				case LONG:
				case FLOAT:
					sb.append(" numeric\n");
					break;
				case BOOLEAN:
					sb.append(" {yes, no}\n");
					break;
				}
			}
			else {
				sb.append(" {");
				sb.append(MatchStatus.MATCH.getLabel());
				sb.append(", ");
				sb.append(MatchStatus.NON_MATCH.getLabel());
				sb.append("}\n");
				break;
			}
		}
		
		if (!hasLabel && dummyLabel) {
			sb.append("@attribute label {");
			sb.append(MatchStatus.MATCH.getLabel());
			sb.append(", ");
			sb.append(MatchStatus.NON_MATCH.getLabel());
			sb.append("}\n");
		}
		
		sb.append("\n@data\n");
		
		for (Tuple tuple : table.getAllTuplesInOrder()) {
			for (int i=0; i<attributes.size(); i++) {
				Attribute attribute = attributes.get(i);
				sb.append(tuple.getAttributeValue(attribute));
				if(i != attributes.size()-1) {
					sb.append(",");
				}
			}
			if (!hasLabel && dummyLabel) {
				sb.append(",?");
			}
			sb.append("\n");
		}
		//System.out.println(sb.toString());
		return new Instances(new StringReader(sb.toString()));
	}
	
	// removes the first attribute
	public static Instances applyFilterToInstances(Instances instances)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = "1";  // first attribute
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInputFormat(instances); // dataset **AFTER** setting options
		Instances filteredInstances = Filter.useFilter(instances, remove);
		return filteredInstances;
	}
	
	public static Instances setClassIndex(Instances instances) {
		instances.setClassIndex(instances.numAttributes() - 1);
		System.out.println("ClassAttr.stats: " +
				instances.attributeStats(instances.classIndex()));
		System.out.println("ClassAttr.enumValues: yes:" +
				instances.classAttribute().value(0));
		if (instances.numClasses() > 1) {
			System.out.println("ClassAttr.enumValues: no:" +
					instances.classAttribute().value(1));								
		}
		return instances;
	}
	
	public static Instances generateFeatures(String relationName, Table table1,
			Table table2, Table pairs, List<Feature> features, boolean setClassIndex) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("@relation " + relationName + "\n\n");
		for (Feature f : features) {
			sb.append("@attribute " + f.getName() + " numeric\n");
		}
		List<Attribute> attributes = pairs.getAttributes();
		Attribute label = attributes.get(attributes.size()-1);
		if (setClassIndex) {
			sb.append("@attribute " + label.getName() + " {1, 0}\n");	
		}
		sb.append("\n@data\n");
		
		Attribute id1 = attributes.get(1);
		Attribute id2 = attributes.get(2);
		for (Tuple pair : pairs.getAllTuplesInOrder()) {
			Object id1Val = pair.getAttributeValue(id1);
			Object id2Val = pair.getAttributeValue(id2);
			Tuple tuple1 = table1.getTuple(id1Val);
			Tuple tuple2 = table2.getTuple(id2Val);
			for (int i=0; i<features.size(); i++) {
				sb.append(features.get(i).compute(tuple1, tuple2));
				if (i != features.size()-1) {
					sb.append(",");
				}
			}
			if (setClassIndex) {
				sb.append("," + pair.getAttributeValue(label));
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());
		Instances featureVectors = new Instances(new StringReader(sb.toString()));
		return featureVectors;
	}
	
	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
 
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}
 
		return 100 * correct / predictions.size();
	}
	
	private static String getUpdatedRuleStats(String s1, String s2) {
		int index1 = s1.indexOf("/");
		String c1 = "0";
		String m1 = "0";
		if (index1 != -1) {
			c1 = s1.substring(1, index1);
			m1 = s1.substring(index1 + 1, s1.length() - 1);
		}
		else {
			c1 = s1.substring(1, s1.length()-1);
			m1 = "0";
		}
		double cov1 = Double.parseDouble(c1);
		double mis1 = Double.parseDouble(m1);
		
		int index2 = s2.indexOf("/");
		String c2 = "0";
		String m2 = "0";
		if (index2 != -1) {
			c2 = s2.substring(1, index2);
			m2 = s2.substring(index2 + 1, s2.length() - 1);
		}
		else {
			c2 = s2.substring(1, s2.length()-1);
			m2 = "0";
		}
		double cov2 = Double.parseDouble(c2);
		double mis2 = Double.parseDouble(m2);
		
		double cov = cov1 + cov2;
		double mis = mis1 + mis2;
		
		return "(" + cov + "/" + mis + ")";
	}
	
	
	private static Map<String, String> getRuleStringsForLabel2(List<String> allRules, String label) {
		Map<String, String> ruleStrings = new LinkedHashMap<String, String>();
		for (String r : allRules) {
			r = r.trim().replaceAll("\\s+", " ");
			//System.out.println("Rule before pattern matching: " + r);
			String pattern = "IF (.+) THEN " + label + " (\\([\\d\\./]+\\))";
			if (r.matches(pattern)) {
				String ruleString = r.replaceAll(pattern, "$1");
				//System.out.println("Rule string: " + ruleString);
				String ruleStats = r.replaceAll(pattern, "$2");
				//System.out.println("Rule stats: " + ruleStats);
				if (ruleStrings.containsKey(ruleString)) {
					ruleStats = getUpdatedRuleStats(ruleStats, ruleStrings.get(ruleString));
				}
				ruleStrings.put(ruleString, ruleStats);
			}
		}
		return ruleStrings;
	}
	/*
	public static void main(String[] args) {
		List<String> rules = new ArrayList<String>();
		String rule1 = "IF year_exact <= 0 THEN 0 (12.0/2.0)";
		String rule2 = "IF year_exact > 0 THEN 1 (8.0)";
		rules.add(rule1);
		rules.add(rule2);
		Map<String, String> posRules = getPositiveRuleStrings2(rules);
		printRules2(posRules);
	}
*/
	
	private static List<String> getRuleStringsForLabel(List<String> allRules, String label) {
		List<String> ruleStrings = new ArrayList<String>();
		for (String r : allRules) {
			r = r.replaceAll("\\s+", " ");
			String pattern = "IF (.+) THEN " + label + "(.*)";
			if (r.matches(pattern)) {
				String ruleString = r.replaceAll(pattern, "$1");
				System.out.println("Rule string: " + ruleString);
				ruleStrings.add(ruleString);
			}
		}
		return ruleStrings;
	}
	
	public static List<String> getPositiveRuleStrings(List<String> allRules) {
		String label = "" + MatchStatus.MATCH.getLabel();
		return getRuleStringsForLabel(allRules, label);
	}

	public static Map<String, String> getPositiveRuleStrings2(List<String> allRules) {
		String label = "" + MatchStatus.MATCH.getLabel();
		return getRuleStringsForLabel2(allRules, label);
	}
	
	public static List<String> collectAllRules(List<List<String>> randomForestRules) {
		List<String> allRules = new ArrayList<String>();
		for(List<String> randomTreeRules : randomForestRules) {
			System.out.println("Random Tree: " + randomTreeRules.size() + " rules");
			allRules.addAll(randomTreeRules);
		}
		return allRules;
	}
	
	public static List<String> getCoalescedRuleStrings(List<String> ruleStrings) {
		List<String> coalescedRuleStrings = new ArrayList<String>();
		for (String rule : ruleStrings) {
			rule = rule.replaceAll("\\s+", " ");
			Map<String, float[]> featureRelopValsMap = new HashMap<String, float[]>();
			String[] terms = rule.split(" AND ");
			for(String term : terms) {
				String[] termComponents = term.split(" ");
				String feature = termComponents[0].trim();
				String relop = termComponents[1].trim();
				float value = Float.parseFloat(termComponents[2].trim());
				// <, <=, >, >=
				float[] rvals = {Float.MAX_VALUE, Float.MAX_VALUE, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
				if (featureRelopValsMap.containsKey(feature)) {
					rvals = featureRelopValsMap.get(feature);
				}
				if (relop.equals("<")) {
					if (value < rvals[0]) {
						rvals[0] = value;
					}
				}
				else if (relop.equals("<=")) {
					if (value < rvals[1]) {
						rvals[1] = value;
					}
				}
				else if (relop.equals(">")) {
					if (value > rvals[2]) {
						rvals[2] = value;
					}
				}
				else if (relop.equals(">=")) {
					if (value > rvals[3]) {
						rvals[3] = value;
					}
				}
				else {
					System.err.println("Relop: " + relop + " not handled");
					continue;
				}
				featureRelopValsMap.put(feature, rvals);
			}
			// construct the coalesced rule string
			String coalescedRule = "";
			boolean found = false;
			for (String feature : featureRelopValsMap.keySet()) {
				float[] rvals = featureRelopValsMap.get(feature);
				if (rvals[0] < Float.MAX_VALUE && rvals[0] <= rvals[1]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " < " + rvals[0];
					found = true;
				}
				if (rvals[1] < Float.MAX_VALUE && rvals[1] < rvals[0]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " <= " + rvals[1];
					found = true;
				}
				if (rvals[2] > Float.NEGATIVE_INFINITY && rvals[2] >= rvals[3]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " > " + rvals[2];
					found = true;
				}
				if (rvals[3] > Float.NEGATIVE_INFINITY && rvals[3] > rvals[2]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " >= " + rvals[3];
					found = true;
				}
			}
			coalescedRuleStrings.add(coalescedRule);
		}
		return coalescedRuleStrings;
	}
	
	public static Map<String, String> getCoalescedRuleStrings2(Map<String, String> ruleStrings) {
		Map<String, String> coalescedRuleStrings = new LinkedHashMap<String, String>();
		for (String rule : ruleStrings.keySet()) {
			rule = rule.replaceAll("\\s+", " ");
			Map<String, float[]> featureRelopValsMap = new HashMap<String, float[]>();
			String[] terms = rule.split(" AND ");
			for(String term : terms) {
				String[] termComponents = term.split(" ");
				String feature = termComponents[0].trim();
				String relop = termComponents[1].trim();
				float value = Float.parseFloat(termComponents[2].trim());
				// <, <=, >, >=
				float[] rvals = {Float.MAX_VALUE, Float.MAX_VALUE, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
				if (featureRelopValsMap.containsKey(feature)) {
					rvals = featureRelopValsMap.get(feature);
				}
				if (relop.equals("<")) {
					if (value < rvals[0]) {
						rvals[0] = value;
					}
				}
				else if (relop.equals("<=")) {
					if (value < rvals[1]) {
						rvals[1] = value;
					}
				}
				else if (relop.equals(">")) {
					if (value > rvals[2]) {
						rvals[2] = value;
					}
				}
				else if (relop.equals(">=")) {
					if (value > rvals[3]) {
						rvals[3] = value;
					}
				}
				else {
					System.err.println("Relop: " + relop + " not handled");
					continue;
				}
				featureRelopValsMap.put(feature, rvals);
			}
			// construct the coalesced rule string
			String coalescedRule = "";
			String coalescedRuleStats = "(0/0)";
			boolean found = false;
			for (String feature : featureRelopValsMap.keySet()) {
				float[] rvals = featureRelopValsMap.get(feature);
				if (rvals[0] < Float.MAX_VALUE && rvals[0] <= rvals[1]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " < " + rvals[0];
					found = true;
				}
				if (rvals[1] < Float.MAX_VALUE && rvals[1] < rvals[0]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " <= " + rvals[1];
					found = true;
				}
				if (rvals[2] > Float.NEGATIVE_INFINITY && rvals[2] >= rvals[3]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " > " + rvals[2];
					found = true;
				}
				if (rvals[3] > Float.NEGATIVE_INFINITY && rvals[3] > rvals[2]) {
					if (found) {
						coalescedRule += " AND ";
					}
					coalescedRule += feature + " >= " + rvals[3];
					found = true;
				}
			}
			if (coalescedRuleStrings.containsKey(coalescedRule)) {
				coalescedRuleStats = coalescedRuleStrings.get(coalescedRule);
			}
			String ruleStats = ruleStrings.get(rule);
			coalescedRuleStats = getUpdatedRuleStats(coalescedRuleStats, ruleStats);
			coalescedRuleStrings.put(coalescedRule, coalescedRuleStats);
		}
		return coalescedRuleStrings;
	}
	
	public static List<String> getUniqueRuleStrings(List<String> ruleStrings) {
		Set<String> uniqueRuleStrings = new LinkedHashSet<String>();
		for (String r : ruleStrings) {
			r = r.replaceAll("\\s+", " ");
			uniqueRuleStrings.add(r);
		}
		return new ArrayList<String>(uniqueRuleStrings);
	}
	
	private static void printRules(List<String> rules) {
		for (String rule : rules) {
			System.out.println(rule);
		}
	}
	
	private static void printRules2(Map<String, String> rules) {
		for (String rule : rules.keySet()) {
			System.out.println(rule + " " + rules.get(rule));
		}
	}
	
	private static List<String> sortRules(List<String> ruleStrings) {
		return null;
	}
	

	public static void main(String[] args) {
		String table1CsvFileName = "/home/sanjib/work/MatchingWebApp/data/books/bowker.csv";
		String table2CsvFileName = "/home/sanjib/work/MatchingWebApp/data/books/walmart.csv";
		String trainSetCsvFile = "/home/sanjib/work/MatchingWebApp/data/books/train.csv";
		String testSetCsvFile = "/home/sanjib/work/MatchingWebApp/data/books/test.csv";
		
		try {
			Table table1 = CSVLoader.loadTableFromCSVWithHeaderAsSchema("books", "bowker", table1CsvFileName);
			Table table2 = CSVLoader.loadTableFromCSVWithHeaderAsSchema("books", "walmart", table2CsvFileName);
			Table trainTable = CSVLoader.loadTableFromCSVWithHeaderAsSchema("books", "bowker", trainSetCsvFile);
			Table testTable = CSVLoader.loadTableFromCSVWithHeaderAsSchema("books", "bowker", testSetCsvFile);
			List<Feature> features = new ArrayList<Feature>();
			Attribute bowkerTitle = table1.getAttributes().get(1);
			Attribute walmartTitle = table2.getAttributes().get(1);
			features.add(new Feature("title_jac", new Jaccard(), "books", bowkerTitle, walmartTitle));
			Attribute bowkerAuthor = table1.getAttributes().get(2);
			Attribute walmartAuthor = table2.getAttributes().get(2);
			features.add(new Feature("author_jac", new Jaccard(), "books", bowkerAuthor, walmartAuthor));
			Attribute bowkerYear = table1.getAttributes().get(7);
			Attribute walmartYear = table2.getAttributes().get(7);
			features.add(new Feature("year_exact", new ExactMatch(), "books", bowkerYear, walmartYear));
			Instances trainSet = generateFeatures("books_train", table1, table2, trainTable, features, true);
			trainSet.setClassIndex(trainSet.numAttributes() - 1);
			
			System.out.println("ClassAttr.stats: " + trainSet.attributeStats(trainSet.classIndex()));
			// System.out.println("ClassAttr.enumValues: " + data.classAttribute().enumerateValues());
			System.out.println("ClassAttr.enumValues: yes:" + trainSet.classAttribute().value(0));
			if (trainSet.numClasses() > 1) {
				System.out.println("ClassAttr.enumValues: no:" + trainSet.classAttribute().value(1));								
			}
			
			Instances testSet = generateFeatures("books_test", table1, table2, testTable, features, true);
			testSet.setClassIndex(testSet.numAttributes() - 1);
			
			System.out.println("ClassAttr.stats: " + testSet.attributeStats(testSet.classIndex()));
			// System.out.println("ClassAttr.enumValues: " + data.classAttribute().enumerateValues());
			System.out.println("ClassAttr.enumValues: yes:" + testSet.classAttribute().value(0));
			if (testSet.numClasses() > 1) {
				System.out.println("ClassAttr.enumValues: no:" + testSet.classAttribute().value(1));								
			}
			
			//J48 model = new J48();
			PART model = new PART();
			//ConjunctiveRule model = new ConjunctiveRule();
			//RandomForest model = new RandomForest();
			//model.buildClassifier(trainSet);
			
			Evaluation validation = classify(model, trainSet, testSet);
			FastVector predictions = validation.predictions();
			
			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);
			 
			// Print model's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + model.getClass().getSimpleName() + ": "
								+ String.format("%.2f%%", accuracy)
								+ "\n---------------------------------");
			System.out.println("Evaluation: " + validation.toSummaryString());
			System.out.println("Precision for yes: " + validation.precision(0));
			System.out.println("Precision for no: " + validation.precision(1));
			System.out.println("Recall for yes: " + validation.recall(0));
			System.out.println("Recall for no: " + validation.recall(1));
			System.out.println("Confusion matrix: ");
			double[][] confusionMatrix = validation.confusionMatrix();
			for(int i=0; i<confusionMatrix.length; i++) {
				for(int j=0;j<confusionMatrix[i].length;j++) {
					System.out.print(confusionMatrix[i][j]);
					System.out.print(" ");
				}
				System.out.println();
			}
			for(int i=0; i<predictions.size(); i++) {
				System.out.println(predictions.elementAt(i));
			}
			
			System.out.println("Rules\n--------");
			//List<String> dtRules = model.getDecisionTreeRules();
			List<String> dtRules = model.getDecisionListRules();
			//List<List<String>> randomForestRules = model.getRandomForestRules();
			//List<String> dtRules = collectAllRules(randomForestRules);
			System.out.println("=========================");
			System.out.println("All rules:");
			printRules(dtRules);
			System.out.println("=========================");
			//List<String> posRuleStrings = getPositiveRuleStrings(dtRules);
			Map<String, String> posRuleStrings2 = getPositiveRuleStrings2(dtRules);
			System.out.println("Positive rules:");
			//printRules(posRuleStrings);
			printRules2(posRuleStrings2);
			//List<String> posRuleStrings = new ArrayList<String>(posRuleStrings2.keySet());
			System.out.println("=========================");
			//List<String> posRuleStringsCoalesced = getCoalescedRuleStrings(posRuleStrings);
			Map<String,String> posRuleStringsCoalesced2 = getCoalescedRuleStrings2(posRuleStrings2);
			System.out.println("Coalesced rules:");
			//printRules(posRuleStringsCoalesced);
			printRules2(posRuleStringsCoalesced2);
			List<String> posRuleStringsCoalesced = new ArrayList<String>(posRuleStringsCoalesced2.keySet());
			System.out.println("=========================");
			//List<String> posRuleStringsCoalescedUnique = getUniqueRuleStrings(posRuleStringsCoalesced);
			//Map<String, String> posRuleStringsCoalescedUnique2 = getUniqueRuleStrings2(posRuleStringsCoalesced2);
			//System.out.println("Unique rules:");
			//printRules(posRuleStringsCoalescedUnique);
			//printRules2(posRuleStringsCoalescedUnique2);
			//List<String> posRuleStringsCoalescedUnique = new ArrayList<String>(posRuleStringsCoalescedUnique2.keySet());
			//System.out.println("=========================");
			//System.out.println("Total rules: " + dtRules.size() + ", Positive rules: " +
			//					posRuleStrings2.size() + ", Coalesced rules: " +
			//					posRuleStringsCoalesced2.size() + ", Unique rules: " +
			//					posRuleStringsCoalescedUnique2.size());
			System.out.println("Total rules: " + dtRules.size() + ", Positive rules: " +
										posRuleStrings2.size() + ", Coalesced rules: " +
										posRuleStringsCoalesced2.size());
 			Project p = ProjectDao.open("books");
			for (int i=0; i<posRuleStringsCoalesced.size(); i++) {
				List<Term> terms = ParsingUtils.parseRuleFromDisplayString(p, posRuleStringsCoalesced.get(i));
				Rule rule = new Rule("SL"+i, "books", terms);
				System.out.println("Rule " + i + ": " + rule.getDisplayString());
				//System.out.println("Rule " + i + ": " + posRuleStringsCoalesced.get(i));
			}

			//System.out.println("The tree: " + model.graph());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public static double getAverageConfidence(Instances instances,
			RandomForest model) throws Exception {
		System.out.println("Number of instances in the validation set: " +
			instances.numInstances());
		double sum = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.get(i);
			//System.out.println("Instance: " + instance);
			double entropy = model.getVotingEntropyForInstance(instance);
			//System.out.println("Entropy: " + entropy);
			double confidence = 1 - entropy;
			sum += confidence;
		}
		double avgConfidence = sum/instances.numInstances();
		return avgConfidence;
	}
	
}
