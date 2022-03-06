package com.moleculateam.aws.dynamodb;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.moleculateam.aws.dynamodb.Attribute.TYPE;


/**
 * The GeneralDB class provides a tool to create a connection with the DynamoDB service and access 2 fixed tables used to represent multiple tables.
 *
 * GeneralSK: It's a Dynamodb table with a single primary key.
 *
 * GeneralDK: It's a Dynamodb table with a composed primary key (primary key and range key)
 * 
 * GeneralDB supports the definition of an environment. This environment allows adding a prefix to the 2 tables names so multiple instances of those tables can be used on the same region.
 * 
 */
public class GeneralDB {
	private AmazonDynamoDB dynamoDB;
	private String env;
	
	/**
	 * Constructor used to create a connection with the default aws profile, the environment defined in APIStatus and the aws Region defined in APIStatus
	 */
	public GeneralDB() {
		if (dynamoDB == null) {
			dynamoDB = AmazonDynamoDBClientBuilder.standard()
					.withRegion(APIStatus.region)
					.build();  
		}
		// Set environment from static APIStatus
		env = APIStatus.getEnv();
	}
	
	/**
	 * Constructor used to create a connection with the default aws profile, the region defined in APIStatus and the environment specified in env
	 */
	public GeneralDB(String env) {
		if (dynamoDB == null) {
			dynamoDB = AmazonDynamoDBClientBuilder.standard()
					.withRegion(APIStatus.region)
					.build();  
		}
		// Set environment from static APIStatus
		this.env = env;
	}
	
	/**
	 * Constructor used to create a connection with the default aws profile and the environment defined in APIStatus
	 * 
	 * @param region The AWS region to connect
	 * 
	 */
	public GeneralDB(Regions region) {
		if (dynamoDB == null) {
			dynamoDB = AmazonDynamoDBClientBuilder.standard()
					.withRegion(region)
					.build();  
		}
		// Set environment from static APIStatus
		env = APIStatus.getEnv();
	}
	
