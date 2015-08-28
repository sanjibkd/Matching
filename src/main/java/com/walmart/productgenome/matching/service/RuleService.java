package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.models.RelationalOperator;
import com.walmart.productgenome.matching.models.audit.MatchStatus;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.rules.Term;
import com.walmart.productgenome.matching.models.savers.FeatureSaver;
import com.walmart.productgenome.matching.models.savers.RuleSaver;
import com.walmart.productgenome.matching.utils.ParsingUtils;
import com.walmart.productgenome.matching.utils.WekaUtils;

public class RuleService {

	public enum RuleBasedModel {
		PART ("Decision List"),
		J48 ("Decision Tree"),
		RF ("Random Forest");
		
		private String displayName;
		
		private RuleBasedModel(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return displayName;
		}
	}
	
	public static List<String> getModelDisplayNames() {
		List<String> displayNames = new ArrayList<String>();
		for (RuleBasedModel model : RuleBasedModel.values()) {
			displayNames.add(model.getDisplayName());
		}
		return displayNames;
	}
	
	public static List<String> getModelNames() {
		List<String> modelNames = new ArrayList<String>();
		for (RuleBasedModel model : RuleBasedModel.values()) {
			modelNames.add(model.name());
		}
		return modelNames;
	}
	
	public static Map<String, ConfusionMatrix> evaluateRulesUsingTestData(Set<String> rules,
			Table testData, Project project, Table table1, Table table2) {
		Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		
		// count the actual positives in test data
		long actualPositives = 0;
		List<Attribute> attributes = testData.getAttributes();
		Attribute label = attributes.get(attributes.size()-1);
		for (Tuple tuple : testData.getAllTuples()) {
			if (tuple.getAttributeValue(label).equals(MatchStatus.MATCH.getLabel())) {
				actualPositives++;
			}
		}
		Attribute id1 = attributes.get(1);
		Attribute id2 = attributes.get(2);
		
		for (String rule : rules) {
			long predictedPositives = 0;
			long truePositives = 0;
			List<Term> terms = ParsingUtils.parseRuleFromDisplayString(project,
					rule);
			for (Tuple tuple : testData.getAllTuples()) {
				Object id1Val = tuple.getAttributeValue(id1);
				Object id2Val = tuple.getAttributeValue(id2);
				Tuple tuple1 = table1.getTuple(id1Val);
				Tuple tuple2 = table2.getTuple(id2Val);
				MatchStatus result = MatchStatus.MATCH;
				for (Term term : terms) {
					try {
						result = term.evaluate(tuple1, tuple2);
						if (MatchStatus.MATCH != result) {
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (result == MatchStatus.MATCH) {
					// predicted positive
					predictedPositives++;
					if (tuple.getAttributeValue(label).equals(MatchStatus.MATCH.getLabel())) {
						truePositives++;
					}
				}
			}
			ConfusionMatrix confusionMatrix = new ConfusionMatrix();
			confusionMatrix.setTruePositives(truePositives);
			confusionMatrix.setFalseNegatives(actualPositives - truePositives);
			confusionMatrix.setFalsePositives(predictedPositives - truePositives);
			confusionMatrix.setTrueNegatives(testData.getSize() -
					actualPositives - predictedPositives + truePositives);
			ruleEvaluations.put(rule, confusionMatrix);
		}
		// print ruleEvaluations
		for (String rule : ruleEvaluations.keySet()) {
			System.out.println(rule + ": " + ruleEvaluations.get(rule));
		}
		return ruleEvaluations;
	}
	
	public static Map<String, ConfusionMatrix> evaluateRulesUsingTestFeaturesData(Set<String> rules,
			Table testFeaturesTable, Project project, Table table1, Table table2) {
		Map<String, ConfusionMatrix> ruleEvaluations = new HashMap<String, ConfusionMatrix>();
		
		// count the actual positives in test data
		long actualPositives = 0;
		List<Attribute> attributes = testFeaturesTable.getAttributes();
		Attribute label = attributes.get(attributes.size()-1);
		for (Tuple tuple : testFeaturesTable.getAllTuples()) {
			if (tuple.getAttributeValue(label).equals(MatchStatus.MATCH.getLabel())) {
				actualPositives++;
			}
		}
		
		int ruleCnt = 0;
		for (String rule : rules) {
			System.out.println("Evaluating rule #" + ruleCnt);
			long predictedPositives = 0;
			long truePositives = 0;
			List<Term> terms = ParsingUtils.parseRuleFromDisplayString(project,
					rule);
			for (Tuple tuple : testFeaturesTable.getAllTuples()) {
				MatchStatus result = MatchStatus.MATCH;
				for (Term term : terms) {
					try {
						result = term.evaluate(tuple);
						if (MatchStatus.MATCH != result) {
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (result == MatchStatus.MATCH) {
					// predicted positive
					predictedPositives++;
					if (tuple.getAttributeValue(label).equals(MatchStatus.MATCH.getLabel())) {
						truePositives++;
					}
				}
			}
			ConfusionMatrix confusionMatrix = new ConfusionMatrix();
			confusionMatrix.setTruePositives(truePositives);
			confusionMatrix.setFalseNegatives(actualPositives - truePositives);
			confusionMatrix.setFalsePositives(predictedPositives - truePositives);
			confusionMatrix.setTrueNegatives(testFeaturesTable.getSize() -
					actualPositives - predictedPositives + truePositives);
			ruleEvaluations.put(rule, confusionMatrix);
			System.out.println(rule + ": " + confusionMatrix);
			ruleCnt++;
		}
		return ruleEvaluations;
	}
	
	public static Map<String, String> learnRulesUsingTrainingData(Table trainFeatureVectors,
			RuleBasedModel modelType) throws Exception {
		Instances instances = WekaUtils.getInstancesFromTable(trainFeatureVectors, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		trainInstances = WekaUtils.setClassIndex(trainInstances);
		List<String> ruleStrings = new ArrayList<String>();
		
		switch (modelType) {
		case PART:
			PART model1 = new PART();
			model1.buildClassifier(trainInstances);
			System.out.println("Model: " + model1);
			System.out.println("Rules\n--------");
			
			try {
				ruleStrings = model1.getDecisionListRules();
			}
			catch (Exception e) {
				System.err.println("Cannot get decision list rules due to " + e.getMessage());
				throw new Exception("[EMS] Cannot learn non-trivial rules due to insufficient training examples or features");
			}
			break;
		case J48:
			J48 model = new J48();
			model.buildClassifier(trainInstances);
			System.out.println("Model: " + model);
			System.out.println("Rules\n--------");
			
			try {
				ruleStrings = model.getDecisionTreeRules();
			}
			catch (Exception e) {
				System.err.println("Cannot get decision tree rules due to " + e.getMessage());
				throw new Exception("[EMS] Cannot learn non-trivial rules due to insufficient training examples or features");
			}
			break;
		case RF:
			RandomForest model2 = new RandomForest();
			model2.setNumExecutionSlots(8);
			Evaluation evaluation = new Evaluation(trainInstances);
			long startTime = System.currentTimeMillis();
			evaluation.crossValidateModel(model2, trainInstances, 10, new Random());
			long endTime = System.currentTimeMillis();
			System.out.println("Cross validation results");
			System.out.println("************************");
			System.out.println("Average accuracy: " + evaluation.pctCorrect());
			System.out.println("Average precision (class index = 0): " + evaluation.precision(0));
			System.out.println("Average recall (class index = 0): " + evaluation.recall(0));
			System.out.println("Average precision (class index = 1): " + evaluation.precision(1));
			System.out.println("Average recall (class index = 1): " + evaluation.recall(1));
			System.out.println("True Positives: " + evaluation.numTruePositives(0));
			System.out.println("False Positives: " + evaluation.numFalsePositives(0));
			System.out.println("True Negatives: " + evaluation.numTrueNegatives(0));
			System.out.println("False Negatives: " + evaluation.numFalseNegatives(0));
			System.out.println("Confusion Matrix: ");
			double[][] confusionMatrix = evaluation.confusionMatrix();
			for (int i = 0; i < confusionMatrix.length; i++) {
				for (int j = 0; j < confusionMatrix[i].length; j++) {
					System.out.print(confusionMatrix[i][j] + " ");
				}
				System.out.println();
			}
			System.out.println("Total time taken (ms): " + (endTime - startTime));
			System.out.println("Summary string: " + evaluation.toSummaryString());
			
			model2 = new RandomForest();
			model2.setNumExecutionSlots(8);
			model2.buildClassifier(trainInstances);
			System.out.println("Model: " + model2);
			System.out.println("Rules\n--------");
			
			try {
				List<List<String>> randomForestRules = model2.getRandomForestRules();
				ruleStrings = WekaUtils.collectAllRules(randomForestRules);
			}
			catch (Exception e) {
				System.err.println("Cannot get random forest rules due to " + e.getMessage());
				throw new Exception("[EMS] Cannot learn rules from the random forest");
			}
			break;
		default:
			System.err.println("Unsupported rule based model " + modelType);
			throw new IllegalArgumentException("Unsupported rule based model " + modelType);
		}
		
//		for (String rule : ruleStrings) {
//			System.out.println(rule);
//		}
//		
		/*
		List<String> posRuleStrings = WekaUtils.getPositiveRuleStrings(ruleStrings);
		List<String> coalescedPosRuleStrings = WekaUtils.getCoalescedRuleStrings(posRuleStrings);
		List<String> uniqueCoalescedPosRuleStrings = WekaUtils.getUniqueRuleStrings(coalescedPosRuleStrings);
		System.out.println("Total rules: " + ruleStrings.size() + ", Positive rules: " +
				posRuleStrings.size() + ", Coalesced rules: " +
				coalescedPosRuleStrings.size() + ", Unique rules: " +
				uniqueCoalescedPosRuleStrings.size());
		
		for (int i = 0; i < uniqueCoalescedPosRuleStrings.size(); i++) {
			List<Term> terms = ParsingUtils.parseRuleFromDisplayString(project, uniqueCoalescedPosRuleStrings.get(i));
			Rule rule = new Rule(ruleNamePrefix+i, project.getName(), table1Name, table2Name, terms);
			System.out.println("Rule " + i + ": " + rule.getDisplayString());
			rules.add(rule);
			project.addUnsavedRule(rule.getName());
		}
		project.addRules(rules);
		*/
		
		Map<String, String> posRuleStrings = WekaUtils.getPositiveRuleStrings2(ruleStrings);
		Map<String, String> coalescedPosRuleStrings = WekaUtils.getCoalescedRuleStrings2(posRuleStrings);
		//List<String> uniqueCoalescedPosRuleStrings = WekaUtils.getUniqueRuleStrings(coalescedPosRuleStrings);
		System.out.println("Total rules: " + ruleStrings.size() + ", Positive rules: " +
				posRuleStrings.size() + ", Coalesced rules: " +
				coalescedPosRuleStrings.size());
		return coalescedPosRuleStrings;
	}
	
	public static void addRule(String projectName, String ruleName,
			String ruleString,
			boolean saveToDisk) throws IOException {

		Project project = ProjectDao.open(projectName);
		List<Term> terms = ParsingUtils.parseRuleFromDisplayString(project, ruleString);

		System.out.println("[RuleDao:addRule] No. of terms: " + terms.size());
		Rule rule = new Rule(ruleName, projectName, terms);
		System.out.println("[RuleDao:addRule] Rule: " + rule);

		// add rule to project
		project.addRule(rule);
		if(saveToDisk) {
			// append to all.rules file
			RuleSaver.addRule(projectName, rule);
		}
		else {
			// put this rule in the unsavedRules
			project.addUnsavedRule(ruleName);
		}
		// update project
		ProjectDao.updateProject(project);
	}
	
	/*
	public static List<String> getRuleNames(Project project, String table1Name,
			String table2Name) {
		List<String> ruleNames = new ArrayList<String>();
		for (Rule r : project.getRules()) {
			if (r.getTable1Name().equals(table1Name) && r.getTable2Name().equals(table2Name)) {
				ruleNames.add(r.getName());
			}
		}
		return ruleNames;
	}
	*/
	
	public static List<String> learnRulesUsingActiveLearning(Table seedFeatureVectors, 
			Table unlabeledFeatureVectors, int numIterations) throws Exception {
		Instances instances = WekaUtils.getInstancesFromTable(seedFeatureVectors, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		RandomForest model = new RandomForest();
		model.setNumExecutionSlots(8);
		model.buildClassifier(trainInstances);
		System.out.println("Model: " + model);
		System.out.println("Rules\n--------");
		List<String> ruleStrings = new ArrayList<String>();
		try {
			List<List<String>> randomForestRules = model.getRandomForestRules();
			ruleStrings = WekaUtils.collectAllRules(randomForestRules);
		}
		catch (Exception e) {
			System.err.println("Cannot get random forest rules due to " + e.getMessage());
			throw new Exception("[EMS] Cannot learn rules from the random forest");
		}
		return ruleStrings;
	}
	
	public static Map<String, String> getRulesFromModel(RandomForest model) {
		List<List<String>> randomForestRules = model.getRandomForestRules();
		List<String> ruleStrings = WekaUtils.collectAllRules(randomForestRules);
		Map<String, String> posRuleStrings = WekaUtils.getPositiveRuleStrings2(ruleStrings);
		Map<String, String> coalescedPosRuleStrings = WekaUtils.getCoalescedRuleStrings2(posRuleStrings);
		//List<String> uniqueCoalescedPosRuleStrings = WekaUtils.getUniqueRuleStrings(coalescedPosRuleStrings);
		System.out.println("Total rules: " + ruleStrings.size() + ", Positive rules: " +
				posRuleStrings.size() + ", Coalesced rules: " +
				coalescedPosRuleStrings.size());
		return coalescedPosRuleStrings;
	}
	
	private static Term getTerm(Project project, String featureName,
			String opName, String val){
		Term term = null;
		if(null != val && !val.isEmpty()){
			float value = Float.parseFloat(val);
			Feature feature = project.findFeatureByName(featureName);
			RelationalOperator op = RelationalOperator.valueOfFromName(opName);
			term = new Term(feature, op, value);
		}
		return term;
	}
	
	public static void editRuleUsingGui(Project project, String ruleName,
			List<String> featureNames, List<String> operators,
			List<String> values, boolean saveToDisk) throws IOException {

		Rule rule = project.findRuleByName(ruleName);
		List<Term> terms = new ArrayList<Term>();

		for (int i = 0; i < featureNames.size(); i++) {
			Term term = getTerm(project, featureNames.get(i), operators.get(i),
					values.get(i));
			if (null != term) {
				terms.add(term);
			}
		}

		System.out.println("[RuleService:editRuleUsingGui] No. of terms: " +
				terms.size());
		rule.setTerms(terms);
		System.out.println("[RuleService:editRuleUsingGui] Rule: " + rule);

		
		if (saveToDisk) {
			// save the rule in all.rules file
			RuleSaver.saveRule(project.getName(), rule, project);
			// remove the rule from unsaved rules
			project.removeUnsavedRule(ruleName);
		}
		else {
			// put this rule in the unsavedRules
			project.addUnsavedRule(ruleName);
		}		
		// update project
		ProjectDao.updateProject(project);
	}
	
	public static void deleteRule(String projectName, String ruleName)
			throws IOException {
		Project project = ProjectDao.open(projectName);
		Rule rule = project.findRuleByName(ruleName);
		RuleSaver.deleteRule(projectName, rule, project);
			
		// update project
		project.removeUnsavedRule(ruleName);
		project.deleteRule(rule);
		ProjectDao.updateProject(project);
	}
	
}