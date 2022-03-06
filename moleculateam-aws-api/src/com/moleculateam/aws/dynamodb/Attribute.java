package com.moleculateam.aws.dynamodb;

/**
 * 
 * @author Ernesto López
 * 
 * Attribute class is used to represent the name, value and type of an attributo to: add, delete or fetch from GeneralDB
 *
 */
public class Attribute {
	
	public String name;
	public enum TYPE { CHAR, INT, JSON, SHORT };  
	public Object value;
	public TYPE Type;
	
	/*
	 * Constructor used to specify attribute name, type and value to add/modify
	 */
	public Attribute(String name, Attribute.TYPE type, Object value) {
		super();
		this.name = name;
		this.Type = type;
		this.value = value;
	}
	
	/*
	 * Constructor used to specify attribute name and type to fetch
	 */
	public Attribute(String name, TYPE type) {
		super();
		this.name = name;
		this.Type = type;
		this.value = null;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", Type=" + Type + ", value=" + value + "]";
	}
	

}
