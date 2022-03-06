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
	
	public Attribute(String name, Attribute.TYPE type, Object value) {
		super();
		this.name = name;
		this.Type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", Type=" + Type + ", value=" + value + "]";
	}
	

}
