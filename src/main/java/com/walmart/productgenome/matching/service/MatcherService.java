package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.Matcher;
import com.walmart.productgenome.matching.models.rules.Rule;
import com.walmart.productgenome.matching.models.savers.FeatureSaver;
import com.walmart.productgenome.matching.models.savers.MatcherSaver;

public class MatcherService {

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
}
