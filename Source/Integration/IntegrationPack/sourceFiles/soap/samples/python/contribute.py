import os, stat
import settings
import equellasoap


def contribute(equella, collectionUuid, itemName, itemDescription, attachmentDescription = None, attachmentFilename = None, localAttachmentFile = None, fileSize = 0):
	#Create the item on the server.  The general process is: 
	# 1. newItem (creates a new item in the staging folder, doesn't get committed until saveItem is called)
	# 2. set metadata on the item XML (some are system fields e.g. /xml/item/attachments, others could be custom schema fields)
	# 3. saveItem (commits the changes to a previous newItem or editItem call)

	itemSession = equella.newItem(collectionUuid)
	
	#set item name and description
	#name and description xpaths are collection dependent!  You need to find the correct xpath to use for the name and description nodes
	collection = equella.getCollection(collectionUuid)
	schema = equella.getSchema(collection.getNode('schemaUuid'))

	itemNameXPath = schema.getNode('itemNamePath')
	if itemNameXPath[0] == '/':
		itemNameXPath = itemNameXPath[1:]
	
	itemDescriptionXPath = schema.getNode('itemDescriptionPath')
	if itemDescriptionXPath[0] == '/':
		itemDescriptionXPath = itemDescriptionXPath[1:]
	
	itemXml = itemSession.getXml()
	itemXml.setNode(itemNameXPath, itemName)
	itemXml.setNode(itemDescriptionXPath, itemDescription)
	
	#itemXml.printXml()

	#upload attachments
	if localAttachmentFile is not None:
		itemSession.uploadFile(attachmentFilename, localAttachmentFile)
		itemSession.addAttachment(attachmentDescription, attachmentFilename, size = fileSize)
		

	#save and submit
	itemSession.submit()

	print 'Item "%s" contributed' % (itemName,)
	
	
	
	
	

equella = equellasoap.EquellaSoap(settings.institutionUrl, settings.username, settings.password, proxyUrl = settings.proxyUrl)

collectionUuid = equella.getContributableCollections().values()[0]['uuid']
itemName = 'test python soap'
itemDescription = 'uploaded via contribute.py'
attachmentDescription = 'attachment 1'
attachmentFilename = 'attach.jpg'
localFilename = 'testAttachment.jpg'
fileSize = os.lstat ( localFilename )[stat.ST_SIZE]
localAttachmentFile = open (localFilename, 'rb')

contribute(equella, collectionUuid, itemName, itemDescription, attachmentDescription, attachmentFilename, localAttachmentFile, fileSize=fileSize)

equella.logout()