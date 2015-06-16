package com.walmart.productgenome.matching.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.models.savers.FunctionSaver;

public class FunctionService {

	public static List<Function> getRecommendedFunctions(Project project, AllTypes type) {
		List<Function> functions = new ArrayList<Function>();
		List<Function> allFunctions = project.getFunctions();
		for (Function function : allFunctions) {
			if(function.getAllRecommendedTypes().contains(type)) {
				functions.add(function);
			}
		}
		return functions;
	}
	
	public static void deleteFunction(String projectName, String functionName)
			throws IOException, SecurityException, IllegalArgumentException,
			ClassNotFoundException, NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Project project = ProjectDao.open(projectName);
		Function function = project.findFunctionByName(functionName);
		FunctionSaver.deleteFunction(projectName, function);
			
		// update project
		project.removeUnsavedFunction(functionName);
		project.deleteFunction(function);
		ProjectDao.updateProject(project);
	}
}
