package com.walmart.productgenome.matching.daos;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.walmart.productgenome.matching.models.RelationalOperator;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.rules.Term;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.models.savers.FeatureSaver;
import com.walmart.productgenome.matching.models.savers.FunctionSaver;
import com.walmart.productgenome.matching.models.savers.MatcherSaver;
import com.walmart.productgenome.matching.models.savers.RuleSaver;
import com.walmart.productgenome.matching.models.savers.TableSaver;
import com.walmart.productgenome.matching.utils.CSVUtils;
import com.walmart.productgenome.matching.utils.ParsingUtils;

public class RuleDao {

	public static List<Feature> importFeaturesFromCSVWithHeader(Project project,
			Table table1, Table table2, String csvFeatureFilePath) throws IOException{
		return CSVUtils.readFeatures(project, table1, table2,
				csvFeatureFilePath);
	}

	public static List<Rule> importRulesFromCSVWithHeader(Project project,
			String table1Name, String table2Name, String csvRuleFilePath) throws IOException{
		return CSVUtils.readRules(project, table1Name, table2Name, csvRuleFilePath);
	}

	public static List<Matcher> importMatchersFromCSVWithHeader(Project project,
			String table1Name, String table2Name, String csvMatcherFilePath) throws IOException{
		return CSVUtils.readMatchers(project, table1Name, table2Name, csvMatcherFilePath);
	}

	public static Matcher importMatcherFromCSVWithHeader(String programName,
			Project project, Table table1, Table table2,
			String programFilePath) throws IOException{
		return ParsingUtils.readMatcher(programName, project, table1,
				table2, programFilePath);
	}

	public static void addFunction(String projectName, String functionName,
			String functionDesc, String functionClassName, boolean saveToDisk)
					throws ClassNotFoundException, SecurityException, NoSuchMethodException,
					IllegalArgumentException, InstantiationException, IllegalAccessException,
					InvocationTargetException, IOException {

		Class<?> functionClass = Class.forName(functionClassName);
		Constructor<?> constructor = functionClass.getConstructor(String.class, String.class);
		Function function = (Function) constructor.newInstance(functionName, functionDesc);

		// add function to project
		Project project = ProjectDao.open(projectName);
		project.addFunction(function);

		if(saveToDisk) {
			// append to all.functions file
			FunctionSaver.addFunction(projectName, function);
		}
		else {
			// put this function in the unsavedFunctions
			project.addUnsavedFunction(functionName);
		}

		// update project
		ProjectDao.updateProject(project);
	}

	public static void saveFunction(String projectName, String functionName) throws SecurityException, IllegalArgumentException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Project project = ProjectDao.open(projectName);
		if (!project.isUnsavedFunction(functionName)){
			// function is already saved
			return;
		}

		// function is in memory but not saved
		Function function = project.findFunctionByName(functionName);
		FunctionSaver.saveFunction(projectName, function);

