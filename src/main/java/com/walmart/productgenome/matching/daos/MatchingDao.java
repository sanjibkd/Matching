package com.walmart.productgenome.matching.daos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import com.walmart.productgenome.matching.models.EMSRandomForest;
import com.walmart.productgenome.matching.models.audit.ItemPairAudit;
import com.walmart.productgenome.matching.models.audit.MatchStatus;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.service.ConfusionMatrix;
import com.walmart.productgenome.matching.service.debug.MatchingSummary;
import com.walmart.productgenome.matching.utils.JSONUtils;
import com.walmart.productgenome.matching.utils.WekaUtils;

public class MatchingDao {

	/*
	public static Table match(String projectName, String candsetName,
			String matcherName, String matchesName,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		String table1Name = matcher.getTable1Name();
		String table2Name = matcher.getTable2Name();
		//	System.out.println("[MatchingDao] table1Name: " + table1Name + ", table2Name: " + table2Name);

		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);
		Table candset = TableDao.open(projectName, candsetName);

		return getMatches(projectName, candset, table1, table2, matcher,
				matchesName, table1AttributeNames, table2AttributeNames);
	}
	 */

	public static Table match(String projectName, String candsetName,
			String matcherName, String matchesName, String table1Name, String table2Name,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		//	System.out.println("[MatchingDao] table1Name: " + table1Name + ", table2Name: " + table2Name);

		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);
		Table candset = TableDao.open(projectName, candsetName);

