# Gecko Notary System
## Data Model

The meta-model of the notary system is located in the project:

*org.gecko.notary.model/model/notary.ecore*

### Assets

Each thing can be an asset. Assets are the objects that can have attributes that may change over their lifetime. These changes will be recorded / audited by the notary system.

You may define an asset as digital twin. This twin usually only contains basic information, that are really necessary for the audit. Things you can do with assets, are Transactions ...

### Transactions

Transactions are definitions for actions that can occur in the context of assets. As a basic there are pre-defined, built in transactions for assets, called Asset-Transactions. 

They record:

* Creation of Assets
* Modification of Assets
* Destruction of Assets
* Join Assets into another Asset
* Separate/Split an assets from others
* Changing an owner of an asset

These types are defined in the *notary.ecore* meta-model as **AssetChangeType**.

Beside that, transactions can also have a notification definition. This can be used to subscribe to certain topics, that belong to this transaction. Every time a new transaction entry is created you can get notified, depending of the transactions notification settings.

### Transaction Entries

Transaction entries are the logs of a certain transaction for an asset, the transaction belongs to. They build up the audit log. Their creation also triggers the event mechanism.

## Text Provider

Any time a transaction entry is recorded or an notification is created a text provider comes into play. The provide the content of a transaction entry, that is human readable. A text is stored beside the transaction entry fields.