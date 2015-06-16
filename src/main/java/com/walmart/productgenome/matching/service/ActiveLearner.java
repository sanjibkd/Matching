package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmart.productgenome.matching.models.audit.MatchStatus;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.utils.WekaUtils;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

public class ActiveLearner {

	public static final double DEFAULT_VALIDATION_PERCENT = 3; // default is 3% validation, 97% test
	public static final int DEFAULT_BATCH_SIZE = 10; // default is a batch of 10 pairs
	
	private Table labeledFeatureVectors;
	private Table unlabeledTestFeatureVectors;
	private Table unlabeledValidationFeatureVectors;
	private List<RandomForest> randomForestModels;
	private List<Double> averageConfidenceofModels; 
	private double validationPercent;
	private int batchSize;
	private Instances validationInstances;
	private List<Object> pairIds;
	
	public ActiveLearner(Table labeledFeatureVectors,
			Table unlabeledFeatureVectors) throws IOException {
		this(labeledFeatureVectors, unlabeledFeatureVectors,
				DEFAULT_VALIDATION_PERCENT, DEFAULT_BATCH_SIZE);
	}
	
	public ActiveLearner(Table labeledFeatureVectors,
			Table unlabeledFeatureVectors, double validationPercent,
			int batchSize) throws IOException {
		this.validationPercent = validationPercent;
		this.batchSize = batchSize;
		this.labeledFeatureVectors = new Table(labeledFeatureVectors);
		Table unlabeledFeatureVectorsCopy = new Table(unlabeledFeatureVectors);
		Table[] validationTestSplits = TableService.split(unlabeledFeatureVectorsCopy,
				this.validationPercent);
		unlabeledValidationFeatureVectors = validationTestSplits[0];
		unlabeledTestFeatureVectors = validationTestSplits[1];
		randomForestModels = new ArrayList<RandomForest>();
		averageConfidenceofModels = new ArrayList<Double>();
		validationInstances = WekaUtils.getInstancesFromTable(unlabeledValidationFeatureVectors, false, true);
		validationInstances = WekaUtils.setClassIndex(validationInstances);
	}
	
	/**
	 * returns the next batch of informative tuple pairs for labeling
	 * @return
	 */
	public List<Object> runNextIteration() throws Exception {
		Instances instances = WekaUtils.getInstancesFromTable(labeledFeatureVectors,
				true, false);
		Instances trainInstances = WekaUtils.applyFilterToInstances(instances);
		trainInstances = WekaUtils.setClassIndex(trainInstances);
		RandomForest model = new RandomForest();
		model.buildClassifier(trainInstances);
		System.out.println(model);
		double averageConfidence = WekaUtils.getAverageConfidence(validationInstances,
				model);
		System.out.println("Avg Confidence of model: " + averageConfidence);
		randomForestModels.add(model);
		averageConfidenceofModels.add(averageConfidence);
		if (unlabeledTestFeatureVectors.getSize() <= batchSize) {
			System.out.println("Getting all unlabeled pairs");
			System.out.println("Unlabeled Pairs: " +
					unlabeledTestFeatureVectors.getSize() + ", Batch size: " +
					batchSize);
			pairIds = getAllPairs();
		}
		else {
			System.out.println("Getting top K informative pairs");
			Instances testInstances = WekaUtils.getInstancesFromTable(unlabeledTestFeatureVectors,
					false, true);
			testInstances = WekaUtils.setClassIndex(testInstances);
			pairIds = getTopKInformativePairs(model, testInstances);
		}
		return pairIds;
	}
	
	private List<Object> getAllPairs() {
		List<Object> allPairIds = Lists.newArrayList();
		for (Object pairId : unlabeledTestFeatureVectors.getAllIdsInOrder()) {
			allPairIds.add(pairId);
		}
		return allPairIds;
	}
	
	/**
	 * Returns the most informative K tuple pairs from unlabeled test instances
	 * using the learned classifier.
	 * 
	 * The pairs for which maximum entropy has been calculated by the classifier
	 * are the most informative examples because the classifier is most confused
	 * about these pairs
	 * 
	 * @return
	 */
	private List<Object> getTopKInformativePairs(RandomForest model, Instances testInstances) {
		PriorityQueue<InstanceEntropy> mostInfoItemPairs = 
			new PriorityQueue<InstanceEntropy>(batchSize);
		
		double maxEntropy = 0.0;
		for (Instance instance : testInstances) {
			double entropy = 0.0;
			try {
				entropy = model.getVotingEntropyForInstance(instance);
				if (Double.compare(entropy, maxEntropy) > 0) {
					maxEntropy = entropy;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (mostInfoItemPairs.size() < batchSize) {
				mostInfoItemPairs.add(new InstanceEntropy(instance, entropy));
			}
			else {
				InstanceEntropy head = mostInfoItemPairs.peek();
				if (Double.compare(head.entropy,entropy) < 0) {
					mostInfoItemPairs.remove(head);
					mostInfoItemPairs.add(new InstanceEntropy(instance, entropy));
				}				
			}
		}
		
		List<Object> topKInfoPairIds = Lists.newArrayList();
		System.out.println("Top-k informative pairs");
		for (InstanceEntropy instanceEntropy : mostInfoItemPairs) {
			Instance instance = instanceEntropy.instance;
			double entropy = instanceEntropy.entropy;
			//System.out.println("Instance: " + instance);
			//System.out.println("Entropy: " + entropy);
			// Get the first attribute value (pairId) from weka instance
			//System.out.println("Instance pairId: " + instance.value(0));
			topKInfoPairIds.add((int) instance.value(0)); //TODO: fix this later
		}
		
		return topKInfoPairIds;
	}
	
	private static class InstanceEntropy implements Comparable<InstanceEntropy> {
		private Instance instance;
		private double entropy;
		
		public InstanceEntropy(Instance instance, double entropy) {
			this.instance = instance;
			this.entropy = entropy;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof InstanceEntropy) {
				InstanceEntropy that = (InstanceEntropy)obj;
				return Objects.equal(this.instance, that.instance) &&
						Objects.equal(this.entropy, that.entropy);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.instance, this.entropy);
		}
		
		public int compareTo(InstanceEntropy that) {
			return Double.compare(this.entropy, that.entropy);
		}
	}
	
	public void processUserLabeledPairs(List<MatchStatus> matchStatuses,
			List<String> comments) {
		for (int i = 0; i < pairIds.size(); i++) {
			Object pairId = pairIds.get(i);
			Tuple pairFeatureVector = unlabeledTestFeatureVectors.getTuple(pairId);
			List<Attribute> attributes = labeledFeatureVectors.getAttributes();
			Attribute labelAttribute = attributes.get(attributes.size() - 1);
			pairFeatureVector.setAttributeValue(labelAttribute,
					matchStatuses.get(i).getLabel());
			labeledFeatureVectors.addTuple(pairFeatureVector);
			unlabeledTestFeatureVectors.removeTuple(pairId);
		}
	}
	
	public List<RandomForest> getLearnedModels() {
		return randomForestModels;
	}
	
	public List<Double> getAverageConfidenceOfModels() {
		return averageConfidenceofModels;
	}
}