	/**
	 * Constructor used to create a connection with the aws profile provided and the environment defined in APIStatus
	 * 
	 * @param region The AWS region to connect
	 * @param profileName Name of the AWS profile to use for the connection
	 */
	public GeneralDB(Regions region, String profileName) {
		if (dynamoDB == null) {
			// Creates provider using the profile defined
			ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(profileName);
			dynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider)
					.withRegion(region)
					.build();  
		}
		// Set environment from static APIStatus
		env = APIStatus.getEnv();
	}
	
	/**
	 * Constructor used to create a connection with the aws profile provided and the environment provided
	 * 
	 * @param region The AWS region to connect
	 * @param profileName Name of the AWS profile to use for the connection
	 * @param environment Environment to be used by GenerarlDB
	 */
	public GeneralDB(Regions region, String profileName, String environment) {
		if (dynamoDB == null) {
			// Creates provider using the profile defined
			ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(profileName);
			dynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider)
					.withRegion(region)
					.build();  
		}
		// Set environment from static APIStatus
		env = environment;
	}

	public String getEnvironment() {
		return this.env;
	}
	
	/**
	 * Copy all items from the generalsk table to the target defined
	 * 
	 * @param target An instance of GeneralDB where the items (records) are going to be copied
	 * 
	 */
	public void copy_SK_ToTarget(GeneralDB target) {
		ScanRequest scanRequest = new ScanRequest()
			    .withTableName(env+"generalsk");

		int i = 0;
		// Uses scan instead of query and all records are fetched
		ScanResult result = dynamoDB.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			// insert the item fetched into the target.
		    target.dynamoDB.putItem(target.getEnvironment()+"generalsk",item);
		    System.out.println("item "+item.get("generalkey")+" copied");
		    i++;
		}
		System.out.println(i+" items copied");
	}
	
	/**
	 * Copy all items from the generaldk table to the target defined
	 * 
	 * @param target An instance of GeneralDB where the items (records) are going to be copied
	 * 
	 */
	public void copyToTarget(GeneralDB target) {
		ScanRequest scanRequest = new ScanRequest()
			    .withTableName(env+"generaldk");

		int i = 0;
		// Uses scan instead of query and all records are fetched
		ScanResult result = dynamoDB.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			// insert the item fetched into the target.
		    target.dynamoDB.putItem(target.getEnvironment()+"generaldk",item);
		    System.out.println("item "+item.get("generalpk")+"/"+item.get("generalsk")+" copied");
		    i++;
		}
		System.out.println(i+" items copied");
	}
	
	/**
	 * Add a new item into the table tableName with a single key identifier. The secondary attributes are specified in the collection attributes. Duplicate records are overwrite.
	 * 
	 * @param tableName Name of the table where the item is going to be created
	 * @param pk Primary key
	 * @param attributes Collection of the attributes to insert
	 */
	public void addItem(String tableName, String pk, Collection<Attribute> attributes) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		Table tabla = dynamoDBinst.getTable(env+"generalsk");
		
		// The item is recorded using the key tableName+'-'+pk
		Item item = new Item().withPrimaryKey("generalkey", tableName+"-"+pk);
		for (Attribute at : attributes) {
			if (at.Type == Attribute.TYPE.JSON) {
				item = item.withJSON(at.name, (String)at.value);
			}
			
			if (at.Type == Attribute.TYPE.CHAR) {
				item = item.withString(at.name, (String)at.value);
			}
			
			if (at.Type == Attribute.TYPE.INT) {
				item = item.withInt(at.name, (int)at.value);
			}
			
			if (at.Type == Attribute.TYPE.SHORT) {
				item = item.withShort(at.name, (short)at.value);
			}
		}
		
		tabla.putItem(item);
	}
	
	/**
	 * Add a new item into the table tableName identified by the composed key. The secondary attributes are specified in the collection attributes. Duplicate records are overwrite.
	 * 
	 * @param tableName Name of the table where the item is going to be created
	 * @param pk Primary key
	 * @param rk rangeKey
	 * @param attributes Collection of the attributes to insert
	 */
	public void addItem(String tableName, String pk, String rk, Collection<Attribute> attributes) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		Table tabla = dynamoDBinst.getTable(env+"generaldk");
		
		// The item is recorded using the key [tableName+'-'+pk, tableName+'-'+rk] 
		Item item = new Item().withPrimaryKey("generalpk", tableName+"-"+pk,"generalrk",tableName+"-"+rk);
		for (Attribute at : attributes) {
			if (at.Type == Attribute.TYPE.JSON) {
				item = item.withJSON(at.name, (String)at.value);
			}
			
			if (at.Type == Attribute.TYPE.CHAR) {
				item = item.withString(at.name, (String)at.value);
			}
			
			if (at.Type == Attribute.TYPE.INT) {
				item = item.withInt(at.name, (int)at.value);
			}
			
			if (at.Type == Attribute.TYPE.SHORT) {
				item = item.withShort(at.name, (short)at.value);
			}
		}
		
		tabla.putItem(item);
	}
	
	/**
	 * delete from the table tableName the item identified by the key pk. 
	 * 
	 * @param tableName Name of the table where the item is going to be deleted
	 * @param pk Primary key
	 */
	public void deleteItem(String tableName, String pk) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generalsk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// The item to deleted is identified by the key tableName+'-'+pk
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
	            .withPrimaryKey(new PrimaryKey("generalkey", tableName+"-"+pk));
		tabla.deleteItem(deleteItemSpec);
	}
	
	/**
	 * delete from the table tableName the item identified by the composed key specified. 
	 * 
	 * @param tableName Name of the table where the item is going to be deleted
	 * @param pk Primary key
	 * @param rk rangeKey
	 */
	public void deleteItem(String tableName, String pk, String rk) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generaldk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// The item to deleted is identified by the key [tableName+'-'+pk, tableName+'-'+rk]
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
	            .withPrimaryKey(new PrimaryKey("generalpk", tableName+"-"+pk,"generalrk",tableName+"-"+rk));
		tabla.deleteItem(deleteItemSpec);
	}
	
	/**
	 * delete from the table tableName all the items with the primary key specified in pk 
	 * 
	 * @param tableName Name of the table where the items are going to be deleted
	 * @param pk Primary key
	 */
	public void deleteItems(String tableName, String pk) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generaldk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// The items to delete are filtered by the condition generalpk == tableName+'-'+pk
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withConditionExpression("generalpk = :usucod");
		ValueMap values = new ValueMap()
		        .withString(":usucod", tableName+'-'+pk);
		deleteItemSpec.withValueMap(values);
		tabla.deleteItem(deleteItemSpec);
	}
	
	/**
	 * Get attribute from record with primary key specified from the table specified. When the item is not found the method return null.
	 * 
	 * @param tableName Table where to look for the PK
	 * @param pk primary key (identifier) of the record to look
	 * @param att Attribute to look for
	 */
	public Object getAttribute(String tableName, String pk, Attribute att) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generalsk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// The item is fetched using the key tableName+'-'+pk
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("generalkey = :usucod");
		ValueMap values = new ValueMap()
        .withString(":usucod", tableName+'-'+pk);
		
		querySpec = querySpec.withValueMap(values);
		ItemCollection<QueryOutcome> items = tabla.query(querySpec);
		Iterator<Item> iterator = items.iterator();
		if (iterator.hasNext()) {
			if (att.Type == Attribute.TYPE.JSON) {
				String json = iterator.next().getJSON(att.name);
				// Replace " " for "" so empty values are recognized by the "" value
				json = json.replace("\" \"","\"\"");
				return (Object)json;
			}
			
			if (att.Type == Attribute.TYPE.CHAR) {
				String json = iterator.next().getString(att.name);
				return (Object)json;
			}
			if (att.Type == Attribute.TYPE.INT) {
				return (Object)iterator.next().getInt(att.name);
			}
			if (att.Type == Attribute.TYPE.SHORT) {
				return (Object)iterator.next().getShort(att.name);
			}
		}
		return (Object)"";
	}	
	
	/**
	 * Get attribute from record with primary key and range key specified from the table specified. When the item is not found the method return null.
	 * 
	 * @param tableName Table where to look for the PK
	 * @param pk primary key (identifier) of the record to look
	 * @param rk range key (identifier) of the record to look
	 * @param att Attribute to look for
	 */
	public Object getAttribute(String tableName, String pk, String rk, Attribute att) {
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generaldk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// The item is fetched using the key [tableName+'-'+pk, tableName+'-'+rk]
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("generalpk = :usucod and generalrk = :cannom");
		ValueMap values = new ValueMap()
        .withString(":usucod", tableName+'-'+pk).withString(":cannom", tableName+'-'+rk);
		querySpec = querySpec.withValueMap(values);
		
		ItemCollection<QueryOutcome> items = tabla.query(querySpec);
		Iterator<Item> iterator = items.iterator();
		if (iterator.hasNext()) {
			if (att.Type == Attribute.TYPE.JSON) {
				String json = iterator.next().getJSON(att.name);
				// Replace " " for "" so empty values are recognized by the "" value
				json = json.replace("\" \"","\"\"");
				return json;
			}

			if (att.Type == Attribute.TYPE.CHAR) {
				String json = iterator.next().getString(att.name);
				return json;
			}
			
			if (att.Type == Attribute.TYPE.INT) {
				return iterator.next().getInt(att.name);
			}
			
			if (att.Type == Attribute.TYPE.SHORT) {
				return iterator.next().getShort(att.name);
			}
		}
		return null;
	}
	
	/**
	 * Query all records of the composed key table tableName with primary key equals to pk and return the attributes specified in att
	 * 
	 * @param tableName Table where to filter for PK
	 * @param pk primary key (identifier) of the records to query
	 * @param att Attribute to look for and return
	 */
	public Collection<Object> getAttributes(String tableName, String pk, Attribute att) {
		Collection<Object> col = new LinkedList<Object>();
		DynamoDB dynamoDBinst = new DynamoDB(dynamoDB);
		String tblName = env+"generaldk";
		Table tabla = dynamoDBinst.getTable(tblName);
		
		// Items are filtered using the condition pk == tableName+'-'+pk
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("generalpk = :usucod");
		ValueMap values = new ValueMap()				
        .withString(":usucod", tableName+"-"+pk);
		querySpec = querySpec.withValueMap(values);
		ItemCollection<QueryOutcome> items = tabla.query(querySpec);
		Iterator<Item> iterator = items.iterator();
		
		while (iterator.hasNext()) {
			if (att.Type == TYPE.JSON) {
				String json = iterator.next().getJSON(att.name);
				// Replace " " for "" so empty values are recognized by the "" value
				json = json.replace("\" \"","\"\"");
				col.add(json);
			}
			
			if (att.Type == TYPE.CHAR) {
				String json = iterator.next().getString(att.name);
				col.add(json);
			}
			if (att.Type == TYPE.INT) {
				col.add(iterator.next().getInt(att.name));
			}
			if (att.Type == TYPE.SHORT) {
				col.add(iterator.next().getShort(att.name));
			}
		}
		return col;
	}
	
}