		// update project
		project.removeUnsavedFunction(functionName);
		ProjectDao.updateProject(project);
	}

	public static void saveAllFunctions(String projectName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if(project.getUnsavedFunctions().isEmpty()) {
			// no unsaved functions
			return;
		}
		FunctionSaver.saveAllFunctions(project);

		// update project
		project.clearUnsavedFunctions();
		ProjectDao.updateProject(project);
	}

	public static void addFeature(String projectName, String featureName, String functionName,
			String table1Name, String table2Name, String attr1Name, String attr2Name, boolean saveToDisk) throws IOException {

		Project project = ProjectDao.open(projectName);
		Function function = project.findFunctionByName(functionName);
		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);
		Attribute attribute1 = table1.getAttributeByName(attr1Name);
		Attribute attribute2 = table2.getAttributeByName(attr2Name);
		Feature feature = new Feature(featureName, function, projectName, attribute1, attribute2);

		// add feature to project
		project.addFeature(feature);
		if(saveToDisk) {
			// append to all.features file
			FeatureSaver.addFeature(projectName, feature);
		}
		else {
			// put this feature in the unsavedFeatures
			project.addUnsavedFeature(featureName);
		}

		// update project
		ProjectDao.updateProject(project);
	}

	public static void saveFeature(String projectName, String featureName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if (!project.isUnsavedFeature(featureName)){
			// feature is already saved
			return;
		}

		// feature is in memory but not saved
		Feature feature = project.findFeatureByName(featureName);
		FeatureSaver.saveFeature(projectName, feature, project);

		// update project
		project.removeUnsavedFeature(featureName);
		ProjectDao.updateProject(project);
	}

	public static void saveAllFeatures(String projectName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if(project.getUnsavedFeatures().isEmpty()) {
			// no unsaved features
			return;
		}
		FeatureSaver.saveAllFeatures(project);

		// update project
		project.clearUnsavedFeatures();
		ProjectDao.updateProject(project);
	}

	private static Term getTerm(Project project, String featureName, String opName, String val){
		Term term = null;
		if(null != val && !val.isEmpty()){
			float value = Float.parseFloat(val);
			Feature feature = project.findFeatureByName(featureName);
			RelationalOperator op = RelationalOperator.valueOfFromName(opName);
			term = new Term(feature, op, value);
		}
		return term;
	}

	public static void addRule(String projectName, String ruleName,
			List<String> featureNames,
			List<String> operators, List<String> values, boolean saveToDisk) throws IOException {

		Project project = ProjectDao.open(projectName);
		List<Term> terms = new ArrayList<Term>();

		for(int i=0; i<featureNames.size(); i++) {
			Term term = getTerm(project, featureNames.get(i), operators.get(i), values.get(i));
			if(null != term) {
				terms.add(term);
			}
		}

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

	public static void saveRule(String projectName, String ruleName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if (!project.isUnsavedRule(ruleName)){
			// rule is already saved
			return;
		}

		// rule is in memory but not saved
		Rule rule = project.findRuleByName(ruleName);
		RuleSaver.saveRule(projectName, rule, project);

		// update project
		project.removeUnsavedRule(ruleName);
		ProjectDao.updateProject(project);
	}

	public static void saveAllRules(String projectName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if(project.getUnsavedRules().isEmpty()) {
			// no unsaved rules
			return;
		}
		RuleSaver.saveAllRules(project);

		// update project
		project.clearUnsavedRules();
		ProjectDao.updateProject(project);
	}

	public static void editFeature(String projectName, String featureName,
			String featureString, boolean saveToDisk) throws IOException {

		System.out.println("featureName: " + featureName + ", featureString: " + featureString);
		Project project = ProjectDao.open(projectName);
		Feature feature = ParsingUtils.parseFeatureFromDisplayString(project, featureName, featureString);
		System.out.println("[RuleDao:editFeature] Feature: " + feature);

		if(saveToDisk) {
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

	public static void editRule(String projectName, String ruleName,
			String ruleString, boolean saveToDisk) throws IOException {

		System.out.println("ruleName: " + ruleName + ", ruleString: " + ruleString);
		Project project = ProjectDao.open(projectName);
		Rule rule = project.findRuleByName(ruleName);
		List<Term> terms = ParsingUtils.parseRuleFromDisplayString(project, ruleString);
		rule.setTerms(terms);
		System.out.println("[RuleDao:editRule] Rule: " + rule);

		if(saveToDisk) {
			// save the rule in all.rules file
			RuleSaver.saveRule(projectName, rule, project);
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

	public static void editMatcher(String projectName, String matcherName,
			String matcherString, boolean saveToDisk) throws IOException {

		System.out.println("matcherName: " + matcherName + ", matcherString: " + matcherString);
		Project project = ProjectDao.open(projectName);
		Matcher matcher = ParsingUtils.parseMatcherFromDisplayString(project,
				matcherName, matcherString, saveToDisk);
		System.out.println("[RuleDao:editMatcher] Matcher: " + matcher);

		if(saveToDisk) {
			// save the matcher in all.matchers file
			MatcherSaver.saveMatcher(projectName, matcher, project);
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

	private static Rule getRule(Project project, String ruleName){
		Rule rule = null;
		if(null != ruleName && !ruleName.isEmpty()){
			rule = project.findRuleByName(ruleName);
		}
		return rule;
	}

	public static void saveMatcher(String projectName, String matcherName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if (!project.isUnsavedMatcher(matcherName)){
			// matcher is already saved
			return;
		}

		// matcher is in memory but not saved
		Matcher matcher = project.findMatcherByName(matcherName);
		MatcherSaver.saveMatcher(projectName, matcher, project);

		// update project
		project.removeUnsavedMatcher(matcherName);
		ProjectDao.updateProject(project);
	}

	public static void saveAllMatchers(String projectName) throws IOException {
		Project project = ProjectDao.open(projectName);
		if(project.getUnsavedMatchers().isEmpty()) {
			// no unsaved matchers
			return;
		}
		MatcherSaver.saveAllMatchers(project);

		// update project
		project.clearUnsavedMatchers();
		ProjectDao.updateProject(project);
	}

	public static List<Function> importFunctions(String projectName, String csvFunctionFilePath, boolean saveToDisk) throws IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Project project = ProjectDao.open(projectName);
		return importFunctions(project, csvFunctionFilePath, saveToDisk);
	}

	public static List<Function> importFunctions(Project project, String csvFunctionFilePath, boolean saveToDisk) throws IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		List<Function> functions = CSVUtils.loadFunctions(csvFunctionFilePath);
		
		// add functions to project
		project.addFunctions(functions);

		if(saveToDisk) {
			// append to all.functions file
			FunctionSaver.addFunctions(project.getName(), functions);
		}
		else {
			// put these functions in the unsavedFunctions
			for(Function function : functions) {
				project.addUnsavedFunction(function.getName());
			}
		}
		
		// update project
		ProjectDao.updateProject(project);
		return functions;
	}
	
	public static void save(String projectName, List<Feature> features) throws IOException {
		// update project
		Project project = ProjectDao.open(projectName);
		project.addFeatures(features);
		ProjectDao.save(project);
	}

	public static void saveRules(String projectName, List<Rule> rules) throws IOException {
		// update project
		Project project = ProjectDao.open(projectName);
		project.addRules(rules);
		ProjectDao.save(project);
	}

	public static void saveMatchers(String projectName, List<Matcher> matchers) throws IOException {
		// update project
		Project project = ProjectDao.open(projectName);
		project.addMatchers(matchers);
		ProjectDao.save(project);
	}

	public static void save(String projectName, Matcher program) throws IOException {
		// update project
		Project project = ProjectDao.open(projectName);
		project.addMatcher(program);
		ProjectDao.save(project);
	}

	public static String getRuleString(String projectName, String ruleName) {
		Project project = ProjectDao.open(projectName);
		Rule rule = project.findRuleByName(ruleName);
		String ruleString = "";
		if (null != rule) {
			ruleString = rule.getDisplayString();
		}
		return ruleString;
	}

	public static String getMatcherString(String projectName, String matcherName) {
		Project project = ProjectDao.open(projectName);
		Matcher matcher = project.findMatcherByName(matcherName);
		return matcher.getDisplayString();
	}

	public static String getFeatureString(String projectName, String featureName) {
		Project project = ProjectDao.open(projectName);
		Feature feature = project.findFeatureByName(featureName);
		return feature.getDisplayString();
	}

}
