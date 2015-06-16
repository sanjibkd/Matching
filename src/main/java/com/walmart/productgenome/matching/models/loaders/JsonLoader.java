package com.walmart.productgenome.matching.models.loaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.utils.JSONUtils;

public class JsonLoader {

	public static Table loadTableFromJson(String projectName, String tableName, String jsonFilePath) throws IOException{

		BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
		String line;
		// get the JSON data
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			if(line.trim().isEmpty()){
				break;
			}
			sb.append(line);
		}
		br.close();
		String tableJson = sb.toString();
		// System.out.println(tableJson);
		JsonReader reader = Json.createReader(new StringReader(tableJson));
		JsonObject obj = reader.readObject();
		JsonObject tableObj = obj.getJsonObject(JSONUtils.TABLE);

		JsonObject idAttribObj = tableObj.getJsonObject(JSONUtils.ID_ATTRIB);
		Attribute idAttrib = null;
		if(null != idAttribObj){
			idAttrib = JSONUtils.getAttributeFromJSON(idAttribObj);
		}

		JsonArray attribs = tableObj.getJsonArray(JSONUtils.ATTRIBUTES);
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (JsonObject attr : attribs.getValuesAs(JsonObject.class)) {
			attributes.add(JSONUtils.getAttributeFromJSON(attr));
		}

		Table table = new Table(tableName,idAttrib, attributes, projectName);

		JsonArray tuples_arr = tableObj.getJsonArray(JSONUtils.TUPLES);
		List<Tuple> tuples = new ArrayList<Tuple>();
		for (JsonObject tuple : tuples_arr.getValuesAs(JsonObject.class)) {
			tuples.add(JSONUtils.getTupleFromJSON(tuple, attributes));
		}

		table.addAllTuples(tuples);
		return table;
	}

}