		return getMatches(projectName, candset, table1, table2, matcher,
				matchesName, table1AttributeNames, table2AttributeNames);
	}

	public static Table match(String projectName, Table candset,
			Table table1, Table table2, String matcherName, String matchesName,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException{
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		return getMatches(projectName, candset, table1, table2, matcher, matchesName,
				table1AttributeNames, table2AttributeNames);
	}

	// MatchWithAudit
	/*
	public static Table match(String projectName, String candsetName,
			String matcherName, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits, String[] table1AttributeNames,
			String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		String table1Name = matcher.getTable1Name();
		String table2Name = matcher.getTable2Name();
		//System.out.println("[MatchingDao] table1Name: " + table1Name + ", table2Name: " + table2Name);

		Table candset = TableDao.open(projectName, candsetName);
		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);

		return getMatches(projectName, candset, table1, table2, matcher,
				matchesName, itemPairAudits, table1AttributeNames, table2AttributeNames);
	}
	 */

	// MatchWithAudit
	public static Table match(String projectName, String candsetName,
			String matcherName, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits, String table1Name, 
			String table2Name, String[] table1AttributeNames,
			String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		//System.out.println("[MatchingDao] table1Name: " + table1Name + ", table2Name: " + table2Name);

		Table candset = TableDao.open(projectName, candsetName);
		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);

		return getMatches(projectName, candset, table1, table2, matcher,
				matchesName, itemPairAudits, table1AttributeNames, table2AttributeNames);
	}
	// MatchWithAudit
	public static Table match(String projectName, Table candset, Table table1,
			Table table2, String matcherName, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits, String[] table1AttributeNames,
			String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		return getMatches(projectName, candset, table1, table2, matcher,
				matchesName, itemPairAudits, table1AttributeNames, table2AttributeNames);
	}

	// MatchWithAudit
	public static Table match(String projectName, Table pairsTable,
			Table featuresTable, Table table1, Table table2,
			String matcherName, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		return getMatches(projectName, pairsTable, featuresTable, table1,
				table2, matcher, matchesName, itemPairAudits,
				table1AttributeNames, table2AttributeNames);
	}

	// MatchWithAudit
	public static Table match(String projectName, Table pairsTable,
			Table testFeaturesTable, Table table1, Table table2,
			String modelName, Table trainFeaturesTable, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits,
			String[] table1AttributeNames, String[] table2AttributeNames) throws Exception {
		AbstractClassifier classifier = null;
		if	("RF".equals(modelName)) {
			EMSRandomForest rf = new EMSRandomForest();
			rf.setNumExecutionSlots(8);
			classifier = rf;
		}
		else if ("J48".equals(modelName)) {
			classifier = new J48();
		}
		else if ("SMO".equals(modelName)) {
			classifier = new SMO();
		}
		else if ("NB".equals(modelName)) {
			classifier = new NaiveBayes();
		}
		else if ("IBK".equals(modelName)) {
			classifier = new IBk();
		}
		else if ("ABM1".equals(modelName)) {
			classifier = new AdaBoostM1();
		}
		else if ("STACKING".equals(modelName)) {
			classifier = new Stacking();
		}
		else if ("LB".equals(modelName)) {
			classifier = new LogitBoost();
		}
		else if ("GBRT".equals(modelName)) {
			LogitBoost lb = new LogitBoost();
			REPTree rep = new REPTree();
			rep.setMaxDepth(6);
			lb.setClassifier(rep);
			lb.setNumIterations(50);
			lb.setShrinkage(0.1);
			classifier = lb;
		}
		Instances instances = WekaUtils.getInstancesFromTable(trainFeaturesTable, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		trainInstances = WekaUtils.setClassIndex(trainInstances);
		classifier.buildClassifier(trainInstances);
		if (classifier instanceof EMSRandomForest) {
			EMSRandomForest rf = (EMSRandomForest) classifier;
			List<List<String>> rfRules = rf.getRandomForestRules();
			saveRules(rfRules);
		}
		return getMatches(projectName, pairsTable, testFeaturesTable, table1,
				table2, classifier, matchesName, itemPairAudits,
				table1AttributeNames, table2AttributeNames);
	}

	private static void saveRules(List<List<String>> rfRules) throws FileNotFoundException {
		long timestamp = System.currentTimeMillis();
		File file = new File ("/Users/sdas7/Documents/RFrules" + timestamp + ".txt");
	    PrintWriter pw = new PrintWriter(file);
		int i = 1;
		for (List<String> treeRules: rfRules) {
			pw.println("Tree #" + i);
			pw.println("*******");
			Map<String, String> ruleMap = WekaUtils.getPositiveRuleStrings2(treeRules);
			for (String r: ruleMap.keySet()) {
				pw.println(r + "\t" + ruleMap.get(r));
				pw.println();
			}
			pw.println();
			i++;
		}
		pw.close();
	}
	
	public static void addMatchingSummary(String projectName,
			MatchingSummary matchingSummary) throws IOException {
		Project project = ProjectDao.open(projectName);
		project.addMatchingSummary(matchingSummary);
		// ProjectDao.save(project);
	}

	private static Table createMatchesTable(String projectName, Table candset,
			Table table1, Table table2, String matchesName,
			List<Attribute> matchesAttributes,
			String[] table1AttributeNames, String[] table2AttributeNames,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes) {

		//		System.out.println("Project name: " + projectName);
		//		System.out.println("Candset: " + candset);
		//		System.out.println("Table 1: " + table1);
		//		System.out.println("Table 2: " + table2);
		//		System.out.println("Matches name: " + matchesName);
		//		System.out.println("Matches attributes: " + matchesAttributes);
		//		System.out.println("Table 1 attribute names: " + table1AttributeNames);
		//		System.out.println("Table 2 attribute names: " + table2AttributeNames);
		//		System.out.println("Table 1 attributes: " + table1Attributes);
		//		System.out.println("Table 2 attributes: " + table2Attributes);

		List<Attribute> candsetAttributes = candset.getAttributes();

		// TODO: We are making assumptions about locations of attributes
		Attribute pairIdAttribute = candsetAttributes.get(0);
		Attribute idAttribute1 = candsetAttributes.get(1);
		Attribute idAttribute2 = candsetAttributes.get(2);
		Attribute labelAttribute = new Attribute("label", Attribute.Type.INTEGER);

		matchesAttributes.add(pairIdAttribute);
		matchesAttributes.add(idAttribute1);
		matchesAttributes.add(idAttribute2);

		// add table 1 attributes
		if(null != table1AttributeNames){
			for(String attributeName : table1AttributeNames) {
				Attribute attribute = table1.getAttributeByName(attributeName);
				table1Attributes.add(attribute);
				matchesAttributes.add(new Attribute(table1.getName() + "." + attributeName, attribute.getType()));
			}
		}

		// add table 2 attributes
		if(null != table2AttributeNames){
			for(String attributeName : table2AttributeNames) {
				Attribute attribute = table2.getAttributeByName(attributeName);
				table2Attributes.add(attribute);
				matchesAttributes.add(new Attribute(table2.getName() + "." + attributeName, attribute.getType()));
			}
		}

		// add label attribute
		matchesAttributes.add(labelAttribute);

		Table matches = new Table(matchesName, pairIdAttribute, matchesAttributes, projectName);
		return matches;
	}

	private static Table getMatches(String projectName, Table candset, Table table1,
			Table table2, Matcher matcher, String matchesName,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException {

		List<Attribute> matchesAttributes = new ArrayList<Attribute>();
		List<Attribute> table1Attributes = new ArrayList<Attribute>();
		List<Attribute> table2Attributes = new ArrayList<Attribute>();

		Table matches = createMatchesTable(projectName, candset, table1, table2,
				matchesName, matchesAttributes, table1AttributeNames, table2AttributeNames,
				table1Attributes, table2Attributes);

		List<Tuple> matchedPairs = getMatchedPairs(candset, table1, table2,
				matcher, matchesAttributes, table1Attributes, table2Attributes);

		System.out.println("No. of matched pairs: " + matchedPairs.size());
		matches.addAllTuples(matchedPairs);
		return matches;
	}

	private static Table getMatches(String projectName, Table candset, Table table1,
			Table table2, Matcher matcher, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits, String[] table1AttributeNames,
			String[] table2AttributeNames ) throws IOException {

		List<Attribute> matchesAttributes = new ArrayList<Attribute>();
		List<Attribute> table1Attributes = new ArrayList<Attribute>();
		List<Attribute> table2Attributes = new ArrayList<Attribute>();

		Table matches = createMatchesTable(projectName, candset, table1, table2,
				matchesName, matchesAttributes, table1AttributeNames, table2AttributeNames,
				table1Attributes, table2Attributes);


		List<Tuple> matchedPairs = getMatchedPairs(candset, table1, table2,
				matcher, matchesAttributes, table1Attributes, table2Attributes, itemPairAudits);

		//System.out.println("No. of matched pairs: " + matchedPairs.size());
		matches.addAllTuples(matchedPairs);
		return matches;
	}

	private static Table getMatches(String projectName, Table pairsTable,
			Table featuresTable, Table table1, Table table2,
			Matcher matcher, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits,
			String[] table1AttributeNames, String[] table2AttributeNames) throws IOException {

		List<Attribute> matchesAttributes = new ArrayList<Attribute>();
		List<Attribute> table1Attributes = new ArrayList<Attribute>();
		List<Attribute> table2Attributes = new ArrayList<Attribute>();

		Table matches = createMatchesTable(projectName, pairsTable,
				table1, table2, matchesName, matchesAttributes, table1AttributeNames,
				table2AttributeNames,
				table1Attributes, table2Attributes);


		List<Tuple> matchedPairs = getMatchedPairs(pairsTable, featuresTable,
				table1, table2,
				matcher, matchesAttributes, table1Attributes, table2Attributes, itemPairAudits);

		//System.out.println("No. of matched pairs: " + matchedPairs.size());
		matches.addAllTuples(matchedPairs);
		return matches;
	}

	private static Table getMatches(String projectName, Table pairsTable,
			Table featuresTable, Table table1, Table table2,
			AbstractClassifier classifier, String matchesName, 
			Map<Tuple, ItemPairAudit> itemPairAudits,
			String[] table1AttributeNames, String[] table2AttributeNames) throws Exception {

		List<Attribute> matchesAttributes = new ArrayList<Attribute>();
		List<Attribute> table1Attributes = new ArrayList<Attribute>();
		List<Attribute> table2Attributes = new ArrayList<Attribute>();

		Table matches = createMatchesTable(projectName, pairsTable,
				table1, table2, matchesName, matchesAttributes, table1AttributeNames,
				table2AttributeNames,
				table1Attributes, table2Attributes);


		List<Tuple> matchedPairs = getMatchedPairs(pairsTable, featuresTable,
				table1, table2,
				classifier, matchesAttributes, table1Attributes, table2Attributes, itemPairAudits);

		//System.out.println("No. of matched pairs: " + matchedPairs.size());
		matches.addAllTuples(matchedPairs);
		return matches;
	}
	
	private static List<Tuple> getMatchedPairs(Table candset, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes) throws IOException {
		List<Tuple> matchedPairs = new ArrayList<Tuple>();
		for (Tuple t : candset.getAllTuplesInOrder()) {
			addMatchedPair(t, table1, table2, matcher, matchesAttributes,
					matchedPairs, table1Attributes, table2Attributes);
		}
		return matchedPairs;
	}

	private static List<Tuple> getMatchedPairs(Table candset, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes,
			Map<Tuple, ItemPairAudit> itemPairAudits) throws IOException {
		List<Tuple> matchedPairs = new ArrayList<Tuple>();
		for (Tuple t : candset.getAllTuplesInOrder()) {
			ItemPairAudit itemPairAudit = new ItemPairAudit(t);
			MatchStatus result = addMatchedPair(t, table1, table2, matcher,
					matchesAttributes,
					matchedPairs, table1Attributes, table2Attributes, itemPairAudit);

			itemPairAudit.setStatus(result);
			itemPairAudits.put(t, itemPairAudit);
		}
		return matchedPairs;
	}

	private static List<Tuple> getMatchedPairs(Table pairsTable, 
			Table featuresTable, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes,
			Map<Tuple, ItemPairAudit> itemPairAudits) throws IOException {
		List<Tuple> matchedPairs = new ArrayList<Tuple>();
		for (Tuple t : pairsTable.getAllTuplesInOrder()) {
			ItemPairAudit itemPairAudit = new ItemPairAudit(t);
			MatchStatus result = addMatchedPair(t, featuresTable, table1, table2, matcher,
					matchesAttributes,
					matchedPairs, table1Attributes, table2Attributes, itemPairAudit);

			itemPairAudit.setStatus(result);
			itemPairAudits.put(t, itemPairAudit);
		}
		return matchedPairs;
	}

	private static List<Tuple> getMatchedPairs(Table pairsTable, 
			Table featuresTable, Table table1,
			Table table2, AbstractClassifier classifier, List<Attribute> matchesAttributes,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes,
			Map<Tuple, ItemPairAudit> itemPairAudits) throws Exception {
		
		Instances instances = WekaUtils.getInstancesFromTable(featuresTable, true, false);
		Instances testInstances = WekaUtils.applyFilterToInstances(instances);
		testInstances = WekaUtils.setClassIndex(testInstances);
		
		List<Tuple> matchedPairs = new ArrayList<Tuple>();
		int index = 0;
		for (Tuple t : pairsTable.getAllTuplesInOrder()) {
			ItemPairAudit itemPairAudit = new ItemPairAudit(t);
			Instance testInstance = testInstances.get(index++);
			addMatchedPair(t, testInstance, table1, table2, classifier,
					matchesAttributes, matchedPairs, table1Attributes,
					table2Attributes, itemPairAudit);
			itemPairAudits.put(t, itemPairAudit);
		}
		return matchedPairs;
	}
	
	private static Map<Attribute, Object> getMatchedPairData(List<Attribute> matchesAttributes,
			Tuple candsetTuple, Tuple tuple1, Tuple tuple2,
			List<Attribute> table1Attributes, List<Attribute> table2Attributes) {

		Map<Attribute, Object> data = new HashMap<Attribute, Object>();
		Attribute pairIdAttribute = matchesAttributes.get(0);
		Object pairId = candsetTuple.getAttributeValue(pairIdAttribute);
		data.put(pairIdAttribute, pairId);

		int i = 3;
		for(Attribute attribute : table1Attributes) {
			Object attributeValue = tuple1.getAttributeValue(attribute);
			data.put(matchesAttributes.get(i), attributeValue);
			i++;
		}
		for(Attribute attribute : table2Attributes) {
			Object attributeValue = tuple2.getAttributeValue(attribute);
			data.put(matchesAttributes.get(i), attributeValue);
			i++;
		}

		return data;
	}

	private static MatchStatus addMatchedPair(Tuple candsetTuple, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Tuple> matchedPairs, List<Attribute> table1Attributes,
			List<Attribute> table2Attributes) throws IOException {

		Attribute idAttribute1 = matchesAttributes.get(1);
		Attribute idAttribute2 = matchesAttributes.get(2);
		Object id1 = candsetTuple.getAttributeValue(idAttribute1);
		Object id2 = candsetTuple.getAttributeValue(idAttribute2);

		Tuple tuple1 = table1.getTuple(id1);

		if (null == tuple1) {
			throw new IOException("Cannot retrieve tuple from table 1. Tuple id: " + id1);
		}
		Tuple tuple2 = table2.getTuple(id2);
		if (null == tuple2) {
			throw new IOException("Cannot retrieve tuple from table 2. Tuple id: " + id2);
		}

		System.out.println("[MatchingDao] Evaluating tuple1: " + tuple1 + ", tuple2: " + tuple2);

		Map<Attribute, Object> data = getMatchedPairData(matchesAttributes,
				candsetTuple, tuple1, tuple2, table1Attributes, table2Attributes);

		data.put(idAttribute1, id1);
		data.put(idAttribute2, id2);

		// System.out.println("[MatchingDao] Evaluating tuple1: " + tuple1 + ", tuple2: " + tuple2);
		MatchStatus result = matcher.evaluate(tuple1, tuple2);
		Attribute labelAttribute = matchesAttributes.get(matchesAttributes.size()-1);
		data.put(labelAttribute, result.getLabel());
		matchedPairs.add(new Tuple(data));
		//System.out.println("MatchingDao (pairId, MatchStatus): (" + pairId + ", " + result + ")");
		return result;
	}

	private static MatchStatus addMatchedPair(Tuple candsetTuple, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Tuple> matchedPairs, List<Attribute> table1Attributes,
			List<Attribute> table2Attributes, ItemPairAudit itemPairAudit) throws IOException {

		Attribute idAttribute1 = matchesAttributes.get(1);
		Attribute idAttribute2 = matchesAttributes.get(2);
		Object id1 = candsetTuple.getAttributeValue(idAttribute1);
		Object id2 = candsetTuple.getAttributeValue(idAttribute2);

		Tuple tuple1 = table1.getTuple(id1);
		if (null == tuple1) {
			throw new IOException("Cannot retrieve tuple from table 1. Tuple id: " + id1);
		}

		Tuple tuple2 = table2.getTuple(id2);
		if (null == tuple2) {
			throw new IOException("Cannot retrieve tuple from table 2. Tuple id: " + id2);
		}

		Map<Attribute, Object> data = getMatchedPairData(matchesAttributes,
				candsetTuple, tuple1, tuple2, table1Attributes, table2Attributes);

		data.put(idAttribute1, id1);
		data.put(idAttribute2, id2);

		//System.out.println("[MatchingDao] Evaluating tuple1: " + tuple1 + ", tuple2: " + tuple2);
		MatchStatus result = matcher.evaluate(tuple1, tuple2, itemPairAudit);
		Attribute labelAttribute = matchesAttributes.get(matchesAttributes.size()-1);
		data.put(labelAttribute, result.getLabel());
		matchedPairs.add(new Tuple(data));
		//System.out.println("MatchingDao (pairId, MatchStatus): (" + pairId + ", " + result + ")");
		return result;
	}

	private static MatchStatus addMatchedPair(Tuple candsetTuple,
			Table featuresTable, Table table1,
			Table table2, Matcher matcher, List<Attribute> matchesAttributes,
			List<Tuple> matchedPairs, List<Attribute> table1Attributes,
			List<Attribute> table2Attributes, ItemPairAudit itemPairAudit) throws IOException {

		Attribute pairIdAttribute = matchesAttributes.get(0);
		Attribute idAttribute1 = matchesAttributes.get(1);
		Attribute idAttribute2 = matchesAttributes.get(2);
		Object pairId = candsetTuple.getAttributeValue(pairIdAttribute);
		Object id1 = candsetTuple.getAttributeValue(idAttribute1);
		Object id2 = candsetTuple.getAttributeValue(idAttribute2);

		Tuple tuple1 = table1.getTuple(id1);
		if (null == tuple1) {
			throw new IOException("Cannot retrieve tuple from table 1. Tuple id: " + id1);
		}

		Tuple tuple2 = table2.getTuple(id2);
		if (null == tuple2) {
			throw new IOException("Cannot retrieve tuple from table 2. Tuple id: " + id2);
		}

		Map<Attribute, Object> data = getMatchedPairData(matchesAttributes,
				candsetTuple, tuple1, tuple2, table1Attributes, table2Attributes);

		data.put(idAttribute1, id1);
		data.put(idAttribute2, id2);

		Tuple featureTuple = featuresTable.getTuple(pairId);
		//System.out.println("[MatchingDao] Evaluating tuple1: " + tuple1 + ", tuple2: " + tuple2);
		MatchStatus result = matcher.evaluate(featureTuple, itemPairAudit);
		Attribute labelAttribute = matchesAttributes.get(matchesAttributes.size()-1);
		data.put(labelAttribute, result.getLabel());
		matchedPairs.add(new Tuple(data));
		//System.out.println("MatchingDao (pairId, MatchStatus): (" + pairId + ", " + result + ")");
		return result;
	}
	
	private static void addMatchedPair(Tuple candsetTuple,
			Instance testInstance, Table table1,
			Table table2, AbstractClassifier classifier, List<Attribute> matchesAttributes,
			List<Tuple> matchedPairs, List<Attribute> table1Attributes,
			List<Attribute> table2Attributes, ItemPairAudit itemPairAudit) throws Exception {

		Attribute idAttribute1 = matchesAttributes.get(1);
		Attribute idAttribute2 = matchesAttributes.get(2);
		Object id1 = candsetTuple.getAttributeValue(idAttribute1);
		Object id2 = candsetTuple.getAttributeValue(idAttribute2);

		Tuple tuple1 = table1.getTuple(id1);
		if (null == tuple1) {
			throw new IOException("Cannot retrieve tuple from table 1. Tuple id: " + id1);
		}

		Tuple tuple2 = table2.getTuple(id2);
		if (null == tuple2) {
			throw new IOException("Cannot retrieve tuple from table 2. Tuple id: " + id2);
		}

		Map<Attribute, Object> data = getMatchedPairData(matchesAttributes,
				candsetTuple, tuple1, tuple2, table1Attributes, table2Attributes);

		data.put(idAttribute1, id1);
		data.put(idAttribute2, id2);

		//System.out.println("[MatchingDao] Evaluating tuple1: " + tuple1 + ", tuple2: " + tuple2);
		double classIndex = classifier.classifyInstance(testInstance);
		double[] classProbabilities = classifier.distributionForInstance(testInstance);
		double votingEntropy = -1.0;
		if (classifier instanceof EMSRandomForest) {
			EMSRandomForest rf = (EMSRandomForest) classifier;
			votingEntropy = rf.getVotingEntropyForInstance(testInstance);
		}
//		if (("12179498".equals(id1) && "12179498#eBags".equals(id2))
//				|| ("11962150".equals(id1) && "11962150#ivgStores".equals(id2))
//				|| ("26989456".equals(id1) && "26989456#eBags".equals(id2))
//				|| ("21984488".equals(id1) && "21984488#eBags".equals(id2))) {
//			System.out.println("id1: " + id1 + ", id2: " + id2
//					+ ", classIndex: " + classIndex + ", prob0: "
//					+ classProbabilities[0] + ", prob1: " + classProbabilities[1]
//					+ ", entropy: " + votingEntropy);
//		}
//		
		MatchStatus result = MatchStatus.NON_MATCH;
		if (classIndex == 0) {
			// MATCH
			result = MatchStatus.MATCH;
		}
		Attribute labelAttribute = matchesAttributes.get(matchesAttributes.size()-1);
		data.put(labelAttribute, result.getLabel());
		matchedPairs.add(new Tuple(data));
		//System.out.println("MatchingDao (pairId, MatchStatus): (" + pairId + ", " + result + ")");
		itemPairAudit.setVotingEntropy(votingEntropy);
		itemPairAudit.setClassProbabilities(classProbabilities);
		itemPairAudit.setStatus(result);
	}
}
