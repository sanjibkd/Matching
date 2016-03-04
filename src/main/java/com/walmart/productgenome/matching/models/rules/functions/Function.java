package com.walmart.productgenome.matching.models.rules.functions;

import java.util.Set;

import org.jsoup.Jsoup;

import com.google.common.base.Objects;
import com.walmart.productgenome.matching.models.data.Attribute.AllTypes;

public abstract class Function {

	private String name;
	private String description;

	public Function(String name, String description){
		this.name = name;
		this.description = description;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public String getReturnType(){
		return "Float";
	}

	public String getArguments(){
		return "String, String";
	}

	public abstract ArgType getArgType();

	public enum ArgType
	{
		STRING,
		NUMERIC
	}

	public float handleMissingValue(String arg1, String arg2) {
		if (arg1 == null || arg2 == null
				|| arg1.toLowerCase().equals("null")
				|| arg2.toLowerCase().equals("null")
				|| arg1.isEmpty() || arg2.isEmpty()) {
			return -1.0f;
		}
		return 0.0f;
	}

	public String removeNonAsciiCharacters(String s) {
		return s.replaceAll("[^\\x00-\\x7F]", "");
	}
	
	public String html2Text(String html) {
		return Jsoup.parse(html).text();
	}
	
	public String removeEnclosingBrackets(String s) {
		String prefix = "[\"";
		String suffix = "\"]";
		int l = s.length();
		if (s.startsWith(prefix) && s.endsWith(suffix)) {
			return s.substring(2, l - 2);
		}
		return s;
	}
	
	public abstract Set<AllTypes> getAllRecommendedTypes();

	public abstract Object compute(String[] args) throws IllegalArgumentException;

	public abstract String getSignature();

	@Override
	public int hashCode(){
		return Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof Function) {
			Function that = (Function) object;
			return Objects.equal(this.name, that.name);
		}
		return false;
	}

}
