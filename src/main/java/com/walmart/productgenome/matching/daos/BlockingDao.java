package com.walmart.productgenome.matching.daos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.walmart.productgenome.matching.models.data.Attribute;
import com.walmart.productgenome.matching.models.data.Table;
import com.walmart.productgenome.matching.models.data.Tuple;
import com.walmart.productgenome.matching.utils.JSONUtils;

public class BlockingDao {

	public static final Attribute PAIR_ID_ATTRIBUTE = new Attribute("pairId",
			Attribute.Type.INTEGER);

	public static Table block(String projectName,String table1Name, String table2Name,
			String attr1Name, String attr2Name, String candsetName,
			String[] table1AttributeNames, String[] table2AttributeNames)
					throws IOException {
		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);

		Attribute attribute1 = table1.getAttributeByName(attr1Name);
		Attribute attribute2 = table2.getAttributeByName(attr2Name);

		if (attribute1.getType() != attribute2.getType()) {
			throw new InputMismatchException("Blocking attributes must be of the same type");
		}

		Attribute idAttribute1 = table1.getIdAttribute();

		// index all the values for attribute1 from table1
		Map<Object, List<Object>> index = new HashMap<Object, List<Object>>();
		for (Tuple tuple : table1.getAllTuples()) {
			Object value = tuple.getAttributeValue(attribute1);
			Object tupleId = tuple.getAttributeValue(idAttribute1);
			if (null != value && value instanceof String && !((String)value).isEmpty()) {
				if (index.containsKey(value)) {
					index.get(value).add(tupleId);
				}
				else {
					List<Object> tupleIdList = new ArrayList<Object>();
					tupleIdList.add(tupleId);
					index.put(value, tupleIdList);
				}
			}
		}

		String id1Name = table1Name + "." + idAttribute1.getName();
		Attribute id1 = new Attribute(id1Name, idAttribute1.getType());

		Attribute idAttribute2 = table2.getIdAttribute();
		String id2Name = table2Name + "." + idAttribute2.getName();
		Attribute id2 = new Attribute(id2Name, idAttribute2.getType());

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(PAIR_ID_ATTRIBUTE);
		attributes.add(id1);
		attributes.add(id2);

		// add table 1 attributes
		if (null != table1AttributeNames) {
			for (String attributeName: table1AttributeNames) {
				Attribute attribute = table1.getAttributeByName(attributeName);
				attributes.add(new Attribute(table1Name + "." + attributeName, attribute.getType()));
			}
		}

		// add table 2 attributes
		if (null != table2AttributeNames) {
			for (String attributeName : table2AttributeNames) {
				Attribute attribute = table2.getAttributeByName(attributeName);
				attributes.add(new Attribute(table2Name + "." + attributeName, attribute.getType()));
			}
		}

		Table candset = new Table(candsetName, PAIR_ID_ATTRIBUTE, attributes, projectName);

