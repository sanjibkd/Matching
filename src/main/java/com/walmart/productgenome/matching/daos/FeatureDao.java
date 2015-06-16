package com.walmart.productgenome.matching.daos;

import java.io.IOException;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.rules.Feature;
import com.walmart.productgenome.matching.models.rules.functions.Function;
import com.walmart.productgenome.matching.models.savers.FeatureSaver;

public class FeatureDao {

	public static void addFeatures(String projectName, String featureName, String functionName,
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
}
