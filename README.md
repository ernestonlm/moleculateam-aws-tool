[DynamoDB]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html
[how to create a DynamoDB table]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/getting-started-step-1.html
[best practices]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html
[DynamoDBExamples.java]: https://github.com/ernestonlm/moleculateam-aws-tool/blob/main/moleculateam-aws-api/src/com/moleculateam/examples/DynamoDBExamples.java
[credentials]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/SettingUp.DynamoWebService.html
[GNU General Public License v3.0]:https://www.gnu.org/licenses/gpl-3.0.html
# Moleculateam AWS API
Java API that allows storing in 2 [DynamoDB] tables multiple entities identified by a simple key or a key composed of 2 attributes in a simplified way

## Motivation

Represent entities (f.e Customer, Invoice, Session) using non-relational databases requires some "obscure" coding, turning our code difficult to read (understand), debug and reuse. This API makes it easy and clean to manage insert/delete/query operations. It's strongly recommended to understand the DynamoDB [best practices].

With a few lines records can be added:

```java
  // Creates secondary attributes name and age
  Collection<Attribute> attributes = new LinkedList<Attribute>();
  attributes.add(new Attribute("name", TYPE.CHAR, "John Smith"));
  attributes.add(new Attribute("age", TYPE.INT, 29));		

  // Add the customer with id 5555555000 to table customer
  db.addItem("Customer", "5555555000", attributes);
```

Adding JSON attributes it's also possible (and recommended):

```java
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
```

It also provides an easy way to copy items between AWS regions and DynamoDB tables:

```java
  // All records are copied from SA_EAST_1 to EU_CENTRAL_1
  GeneralDB target = new GeneralDB(Regions.EU_CENTRAL_1);		
  db.copy_SK_ToTarget(target); // Copy all the single key tables
  db.copyToTarget(target); // Copy all the composed key tables
```
```java
  // All record are copied to new dynamodb tables for testing purpose. This tables will have the prefix "TEST-"
  target = new GeneralDB("TEST-");		
  db.copy_SK_ToTarget(target); // Copy all the single key tables
  db.copyToTarget(target); // Copy all the composed key tables
```

## How it works

API asummes 2 [DynamoDB] tables were created:

  - GeneralSK: [DynamoDB] table named "GeneralSK" with a single primary key named "generalkey" used to store single key entities
  - GeneralDK: [DynamoDB] table named "GeneralDK" with primary key named "generapk" and range key named "generalrk" used to store composed key entities

See documentation on [how to create a DynamoDB table]

A small but playful example is the following. Suppose the tables below with Customers and Invoices information:

Customers:
<table style="border-collapse: collapse; width: 100%;" border="1">
<tbody>
<tr>
<td style="width: 25%;">Id</td>
<td style="width: 25%;">CustomerInfo</td>
</tr>
<tr>
<td style="width: 25%;">5555555000</td>
<td style="width: 25%;">{<br />"name":"John Smith",<br />"age":29,<br />"phones":[{"phone":"(+1)555-555-555","type":"home"},{"phone":"(+1)111-111-111","type":"work"}]<br />}</td>
</tr>
<tr>
<td style="width: 25%;">2222222000</td>
<td style="width: 25%;">{<br />"name":"Jane Doe",<br />"age":21,<br />"phones":[ { "phone":"(+1)222-222-222", "type":"home"}]<br />}</td>
</tr>
</tbody>
</table>

Invoices.
<table style="border-collapse: collapse; width: 100%;" border="1">
<tbody>
<tr>
<td style="width: 25%;">Id</td>
<td style="width: 25%;">InvoiceTotal</td>
</tr>
<tr>
<td style="width: 25%;">2345</td>
<td style="width: 25%;">$ 5000</td>
</tr>
<tr>
<td style="width: 25%;">3111</td>
<td style="width: 25%;">$ 322</td>
</tr>
</tbody>
</table>

Using the following code, this information is added to dynamoDB:

```java
  :
  db.addItem("Customer", "5555555000", attributes);
  db.addItem("Customer", "2222222000", attributes);
  :
  db.addItem("invoice", "2345", attributes);
  db.addItem("invoice", "3111", attributes);
```

As a result, the GeneralSK have the following records:

<table style="border-collapse: collapse; width: 100%;" border="1">
<tbody>
<tr>
<td style="width: 25%;">generalkey</td>
<td style="width: 12.5%;">CustomerInfo</td>
<td style="width: 12.5%;">InvoiceTotal</td>
</tr>
<tr>
<td style="width: 25%;">customer-5555555000</td>
<td style="width: 12.5%;">{<br />"name":"John Smith",<br />"age":29,<br />"phones":[{"phone":"(+1)555-555-555","type":"home"},{"phone":"(+1)111-111-111","type":"work"}]<br />}</td>
<td style="width: 12.5%;">&nbsp;</td>
</tr>
<tr>
<td style="width: 25%;">customer-2222222000</td>
<td style="width: 12.5%;">{<br />"name":"Jane Doe",<br />"age":21,<br />"phones":[ { "phone":"(+1)222-222-222", "type":"home"}]<br />}</td>
<td style="width: 12.5%;">&nbsp;</td>
</tr>
<tr>
<td style="width: 25%;">invoice-2345</td>
<td style="width: 12.5%;">&nbsp;</td>
<td style="width: 12.5%;">$ 5000</td>
</tr>
<tr>
<td style="width: 25%;">invoice-3111</td>
<td style="width: 12.5%;">&nbsp;</td>
<td style="width: 12.5%;">$ 322</td>
</tr>
</tbody>
</table>

## Usage

The [DynamoDBExamples.java] provides a series of examples to understand how to use de API.

## Configuration

  - As mentioned above the creation of GeneralSK and GeneralDK tables are not included as part of the solution. 
  - It is assumed that the AWS [credentials] are correctly configured.

## License

This library is available as open source under the terms of the [GNU General Public License v3.0]


