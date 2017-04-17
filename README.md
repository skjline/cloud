# Mobile Technical Challenge

### Mobile Application with AWS Serverless Backend.

A simple Android application view and manage contacts.

Contacts are stored on the AWS DynamoDB database. Anyone on the application is allow to view a list of contacts.
The list is seacheable by either give or last name. The search is performed on the database and the application makes fetching requests.

A user can select a contact from a list to view detail information of the contact.
A detail view of a contact shows name, phone number, e-mail address and mail address.

When a user logs in with a valid Google ID, he/she will then gain a privilege to create, update, and delete contacts. 
After making any change in the database, the application will be notified through firebase cloud messaging system and trigger a notification.

----
### Cloud Components
<p>
[[https://raw.githubusercontent.com/skjline/cloud/master/res/aws-layout.png]]
<p>

* Amazon Web Service
  * DynamoDB
  * Congito
  * IAM
  * Lambda
  * SNS
* Google Cloud Service
  * Google API OAuth 2.0
  * Firebase Cloud Message
