package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.data.Attribute.Type;
import com.walmart.productgenome.matching.models.data.AttributePair;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.models.savers.FeatureSaver;
import com.walmart.productgenome.matching.models.savers.FunctionSaver;

public class FeatureService {

	public static Table generateFeatures(String featureTableName, String projectName,
			Table pairsTable, Table table1, Table table2, List<Feature> features,
			boolean hasLabel) {
		Attribute idAttribute = pairsTable.getIdAttribute();
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(idAttribute);
		for (Feature f : features) {
			attributes.add(new Attribute(f.getName(), Type.FLOAT));
		}
		List<Attribute> pairsAttributes = pairsTable.getAttributes();
		Attribute labelAttribute = pairsAttributes.get(pairsAttributes.size()-1);
		if (hasLabel) { 
			attributes.add(labelAttribute);
		}
		Table featureVectors = new Table(featureTableName, idAttribute,
				attributes, projectName);

		Attribute id1 = pairsAttributes.get(1);
		Attribute id2 = pairsAttributes.get(2); 
		for (Tuple pair : pairsTable.getAllTuplesInOrder()) {
			Object id1Val = pair.getAttributeValue(id1);
			Object id2Val = pair.getAttributeValue(id2);
			Tuple tuple1 = table1.getTuple(id1Val);
			Tuple tuple2 = table2.getTuple(id2Val);
			Map<Attribute, Object> featureData = new HashMap<Attribute, Object>();
			featureData.put(idAttribute, pair.getAttributeValue(idAttribute));
			for (int i = 0; i < features.size(); i++) {
				float featureVal = features.get(i).compute(tuple1, tuple2); 
				featureData.put(attributes.get(i+1), featureVal);
			}
			if (hasLabel) {
				featureData.put(labelAttribute, pair.getAttributeValue(labelAttribute));
			}
			featureVectors.addTuple(new Tuple(featureData));
		}
		return featureVectors;
	}

	private static int getAverageWordCount(Table table, Attribute attribute) {
		// do one pass of table to count the number of words
		long numWords = 0;
		int numNonEmptyVals = 0;
		List<Object> attributeValues = table.getAllValuesForAttribute(attribute);
		for (Object o : attributeValues){
			if (null == o) {
				continue;
			}
			String s = (String) o;
			s = s.trim();
			if (s.isEmpty()) {
				continue;
			}
			numWords += s.split("\\s+").length;
			numNonEmptyVals++;
		}
		System.out.println("Scanned " + attribute.getName() + " of " +
				table.getName() + ". #words: " + numWords + ", #non-empty tuples: " +
				numNonEmptyVals);
		return Math.round(numWords/numNonEmptyVals);
	}

	private static AllTypes getStringType(Table table1, Table table2,
			Attribute attribute1, Attribute attribute2) {
		int avg1 = getAverageWordCount(table1, attribute1);
		int avg2 = getAverageWordCount(table2, attribute2);
		int overallAvg = Math.max(avg1, avg2);
		System.out.println("Table " + table1.getName() + ", Attribute " +
				attribute1.getName() + ", Avg word count: " + avg1);
		System.out.println("Table " + table2.getName() + ", Attribute " +
				attribute2.getName() + ", Avg word count: " + avg2);
		if (overallAvg <= 1) {
			return AllTypes.SINGLE_WORD_STRING;
		}
		else if (overallAvg <= 5) {
			return AllTypes.MULTI_WORD_SHORT_STRING;
		}
		else if (overallAvg <= 10) {
			return AllTypes.MULTI_WORD_MEDIUM_STRING;
		}
		else{
			return AllTypes.MULTI_WORD_LONG_STRING;
		}
	}

	private static String getInitials(String s) {
		String[] v = s.split("_");
		if (v.length == 1) {
			return s;
		}
		String initials = "";
		for (int i = 0; i < v.length; i++) {
			initials += v[i].charAt(0);
		}
		return initials;
	}
	
