package com.moleculateam.examples;

import java.util.Collection;
import java.util.LinkedList;

import com.amazonaws.regions.Regions;
import com.moleculateam.aws.dynamodb.APIStatus;
import com.moleculateam.aws.dynamodb.Attribute;
import com.moleculateam.aws.dynamodb.Attribute.TYPE;
import com.moleculateam.aws.dynamodb.GeneralDB;

public class DynamoDBExamples {

	public static void main(String[] args) {
		
		// Set the AWS region and the empty environment
		APIStatus.region = Regions.SA_EAST_1;
		APIStatus.env = "";
		
		// Creates Dynamodb connection using default profile, region from APIStatus and environment from APIStatus
		GeneralDB db = new GeneralDB();
		
		/*
		 * Example 1 - Simplified customer:
		 * Add customer and his phones using tables named customer and CustomerPhone
    	 */
		
		// Creates secondary attributes name and age
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("name", TYPE.CHAR, "John Smith"));
		attributes.add(new Attribute("age", TYPE.INT, 29));		
		
		// Add the customer with id 5555555000 to table customer
		db.addItem("Customer", "5555555000", attributes);
		
		attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("PhoneType", TYPE.CHAR, "home"));
		
		// Add the home phone "(+1)555-555-555" of the customer with id 5555555000 to table CustomerPhone
		db.addItem("CustomerPhone", "5555555000", "(+1)555-555-555", attributes);
		
		attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("PhoneType", TYPE.CHAR, "work"));
		
		// Add the work phone "(+1)111-111-111" of the customer with id 5555555000 to table CustomerPhone
		db.addItem("CustomerPhone", "5555555000", "(+1)111-111-111", attributes);
		
		// Delete all phones associated with customer 5555555000 and the customer record from table "customer"
		db.deleteItems("CustomerPhone", "5555555000");
		db.deleteItem("Customer", "5555555000");
		
		/*
		 * Example 2 - Customer example using JSON (recommended for non-relational db):
		 * Add customer and his phones using table Customer
		 */
		
		// In this example the customer information is represented as a JSON and it's stored as an attribute
		String customerInfo = "{\r\n" + 
				"   \"name\":\"John Smith\",\r\n" + 
				"   \"age\":29,\r\n" + 
				"   \"phones\":[\r\n" + 
				"      {\r\n" + 
				"         \"phone\":\"(+1)555-555-555\",\r\n" + 
				"         \"type\":\"home\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"         \"phone\":\"(+1)111-111-111\",\r\n" + 
				"         \"type\":\"work\"\r\n" + 
				"      }\r\n" + 
				"   ]\r\n" + 
				"}";

		attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("CustomerInfo", TYPE.JSON, customerInfo));
		
		db.addItem("Customer", "5555555000", attributes);
		
		/*
		 * Example 3 - Getting records
		 */

		System.out.println(db.getAttribute("Customer", "5555555000", new Attribute("CustomerInfo", TYPE.JSON)));
		
		/*
		 * Example 4 - Migrate between regions:
		 * All record are copied from SA_EAST_1 to EU_CENTRAL_1
		 */
		
		// Creates a connection using the default profile and environment 
		GeneralDB target = new GeneralDB(Regions.EU_CENTRAL_1);		
		db.copy_SK_ToTarget(target); // Copy all the single key tables
		db.copyToTarget(target); // Copy all the composed key tables
		
		/*
		 * Example 5 - Create a test version of the tables
		 * All record are copied to new dynamodb tables for testing purpose. This tables will have the prefix "TEST-"
		 */
		
		// Creates a connection using the default profile, region from APIStatus and environment specified by parameter 
		target = new GeneralDB("TEST-");		
		db.copy_SK_ToTarget(target); // Copy all the single key tables
		db.copyToTarget(target); // Copy all the composed key tables
		
	}

}
