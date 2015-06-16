package com.walmart.productgenome.matching.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.walmart.productgenome.matching.daos.RuleDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.Constants;
import com.walmart.productgenome.matching.models.RelationalOperator;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.rules.Term;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.models.savers.RuleSaver;

public class ParsingUtils {

	public static int SYS_RULE_NUM = 1;
	public static final String SYS_RULE = "SYS_RULE_";
	
	public static Matcher readMatcher(String programName, Project project,
			Table table1, Table table2, String programFilePath) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(programFilePath));
		String line = br.readLine(); // skip the header
		List<Rule> rules = new ArrayList<Rule>();
		while ((line = br.readLine()) != null) {
			// process the line to get a Rule
			String ruleName = line.trim();
			System.out.println("Rule name: " + ruleName);
			Rule rule = project.findRuleByName(ruleName);
			System.out.println("Rule: " + rule);
			rules.add(rule);
		}
		br.close();
		System.out.println("Rules' size: " + rules.size());
		return new Matcher(programName, project.getName(), rules);
	}
	
	public static List<Term> parseRule(Project p, String ruleString){
    	List<Term> terms = new ArrayList<Term>();
    	String[] termValues = ruleString.split(Constants.TERM_SEPARATOR);
    	for(String t : termValues){
    		System.out.println("[ParsingUtils] Term: " + t);
    		String[] values = t.split(",");
    		String featureName = values[0];
    		String comparison = values[1];
    		String rvalue = values[2];
    		Feature feature1 = p.findFeatureByName(featureName);
    		RelationalOperator relop = RelationalOperator.valueOf(comparison);
    		float value = Float.NaN;
    		Feature feature2 = null;
    		try{
    			value = Float.parseFloat(rvalue);
    		}
    		catch(NumberFormatException nfe){
    			feature2 = p.findFeatureByName(rvalue);
    		}
    		Term term;
    		if(feature2 != null){
    			term = new Term(feature1,relop,feature2);
    		}
    		else{
    			term = new Term(feature1,relop,value);
    		}
    		terms.add(term);
    	}
    	return terms;
    }
	
	public static Feature parseFeatureFromDisplayString(Project p, String featureName, 
			String featureString) throws IOException{
		Feature feature = p.findFeatureByName(featureName);
		String[] values = featureString.split(",");
		String functionName = values[0];
		if(!feature.getFunction().getName().equals(functionName)){
			Function function = p.findFunctionByName(functionName);
			feature.setFunction(function);
		}
		String attr1Name = values[1];
		String attr1Type = values[2];
		String attr2Name = values[3];
		String attr2Type = values[4];
    	if (!feature.getAttribute1().getName().equals(attr1Name)) {
    		Attribute attribute1 = new Attribute(attr1Name, Attribute.Type.valueOf(attr1Type));
    		feature.setAttribute1(attribute1);
    	}
    	
    	if (!feature.getAttribute2().getName().equals(attr2Name)) {
    		Attribute attribute2 = new Attribute(attr2Name, Attribute.Type.valueOf(attr2Type));
    		feature.setAttribute2(attribute2);
    	}
    	
    	return feature;
    }
	
	public static List<Term> parseRuleFromDisplayString(Project p, 
			String ruleString) {
		
    	List<Term> terms = new ArrayList<Term>();
    	String[] termValues = ruleString.split(Constants.TERM_SEPARATOR);
    	for (String t : termValues) {
    		//System.out.println("[ParsingUtils] Term: " + t);
    		String[] values = t.split(" ");
    		String featureName = values[0];
    		String comparison = values[1];
    		String rvalue = values[2];
    		Feature feature1 = p.findFeatureByName(featureName);
    		RelationalOperator relop = RelationalOperator.valueOfFromName(comparison);
    		float value = Float.NaN;
    		Feature feature2 = null;
    		try{
    			value = Float.parseFloat(rvalue);
    		}
    		catch(NumberFormatException nfe){
    			feature2 = p.findFeatureByName(rvalue);
    		}
    		Term term;
    		if(feature2 != null){
    			term = new Term(feature1,relop,feature2);
    		}
    		else{
    			term = new Term(feature1,relop,value);
    		}
    		terms.add(term);
    	}
    	return terms;
    }
	
	public static List<Rule> parseMatcherFromShortDisplayString(Project p, 
			String matcherString){
    	List<Rule> rules = new ArrayList<Rule>();
    	String[] ruleNames = matcherString.split(Constants.RULE_SEPARATOR);
    	for(String ruleName : ruleNames){
    		Rule rule = p.findRuleByName(ruleName);
    		rules.add(rule);
    	}
    	return rules;
    }
	
	public static Matcher parseMatcherFromDisplayString(Project p, String matcherName, 
			String matcherString, boolean saveToDisk) throws IOException{
		Matcher matcher = p.findMatcherByName(matcherName);
		List<Rule> rules = matcher.getRules();
		List<Rule> newRules = new ArrayList<Rule>();
		Map<String, Rule> ruleMap = new HashMap<String, Rule>();
		for(Rule r : rules){
			ruleMap.put(r.getDisplayString(),r);
		}
    	String[] ruleStrings = matcherString.split(Constants.RULE_SEPARATOR);
    	for(String ruleString : ruleStrings){
    		Rule rule = ruleMap.get(ruleString);
    		if(rule == null){
    			// new/modified rule - treat it as a new rule
    			String ruleName = SYS_RULE + SYS_RULE_NUM;
    			SYS_RULE_NUM++;
    			List<Term> terms = parseRuleFromDisplayString(p, ruleString);
    			rule = new Rule(ruleName, p.getName(), terms);
    			// add this new rule to the project
    			p.addRule(rule);
    			if(saveToDisk) {
    				// save the rule to disk
    				RuleSaver.saveRule(p.getName(), rule, p);
    				p.removeUnsavedRule(ruleName);
    			}
    			else {
    				p.addUnsavedRule(ruleName);
    			}
    		}
    		newRules.add(rule);
    	}
    	matcher.setRules(newRules);
    	return matcher;
    }
	
	public static void main(String[] args) throws IOException{
		/*
		Matcher matcher = readMatcher("test","lskjfdkfjd","ljkkkkkfg","vcvxv",
				"/home/sanjib/work/MatchingWebApp/data/books/matcher.csv");
		System.out.println(matcher);
		*/
	}
}