	private static String getFeatureName(
			String  attribute1Name, String attribute2Name, String functionName) {
		String i1 = getInitials(attribute1Name);
		String i2 = getInitials(attribute2Name);
		String featureName = i1;
		if (!i1.equals(i2)) {
			featureName += "_" + i2;
		}
		featureName += "_" + functionName;
		return featureName;
	}
	public static List<Feature> recommendFeatures(Project project, Table table1,
			Table table2, List<AttributePair> attributePairs) {
		List<Feature> recommendedFeatures = new ArrayList<Feature>();
		for(AttributePair attributePair : attributePairs) {
			Attribute attribute1 = attributePair.getFirst();
			Attribute attribute2 = attributePair.getSecond();
			if (attribute1.getType() != attribute2.getType()) {
				throw new IllegalArgumentException("[EMS] Conflicting types for" +
						"attributes " + attribute1.getName() + " and " +
						attribute2.getName());
			}
			List<Function> recommendedFunctions = new ArrayList<Function>();
			AllTypes type;
			switch(attribute1.getType()) {
			case INTEGER:
			case LONG:
			case FLOAT:
				type = AllTypes.NUMERIC;
				break;
			case TEXT:
				type = getStringType(table1, table2, attribute1, attribute2);
				break;
			case BOOLEAN:
				type = AllTypes.CATEGORICAL;
				break;
			default:
				throw new IllegalArgumentException("[EMS] Invalid attribute type " +
						attribute1.getType() + " for attirbute " + attribute1.getName());
			}
			recommendedFunctions = FunctionService.getRecommendedFunctions(project,
					type);
			for (Function function : recommendedFunctions) {
				/*
				String featureName = table1.getName() + "_" + table2.getName() +
						"_" + attribute1.getName() + "_" +
						attribute2.getName() + "_" + function.getName();
				*/
				String featureName = getFeatureName(attribute1.getName(),
						attribute2.getName(), function.getName());
				Feature feature = new Feature(featureName, function,
						project.getName(), attribute1, attribute2);
				recommendedFeatures.add(feature);
			}
		}
		return recommendedFeatures;
	}

	public static void addFeatures(Project project, List<Feature> features,
			boolean saveToDisk) throws IOException {
		for(Feature feature : features) {
			// add feature to project
			project.addFeature(feature);
			if(saveToDisk) {
				// append to all.features file
				FeatureSaver.addFeature(project.getName(), feature);
			}
			else {
				// put this feature in the unsavedFeatures
				project.addUnsavedFeature(feature.getName());
			}
		}
		// update project
		ProjectDao.updateProject(project);
	}

	public static void editFeatureUsingGui(Project project, String featureName,
			String functionName, String table1Name, String attribute1Name,
			String table2Name, String attribute2Name, boolean saveToDisk) throws IOException {
		String projectName = project.getName();
		Feature feature = project.findFeatureByName(featureName);
		if (null == feature) {
			throw new IllegalArgumentException("Cannot find feature " +
					featureName + " in project " + projectName);
		}
		Function function = project.findFunctionByName(functionName);
		if (null == function) {
			throw new IllegalArgumentException("Cannot find function " +
					functionName + " in project " + projectName);
		}
		List<String> tableNames = project.getTableNames();
		if (!tableNames.contains(table1Name)) {
			throw new IllegalArgumentException("Cannot find table " +
					table1Name + " in project " + projectName);
		}
		if (!tableNames.contains(table2Name)) {
			throw new IllegalArgumentException("Cannot find table " +
					table2Name + " in project " + projectName);
		}

		Table table1 = TableDao.open(projectName, table1Name);
		if (null == table1) {
			throw new IllegalArgumentException("Cannot open table " +
					table1Name + " in project " + projectName);
		}

		Table table2 = TableDao.open(projectName, table2Name);
		if (null == table2) {
			throw new IllegalArgumentException("Cannot open table " +
					table2Name + " in project " + projectName);
		}

		Attribute attribute1 = table1.getAttributeByName(attribute1Name);
		if (null == attribute1) {
			throw new IllegalArgumentException("Cannot find attribute " +
					attribute1Name + " in table " + table1Name);
		}

		Attribute attribute2 = table2.getAttributeByName(attribute2Name);
		if (null == attribute2) {
			throw new IllegalArgumentException("Cannot find attribute " +
					attribute2Name + " in table " + table2Name);
		}

		feature.setFunction(function);
		feature.setAttribute1(attribute1);
		feature.setAttribute2(attribute2);

		if (saveToDisk) {
			// save the feature in all.features file
			FeatureSaver.saveFeature(projectName, feature, project);
			// remove the feature from unsaved features
			project.removeUnsavedFeature(featureName);
		}
		else {
			// put this feature in the unsavedFeatures
			project.addUnsavedFeature(featureName);
		}		
		// update project
		ProjectDao.updateProject(project);
	}

