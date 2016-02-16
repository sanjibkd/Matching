package com.walmart.productgenome.matching.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.walmart.productgenome.matching.daos.ProjectDao;
import com.walmart.productgenome.matching.daos.TableDao;
import com.walmart.productgenome.matching.models.Constants;
import com.walmart.productgenome.matching.models.DefaultType;
import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Project;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.loaders.CSVLoader;
import com.walmart.productgenome.matching.models.loaders.JsonLoader;

public class TableService {

	public static Table[] split(Table labeledData, double trainPercent) {
		Table[] trainTestTables = new Table[2];
		String labeledDataName = labeledData.getName();
		String trainTableName = labeledDataName + "_train";
		String testTableName = labeledDataName + "_test";
		List<Object> trainIds = new ArrayList<Object>();
		List<Object> testIds = new ArrayList<Object>();
		Random random = new Random(0);
		double trainProb = trainPercent/100.0;
		for (Object id : labeledData.getAllIdsInOrder()) {
			if (random.nextDouble() < trainProb) {
				trainIds.add(id);
			}
			else {
				testIds.add(id);
			}
		}
		Table trainTable = new Table(labeledData, trainTableName, trainIds);
		Table testTable = new Table(labeledData, testTableName, testIds);
		trainTestTables[0] = trainTable;
		trainTestTables[1] = testTable;
		return trainTestTables;
	}

	public static Table[] splitTable(Table table,
			String split1Name, String split2Name, double splitRatio) {
		Table[] splitTables = new Table[2];
		List<Object> ids1 = new ArrayList<Object>();
		List<Object> ids2 = new ArrayList<Object>();
		Random random = new Random();
		for (Object id : table.getAllIdsInOrder()) {
			if (random.nextDouble() < splitRatio) {
				ids1.add(id);
			}
			else {
				ids2.add(id);
			}
		}
		splitTables[0] = new Table(table, split1Name, ids1);
		splitTables[1] = new Table(table, split2Name, ids2);
		return splitTables;
	}
	
	public static Table importFromJson(String projectName,
			String tableName, String jsonFilePath) throws IOException {
		return JsonLoader.loadTableFromJson(projectName,
				tableName, jsonFilePath);
	}
	
	public static Table difference(Table table1, Table table2) {
		Table diffTable = new Table(table1);
		diffTable.setName(table1.getName() + "_minus_" + table2.getName());
		List<Attribute> table2Attributes = table2.getAttributes();
		List<Attribute> table1Attributes = table1.getAttributes();
		for (Tuple table2Tuple : table2.getAllTuples()) {
			Object id1 = table2Tuple.getAttributeValue(table2Attributes.get(1));
			Object id2 = table2Tuple.getAttributeValue(table2Attributes.get(2));
			for (Tuple table1Tuple : table1.getAllTuples()) {
				if (id1.equals(table1Tuple.getAttributeValue(table1Attributes.get(1))) &&
						id2.equals(table1Tuple.getAttributeValue(table1Attributes.get(2)))) {
					Object id = table1Tuple.getAttributeValue(table1.getIdAttribute());
					diffTable.removeTuple(id);
				}
			}
		}
		return diffTable;
	}
	
	public static void main(String[] args) throws IOException {
		//Project project = ProjectDao.open("haojun_wentao");
		Table table1 = TableDao.open("video_games", "candset");
		Table table2 = TableDao.open("video_games", "gold");
		Table diffTable = TableService.difference(table1, table2);
		TableDao.save(diffTable, new HashSet<DefaultType>(), true);
	}
	
}
