package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.savers.MatcherSaver;
import com.walmart.productgenome.matching.utils.WekaUtils;

public class MatcherService {

	public enum LearningBasedModel {
		NB ("Naive Bayes"),
		IBK ("K-Nearest Neighbors"),
		J48 ("Decision Tree"),
		SMO ("Support Vector Machine"),
		RF ("Random Forest"),
		ABM1 ("Adaptive Boosting (AdaBoostM1)"),
		ALR ("Additive Logistic Regression with Decision Stumps"),
		GBRT ("Gradient Boosting with REPTrees"),
		STACKING ("Stacking (Logistic Regression on J48 and IBK)");

		private String displayName;

		private LearningBasedModel(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
	private static Rule getRule(Project project, String ruleName) {
		Rule rule = null;
		if (null != ruleName && !ruleName.isEmpty()) {
			rule = project.findRuleByName(ruleName);
		}
		return rule;
	}

	public static void addMatcher(Project project, String matcherName,
			List<String> ruleNames, boolean saveToDisk) throws IOException {

		List<Rule> rules = new ArrayList<Rule>();

		for (String ruleName : ruleNames) {
			Rule rule = getRule(project, ruleName);
			if (rule != null) {
				rules.add(rule);
			}
		}

		Matcher matcher = new Matcher(matcherName, project.getName(), rules);

		// add matcher to project
		project.addMatcher(matcher);
		if(saveToDisk) {
			// append to all.matchers file
			MatcherSaver.addMatcher(project.getName(), matcher);
		}
		else {
			// put this matcher in the unsavedMatchers
			project.addUnsavedMatcher(matcherName);
		}

		// update project
		ProjectDao.updateProject(project);
	}

	public static void editMatcherUsingGui(Project project, String matcherName,
			List<String> ruleNames, boolean saveToDisk) throws IOException {

		Matcher matcher = project.findMatcherByName(matcherName);
		List<Rule> rules = new ArrayList<Rule>();
		for (String ruleName : ruleNames) {
			Rule rule = getRule(project, ruleName);
			if (rule != null) {
				rules.add(rule);
			}
		}
		matcher.setRules(rules);
		if(saveToDisk) {
			// save the matcher in all.matchers file
			MatcherSaver.saveMatcher(project.getName(), matcher, project);
			// remove the matcher from unsaved matchers
			project.removeUnsavedMatcher(matcherName);
		}
		else {
			// put this matcher in the unsavedMatchers
			project.addUnsavedMatcher(matcherName);
		}		
		// update project
		ProjectDao.updateProject(project);
	}

	public static void deleteMatcher(String projectName, String matcherName)
			throws IOException {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		MatcherSaver.deleteMatcher(projectName, matcher, project);

		// update project
		project.removeUnsavedMatcher(matcherName);
		project.deleteMatcher(matcher);
		ProjectDao.updateProject(project);
	}

	private static ConfusionMatrix evaluateModel(AbstractClassifier classifier,
			Instances trainInstances, int numFolds) throws Exception {
		long startTime = System.currentTimeMillis();
		Evaluation eval = new Evaluation(trainInstances);
		eval.crossValidateModel(classifier, trainInstances, numFolds, new Random());
		long cvTime = System.currentTimeMillis() - startTime;
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.setTruePositives((long)eval.numTruePositives(0));
		cm.setFalseNegatives((long)eval.numFalseNegatives(0));
		cm.setTrueNegatives((long)eval.numTrueNegatives(0));
		cm.setFalsePositives((long)eval.numFalsePositives(0));
		cm.setTimeMillis(cvTime);
		StringBuilder sb = new StringBuilder();
		for (String s: classifier.getOptions()) {
			sb.append(s);
			sb.append("\n");
		}
		cm.setModelOptions(sb.toString());
		return cm;
	}

	private static ConfusionMatrix evaluateModelWithMatchingThreshold(AbstractClassifier classifier,
			Instances trainInstances, int numRuns, int numFolds, double matchingThreshold) throws Exception {
		long startTime = System.currentTimeMillis();
		// perform cross-validation
		System.out.println();
		System.out.println("=== Setup ===");
		System.out.println("Classifier: " + classifier.getClass().getName());
		System.out.println("Dataset: " + trainInstances.relationName());
		System.out.println("Runs: " + numRuns);
		System.out.println("Folds: " + numFolds);
		System.out.println("Matching Threshold: " + matchingThreshold);
		System.out.println();
		int totalTp = 0; int totalFp = 0; int totalAp = 0; int totalAn = 0;
		for (int r = 0; r < numRuns; r++) {
			Random rand = new Random();
			Instances randData = new Instances(trainInstances);
			randData.randomize(rand);
			if (randData.classAttribute().isNominal())
				randData.stratify(numFolds);
			int sumTp = 0; int sumFp = 0; int sumAp = 0; int sumAn = 0;
			for (int n = 0; n < numFolds; n++) {
				Instances train = randData.trainCV(numFolds, n);
				Instances test = randData.testCV(numFolds, n);

				// build and evaluate classifier
				Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
				clsCopy.buildClassifier(train);
				int ap = 0; int an = 0; int tp = 0; int fp = 0;
				for (int i = 0; i < test.numInstances(); i++) {
					Instance testInstance = test.instance(i);
					//double clsLabel = clsCopy.classifyInstance(testInstance);
					double[] classProbabilities = clsCopy.distributionForInstance(testInstance);
					System.out.println("Id: " + testInstance.value(0) +
							", p1 = " + classProbabilities[0] + ", p2 = " + classProbabilities[1]);
					//System.out.println("testInstance: " + testInstance.toString() + ", classValue: " + testInstance.classValue());
					if (Double.compare(0.0, testInstance.classValue()) == 0) {
						// actual positive
						ap++;
						if (classProbabilities[0] >= matchingThreshold) {
							tp++;
						}
					}
					else {
						an++;
						if (classProbabilities[0] >= matchingThreshold) {
							fp++;
						}
					}   
				}
				System.out.println("Fold " + n + ": TP = " + tp + ", FP = " + fp + ", AP = " + ap + ", AN = " + an);
				sumTp += tp; sumFp += fp; sumAp += ap; sumAn += an;
			}
			totalTp += sumTp; totalFp += sumFp; totalAp += sumAp; totalAn += sumAn;
			// output evaluation
			System.out.println();
			System.out.println("Run " + r + ": TP = " + sumTp + ", FP = " + sumFp + ", AP = " + sumAp + ", AN = " + sumAn);
		}
		System.out.println();
		System.out.println("Overall: TP = " + totalTp + ", FP = " + totalFp + ", AP = " + totalAp + ", AN = " + totalAn);
		long cvTime = System.currentTimeMillis() - startTime;
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.setTruePositives(totalTp);
		cm.setFalseNegatives(totalAp - totalTp);
		cm.setTrueNegatives(totalAn - totalFp);
		cm.setFalsePositives(totalFp);
		cm.setTimeMillis(cvTime);
		StringBuilder sb = new StringBuilder();
		for (String s: classifier.getOptions()) {
			sb.append(s);
			sb.append("\n");
		}
		cm.setModelOptions(sb.toString());
		return cm;
	}

	private static ConfusionMatrix evaluateModel(AbstractClassifier classifier,
			Instances trainInstances, Instances testInstances) throws Exception {
		long startTime = System.currentTimeMillis();
		classifier.buildClassifier(trainInstances);
		Evaluation eval = new Evaluation(trainInstances);
		eval.evaluateModel(classifier, testInstances);
		long trainTestTime = System.currentTimeMillis() - startTime;
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.setTruePositives((long)eval.numTruePositives(0));
		cm.setFalseNegatives((long)eval.numFalseNegatives(0));
		cm.setTrueNegatives((long)eval.numTrueNegatives(0));
		cm.setFalsePositives((long)eval.numFalsePositives(0));
		cm.setTimeMillis(trainTestTime);
		StringBuilder sb = new StringBuilder();
		for (String s: classifier.getOptions()) {
			sb.append(s);
			sb.append("\n");
		}
		cm.setModelOptions(sb.toString());
		return cm;
	}

	public static Map<String, ConfusionMatrix> evaluateModelsUsingCV(Table trainFeaturesTable,
			String[] modelNames, int numFolds) throws Exception {
		Instances instances = WekaUtils.getInstancesFromTable(trainFeaturesTable, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		trainInstances = WekaUtils.setClassIndex(trainInstances);

		Map<String, ConfusionMatrix> modelEvaluations = new HashMap<String, ConfusionMatrix>();
		for (String s: modelNames) {
			if	("RF".equals(s)) {
				RandomForest rf = new RandomForest();
				rf.setNumExecutionSlots(8);
				ConfusionMatrix cm = evaluateModel(rf, trainInstances, numFolds);
				cm.setModelInfo(rf.globalInfo());
				modelEvaluations.put("Random Forest", cm);
			}
			else if ("J48".equals(s)) {
				J48 j48 = new J48();
				ConfusionMatrix cm = evaluateModel(j48, trainInstances, numFolds);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree", cm);
			}
			else if ("SMO".equals(s)) {
				SMO smo = new SMO();
				ConfusionMatrix cm = evaluateModel(smo, trainInstances, numFolds);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine", cm);
			}
			else if ("NB".equals(s)) {
				NaiveBayes nb = new NaiveBayes();
				ConfusionMatrix cm = evaluateModel(nb, trainInstances, numFolds);
				cm.setModelInfo(nb.globalInfo());
				modelEvaluations.put("Naive Bayes", cm);
			}
			else if ("IBK".equals(s)) {
				IBk ibk = new IBk();
				ConfusionMatrix cm = evaluateModel(ibk, trainInstances, numFolds);
				cm.setModelInfo(ibk.globalInfo());
				modelEvaluations.put("K-Nearest Neighbors", cm);
			}
			else if ("ABM1".equals(s)) {
				AdaBoostM1 abm1 = new AdaBoostM1();
				ConfusionMatrix cm = evaluateModel(abm1, trainInstances, numFolds);
				cm.setModelInfo(abm1.globalInfo());
				modelEvaluations.put("Adaptive Boosting (AdaBoostM1)", cm);
			}
			else if ("STACKING".equals(s)) {
				Stacking stacking = new Stacking();
				stacking.setMetaClassifier(new Logistic());
				Classifier[] baseClassifiers = new Classifier[2];
				baseClassifiers[0] = new J48();
				baseClassifiers[1] = new IBk();
				stacking.setClassifiers(baseClassifiers);
				ConfusionMatrix cm = evaluateModel(stacking, trainInstances, numFolds);
				cm.setModelInfo(stacking.globalInfo());
				modelEvaluations.put("Stacking", cm);
			}
			else if ("LB".equals(s)) {
				LogitBoost lb = new LogitBoost();
				ConfusionMatrix cm = evaluateModel(lb, trainInstances, numFolds);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Additive Logistic Regression", cm);
			}
			else if ("GBRT".equals(s)) {
				LogitBoost lb = new LogitBoost();
				REPTree rep = new REPTree();
				rep.setMaxDepth(6);
				lb.setClassifier(rep);
				lb.setNumIterations(50);
				lb.setShrinkage(0.1);
				ConfusionMatrix cm = evaluateModel(lb, trainInstances, numFolds);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Gradient Boosting Trees", cm);
			}
		}
		return modelEvaluations;
	}

	public static Map<String, ConfusionMatrix> evaluateModelsUsingCVWithMatchingThreshold(Table trainFeaturesTable,
			String[] modelNames, int numRuns, int numFolds, double matchingThreshold) throws Exception {
		Instances instances = WekaUtils.getInstancesFromTable(trainFeaturesTable, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		trainInstances = WekaUtils.setClassIndex(trainInstances);

		Map<String, ConfusionMatrix> modelEvaluations = new HashMap<String, ConfusionMatrix>();
		for (String s: modelNames) {
			if	("RF".equals(s)) {
				RandomForest rf = new RandomForest();
				rf.setNumExecutionSlots(8);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(rf, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(rf.globalInfo());
				modelEvaluations.put("Random Forest", cm);
			}
			else if ("J48_1".equals(s)) {
				// default: -C 0.25 -M 2
				J48 j48 = new J48();
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 1 (default)", cm);
			}
			else if ("J48_2".equals(s)) {
				// -C 0.25 -M 5
				J48 j48 = new J48();
				j48.setMinNumObj(5);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 2", cm);
			}
			else if ("J48_3".equals(s)) {
				// -C 0.25 -M 10
				J48 j48 = new J48();
				j48.setMinNumObj(10);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 3", cm);
			}
			else if ("J48_4".equals(s)) {
				// -C 0.1 -M 2
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.1f);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 4", cm);
			}
			else if ("J48_5".equals(s)) {
				// -C 0.1 -M 5
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.1f);
				j48.setMinNumObj(5);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 5", cm);
			}
			else if ("J48_6".equals(s)) {
				// -C 0.1 -M 10
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.1f);
				j48.setMinNumObj(10);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 6", cm);
			}
			else if ("J48_7".equals(s)) {
				// -C 0.5 -M 2
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.5f);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 7", cm);
			}
			else if ("J48_8".equals(s)) {
				// -C 0.5 -M 5
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.5f);
				j48.setMinNumObj(5);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 8", cm);
			}
			else if ("J48_9".equals(s)) {
				// -C 0.5 -M 10
				J48 j48 = new J48();
				j48.setConfidenceFactor(0.5f);
				j48.setMinNumObj(10);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(j48, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree 9", cm);
			}
			else if ("SMO1".equals(s)) {
				// default: -N 0 -K Poly -E 1
				SMO smo = new SMO();
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 1 (default)", cm);
			}
			else if ("SMO2".equals(s)) {
				// -N 1 -K Poly -E 1
				SMO smo = new SMO();
				String[] options = weka.core.Utils.splitOptions("-N 1");
				smo.setOptions(options);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 2", cm);
			}
			else if ("SMO3".equals(s)) {
				// -N 2 -K Poly -E 1
				SMO smo = new SMO();
				String[] options = weka.core.Utils.splitOptions("-N 2");
				smo.setOptions(options);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 3", cm);
			}
			else if ("SMO4".equals(s)) {
				// -N 0 -K Poly -E 2
				SMO smo = new SMO();
				Kernel kernel = smo.getKernel();
				String[] options = weka.core.Utils.splitOptions("-E 2");
				kernel.setOptions(options);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 4", cm);
			}
			else if ("SMO5".equals(s)) {
				// -N 1 -K Poly -E 2
				SMO smo = new SMO();
				String[] options1 = weka.core.Utils.splitOptions("-N 1");
				smo.setOptions(options1);
				String[] options2 = weka.core.Utils.splitOptions("-E 2");
				Kernel kernel = smo.getKernel();
				kernel.setOptions(options2);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 5", cm);
			}
			else if ("SMO6".equals(s)) {
				// -N 2 -K Poly -E 2
				SMO smo = new SMO();
				String[] options1 = weka.core.Utils.splitOptions("-N 2");
				smo.setOptions(options1);
				String[] options2 = weka.core.Utils.splitOptions("-E 2");
				Kernel kernel = smo.getKernel();
				kernel.setOptions(options2);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 6", cm);
			}
			else if ("SMO7".equals(s)) {
				// -N 0 -K RBF -G 0.01
				SMO smo = new SMO();
				Kernel kernel = new RBFKernel();
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 7", cm);
			}
			else if ("SMO8".equals(s)) {
				// -N 1 -K RBF -G 0.01
				SMO smo = new SMO();
				String[] options = weka.core.Utils.splitOptions("-N 1");
				smo.setOptions(options);
				Kernel kernel = new RBFKernel();
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 8", cm);
			}
			else if ("SMO9".equals(s)) {
				// -N 2 -K RBF -G 0.01
				SMO smo = new SMO();
				String[] options = weka.core.Utils.splitOptions("-N 2");
				smo.setOptions(options);
				Kernel kernel = new RBFKernel();
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 9", cm);
			}
			else if ("SMO10".equals(s)) {
				// -N 0 -K RBF -G 0.1
				SMO smo = new SMO();
				String[] options = weka.core.Utils.splitOptions("-G 0.1");
				Kernel kernel = new RBFKernel();
				kernel.setOptions(options);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 10", cm);
			}
			else if ("SMO11".equals(s)) {
				// -N 1 -K RBF -G 0.1
				SMO smo = new SMO();
				String[] options1 = weka.core.Utils.splitOptions("-N 1");
				smo.setOptions(options1);
				String[] options2 = weka.core.Utils.splitOptions("-G 0.1");
				Kernel kernel = new RBFKernel();
				kernel.setOptions(options2);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 11", cm);
			}
			else if ("SMO12".equals(s)) {
				// -N 2 -K RBF -G 0.1
				SMO smo = new SMO();
				String[] options1 = weka.core.Utils.splitOptions("-N 2");
				smo.setOptions(options1);
				String[] options2 = weka.core.Utils.splitOptions("-G 0.1");
				Kernel kernel = new RBFKernel();
				kernel.setOptions(options2);
				smo.setKernel(kernel);
				smo.setBuildLogisticModels(true);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(smo, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine 12", cm);
			}
			else if ("NB".equals(s)) {
				NaiveBayes nb = new NaiveBayes();
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(nb, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(nb.globalInfo());
				modelEvaluations.put("Naive Bayes", cm);
			}
			else if ("IBK".equals(s)) {
				IBk ibk = new IBk();
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(ibk, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(ibk.globalInfo());
				modelEvaluations.put("K-Nearest Neighbors", cm);
			}
			else if ("ABM1".equals(s)) {
				AdaBoostM1 abm1 = new AdaBoostM1();
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(abm1, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(abm1.globalInfo());
				modelEvaluations.put("Adaptive Boosting (AdaBoostM1)", cm);
			}
			else if ("STACKING".equals(s)) {
				Stacking stacking = new Stacking();
				stacking.setMetaClassifier(new Logistic());
				Classifier[] baseClassifiers = new Classifier[2];
				baseClassifiers[0] = new J48();
				baseClassifiers[1] = new IBk();
				stacking.setClassifiers(baseClassifiers);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(stacking, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(stacking.globalInfo());
				modelEvaluations.put("Stacking", cm);
			}
			else if ("LB".equals(s)) {
				LogitBoost lb = new LogitBoost();
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(lb, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Additive Logistic Regression", cm);
			}
			else if ("GBRT".equals(s)) {
				LogitBoost lb = new LogitBoost();
				REPTree rep = new REPTree();
				rep.setMaxDepth(6);
				lb.setClassifier(rep);
				lb.setNumIterations(50);
				lb.setShrinkage(0.1);
				ConfusionMatrix cm = evaluateModelWithMatchingThreshold(lb, trainInstances, numRuns, numFolds, matchingThreshold);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Gradient Boosting Trees", cm);
			}
		}
		return modelEvaluations;
	}

	public static Map<String, ConfusionMatrix> evaluateModelsUsingTrainTest(Table trainFeaturesTable,
			Table testFeaturesTable, String[] modelNames) throws Exception {
		Instances instances1 = WekaUtils.getInstancesFromTable(trainFeaturesTable, true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances1);
		trainInstances = WekaUtils.setClassIndex(trainInstances);

		Instances instances2 = WekaUtils.getInstancesFromTable(testFeaturesTable, true, false);
		Instances testInstances = WekaUtils.applyFilterToInstances(instances2);
		testInstances = WekaUtils.setClassIndex(testInstances);

		Map<String, ConfusionMatrix> modelEvaluations = new HashMap<String, ConfusionMatrix>();
		for (String s: modelNames) {
			if	("RF".equals(s)) {
				RandomForest rf = new RandomForest();
				rf.setNumExecutionSlots(8);
				ConfusionMatrix cm = evaluateModel(rf, trainInstances, testInstances);
				cm.setModelInfo(rf.globalInfo());
				modelEvaluations.put("Random Forest", cm);
			}
			else if ("J48".equals(s)) {
				J48 j48 = new J48();
				ConfusionMatrix cm = evaluateModel(j48, trainInstances, testInstances);
				cm.setModelInfo(j48.globalInfo());
				modelEvaluations.put("Decision Tree", cm);
			}
			else if ("SMO".equals(s)) {
				SMO smo = new SMO();
				ConfusionMatrix cm = evaluateModel(smo, trainInstances, testInstances);
				cm.setModelInfo(smo.globalInfo());
				modelEvaluations.put("Support Vector Machine", cm);
			}
			else if ("NB".equals(s)) {
				NaiveBayes nb = new NaiveBayes();
				ConfusionMatrix cm = evaluateModel(nb, trainInstances, testInstances);
				cm.setModelInfo(nb.globalInfo());
				modelEvaluations.put("Naive Bayes", cm);
			}
			else if ("IBK".equals(s)) {
				IBk ibk = new IBk();
				ConfusionMatrix cm = evaluateModel(ibk, trainInstances, testInstances);
				cm.setModelInfo(ibk.globalInfo());
				modelEvaluations.put("K-Nearest Neighbors", cm);
			}
			else if ("ABM1".equals(s)) {
				AdaBoostM1 abm1 = new AdaBoostM1();
				ConfusionMatrix cm = evaluateModel(abm1, trainInstances, testInstances);
				cm.setModelInfo(abm1.globalInfo());
				modelEvaluations.put("Adaptive Boosting (AdaBoostM1)", cm);
			}
			else if ("STACKING".equals(s)) {
				Stacking stacking = new Stacking();
				stacking.setMetaClassifier(new Logistic());
				Classifier[] baseClassifiers = new Classifier[2];
				baseClassifiers[0] = new J48();
				baseClassifiers[1] = new IBk();
				stacking.setClassifiers(baseClassifiers);
				ConfusionMatrix cm = evaluateModel(stacking, trainInstances, testInstances);
				cm.setModelInfo(stacking.globalInfo());
				modelEvaluations.put("Stacking", cm);
			}
			else if ("LB".equals(s)) {
				LogitBoost lb = new LogitBoost();
				ConfusionMatrix cm = evaluateModel(lb, trainInstances, testInstances);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Additive Logistic Regression", cm);
			}
			else if ("GBRT".equals(s)) {
				LogitBoost lb = new LogitBoost();
				REPTree rep = new REPTree();
				rep.setMaxDepth(6);
				lb.setClassifier(rep);
				lb.setNumIterations(50);
				lb.setShrinkage(0.1);
				ConfusionMatrix cm = evaluateModel(lb, trainInstances, testInstances);
				cm.setModelInfo(lb.globalInfo());
				modelEvaluations.put("Gradient Boosting Trees", cm);
			}
		}
		return modelEvaluations;
	}
}