		Map<Attribute,Object> data = new HashMap<Attribute,Object>();
		int pairId = 1;
		// iterate through all the values for attribute2 in table2
		for (Tuple tuple : table2.getAllTuplesInOrder()){
			Object value = tuple.getAttributeValue(attribute2);
			Object tupleId = tuple.getAttributeValue(idAttribute2);
			if (index.containsKey(value)) {
				List<Object> id1List = index.get(value);
				for (Object id1Val: id1List) {
					data.put(PAIR_ID_ATTRIBUTE, pairId);
					data.put(id1, id1Val);
					data.put(id2, tupleId);

					// put table 1 attribute values
					int i = 3;
					Tuple table1Tuple = table1.getTuple(id1Val);
					if (null != table1AttributeNames) {
						for (String attributeName : table1AttributeNames) {
							Attribute attribute = table1.getAttributeByName(attributeName);
							Object attributeValue = table1Tuple.getAttributeValue(attribute);
							data.put(attributes.get(i), attributeValue);
							i++;
						}
					}

					// put table 2 attribute values
					if (null != table2AttributeNames) {
						for (String attributeName : table2AttributeNames) {
							Attribute attribute = table2.getAttributeByName(attributeName);
							Object attributeValue = tuple.getAttributeValue(attribute);
							data.put(attributes.get(i), attributeValue);
							i++;
						}
					}

					Tuple pair = new Tuple(data);
					candset.addTuple(pair);
					//System.out.println("Found candidate pair: " + pairId + ", " +
					//						id1Val + ", " + tupleId);
					pairId++;
					data.clear();
				}
			}
		}
		//System.out.println(JSONUtils.getTableJSON(candset));
		return candset;
	}

	public static Table block(String projectName, String table1Name, String table2Name,
			String[] blockingAttr1Names, String[] blockingAttr2Names, String candsetName,
			String[] table1AttributeNames, String[] table2AttributeNames)
					throws IOException {
		int l = blockingAttr1Names.length;
		if (l != blockingAttr2Names.length) {
			throw new InputMismatchException("Unequal number of blocking "
					+ "attributes for the two tables");
		}

		Table table1 = TableDao.open(projectName, table1Name);
		Table table2 = TableDao.open(projectName, table2Name);

		// blocking attributes
		Attribute[] table1BlockingAttributes = new Attribute[l];
		Attribute[] table2BlockingAttributes = new Attribute[l];

		// indexes
		Map<String, Map<Object, Set<Object>>> table1Indexes = new HashMap<String, Map<Object, Set<Object>>>();

		Attribute idAttribute1 = table1.getIdAttribute();
		for (int i = 0; i < l; i++) {
			String table1BlockingAttributeName = blockingAttr1Names[i];
			Attribute attribute1 = table1.getAttributeByName(table1BlockingAttributeName);
			String table2BlockingAttributeName = blockingAttr2Names[i];
			Attribute attribute2 = table2.getAttributeByName(table2BlockingAttributeName);
			if (attribute1.getType() != attribute2.getType()) {
				throw new InputMismatchException("Blocking attributes must be of "
						+ "the same type. Violating attributes: "
						+ table1BlockingAttributeName + " and "
						+ table2BlockingAttributeName);
			}
			table1BlockingAttributes[i] = attribute1;
			table2BlockingAttributes[i] = attribute2;

			if (table1Indexes.containsKey(table1BlockingAttributeName)) {
				continue;
			}

			// index all the values for attribute1 from table1
			Map<Object, Set<Object>> index = new HashMap<Object, Set<Object>>();
			for (Tuple tuple : table1.getAllTuples()) {
				Object value = tuple.getAttributeValue(attribute1);
				Object tupleId = tuple.getAttributeValue(idAttribute1);
				if (null != value && value instanceof String && !((String)value).isEmpty()) {
					if (index.containsKey(value)) {
						index.get(value).add(tupleId);
					}
					else {
						Set<Object> tupleIdSet = new HashSet<Object>();
						tupleIdSet.add(tupleId);
						index.put(value, tupleIdSet);
					}
				}
			}
			System.out.println("Created index of size " + index.size() + " for attribute " + table1BlockingAttributeName);
			table1Indexes.put(table1BlockingAttributeName, index);
		}

		String id1Name = table1Name + "." + idAttribute1.getName();
		Attribute id1 = new Attribute(id1Name, idAttribute1.getType());

		Attribute idAttribute2 = table2.getIdAttribute();
		String id2Name = table2Name + "." + idAttribute2.getName();
		Attribute id2 = new Attribute(id2Name, idAttribute2.getType());

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(PAIR_ID_ATTRIBUTE);
		attributes.add(id1);
		attributes.add(id2);

		// add table 1 attributes
		if (null != table1AttributeNames) {
			for (String attributeName: table1AttributeNames) {
				Attribute attribute = table1.getAttributeByName(attributeName);
				attributes.add(new Attribute(table1Name + "." + attributeName, attribute.getType()));
			}
		}

		// add table 2 attributes
		if (null != table2AttributeNames) {
			for (String attributeName : table2AttributeNames) {
				Attribute attribute = table2.getAttributeByName(attributeName);
				attributes.add(new Attribute(table2Name + "." + attributeName, attribute.getType()));
			}
		}

		Table candset = new Table(candsetName, PAIR_ID_ATTRIBUTE, attributes, projectName);

		Map<Attribute, Object> data = new HashMap<Attribute, Object>();
		int pairId = 1;

		for (Tuple tuple : table2.getAllTuplesInOrder()) {
			Set<Object> tupleIdSet = new HashSet<Object>();
			Object tupleId = tuple.getAttributeValue(idAttribute2);
			for (int i = 0; i < l; i++) {
				Attribute attribute2 = table2BlockingAttributes[i];
				Object value = tuple.getAttributeValue(attribute2);
				String attribute1Name = table1BlockingAttributes[i].getName();
				Map<Object, Set<Object>> index = table1Indexes.get(attribute1Name);
				if (index.containsKey(value)) {
					Set<Object> id1Set = index.get(value);
					System.out.println("Hits from index " + i + ": " + id1Set.size());
					if (i == 0) {
						tupleIdSet.addAll(id1Set);
					}
					else {
						tupleIdSet.retainAll(id1Set);
					}
					System.out.println("Total Hits after probing index " + i + ": " + tupleIdSet.size());
				}
				else {
					tupleIdSet.clear();
					break;
				}
			}
			for (Object id1Val: tupleIdSet) {
				data.put(PAIR_ID_ATTRIBUTE, pairId);
				data.put(id1, id1Val);
				data.put(id2, tupleId);

				// put table 1 attribute values
				int i = 3;
				Tuple table1Tuple = table1.getTuple(id1Val);
				if (null != table1AttributeNames) {
					for (String attributeName : table1AttributeNames) {
						Attribute attribute = table1.getAttributeByName(attributeName);
						Object attributeValue = table1Tuple.getAttributeValue(attribute);
						data.put(attributes.get(i), attributeValue);
						i++;
					}
				}

				// put table 2 attribute values
				if (null != table2AttributeNames) {
					for (String attributeName : table2AttributeNames) {
						Attribute attribute = table2.getAttributeByName(attributeName);
						Object attributeValue = tuple.getAttributeValue(attribute);
						data.put(attributes.get(i), attributeValue);
						i++;
					}
				}

				Tuple pair = new Tuple(data);
				candset.addTuple(pair);
				//System.out.println("Found candidate pair: " + pairId + ", " +
				//						id1Val + ", " + tupleId);
				pairId++;
				data.clear();
			}

		}
		//System.out.println(JSONUtils.getTableJSON(candset));
		return candset;
	}
}
