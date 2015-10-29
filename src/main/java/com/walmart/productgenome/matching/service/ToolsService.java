package com.walmart.productgenome.matching.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.models.data.Attribute.Type;
import com.walmart.productgenome.matching.models.rules.Feature;

public class ToolsService {
	
	private static void outputMap(BufferedWriter bw, Map<Attribute, Object> data, List<Attribute> attributes) throws IOException {
		for (Attribute a: attributes) {
			if ("id".equalsIgnoreCase(a.getName())
					|| "Product_Short_Description".equalsIgnoreCase(a.getName())) {
				continue;
			}
			Object v = data.get(a);
			if (null != v) {
				bw.write(a.getName());
				bw.write(": ");
				bw.write(v.toString());
				bw.newLine();
				bw.newLine();
			}
		}
	}
	
	public static int outputPairs(String projectName,
			Table pairsTable, Table table1, Table table2, String outputFileName, String pairIdsList) throws IOException {
		Attribute idAttribute = pairsTable.getIdAttribute();
		List<Attribute> pairsAttributes = pairsTable.getAttributes();
		Attribute id1 = pairsAttributes.get(1);
		Attribute id2 = pairsAttributes.get(2);
		String[] vals = pairIdsList.split(",");
		Set<String> pairIdsToOutput = new HashSet<String>();
		for (String v: vals) {
			pairIdsToOutput.add(v);
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
		int n = 0;
		List<Attribute> attributes = table1.getAttributes();
		for (Tuple pair : pairsTable.getAllTuples()) {
			Object pairId = pair.getAttributeValue(idAttribute);
			if (!pairIdsToOutput.contains(String.valueOf(pairId))) {
				continue;
			}
			Object id1Val = pair.getAttributeValue(id1);
			Object id2Val = pair.getAttributeValue(id2);
			Tuple tuple1 = table1.getTuple(id1Val);
			Tuple tuple2 = table2.getTuple(id2Val);
			try {
				bw.write("********************* Pair #" + pairId + " ************************");
				bw.newLine();
				bw.newLine();
				bw.write("Walmart product (id: " + id1Val + ")");
				bw.newLine();
				outputMap(bw, tuple1.getData(), attributes);
				bw.write("Vendor product (id: " + id2Val + ")");
				bw.newLine();
				outputMap(bw, tuple2.getData(), attributes);
				n++;
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		bw.close();
		return n;
	}
}