	public static void deleteFeature(String projectName, String featureName)
			throws IOException {
		Project project = ProjectDao.open(projectName);
		Feature feature = project.findFeatureByName(featureName);
		FeatureSaver.deleteFeature(projectName, feature, project);

		// update project
		project.removeUnsavedFeature(featureName);
		project.deleteFeature(feature);
		ProjectDao.updateProject(project);
	}

	public static Map<String, FeatureStatistics> computeFeatureCosts(String
			projectName, Table pairsTable, Table table1, Table table2, List<Feature> features) {
		List<Attribute> pairsAttributes = pairsTable.getAttributes();
		Attribute id1 = pairsAttributes.get(1);
		Attribute id2 = pairsAttributes.get(2);
		Map<String, FeatureStatistics> featureStatsMap = new HashMap<String, FeatureStatistics>();
		System.out.println("No. of tuple pairs: " + pairsTable.getSize());
		for (Feature f: features) {
			String featureName = f.getName();
			long count0 = 0, count1 = 0, countNull = 0;
			long[] bucketCounts = {0, 0, 0, 0, 0}; // (0-0.2], (0.2-0.4], ..., (0.8,1)
			long avgFeatureCostPerTuple = 0;
			long avgFeatureCostPerTupleExcludingNull = 0;
			for (Tuple pair : pairsTable.getAllTuples()) {
				Object id1Val = pair.getAttributeValue(id1);
				Object id2Val = pair.getAttributeValue(id2);
				Tuple tuple1 = table1.getTuple(id1Val);
				Tuple tuple2 = table2.getTuple(id2Val);
				long startTime = System.nanoTime();
				float featureVal = f.compute(tuple1, tuple2);
				long elapsedTime = System.nanoTime() - startTime;
				if (featureVal == -1.0) {
					countNull++;
				}
				else {
					if (featureVal == 0) {
						count0++;
					}
					else if (featureVal <= 0.2) {
						bucketCounts[0]++;
					}
					else if (featureVal <= 0.4) {
						bucketCounts[1]++;
					}
					else if (featureVal <= 0.6) {
						bucketCounts[2]++;
					}
					else if (featureVal <= 0.8) {
						bucketCounts[3]++;
					}
					else if (featureVal < 1) {
						bucketCounts[4]++;
					}
					else {
						count1++;
					}
					avgFeatureCostPerTupleExcludingNull += elapsedTime;
				}
				avgFeatureCostPerTuple += elapsedTime;
			}
			System.out.println("Feature Name: " + featureName + ", countNull: " + countNull);
			avgFeatureCostPerTuple /= pairsTable.getSize();
			if (pairsTable.getSize() != countNull)
				avgFeatureCostPerTupleExcludingNull /= (pairsTable.getSize() - countNull);
			FeatureStatistics featureStats = new FeatureStatistics(count0,
					count1, countNull, bucketCounts, avgFeatureCostPerTuple,
					avgFeatureCostPerTupleExcludingNull);
			featureStatsMap.put(featureName, featureStats);
		}
		return featureStatsMap;
	}
}
