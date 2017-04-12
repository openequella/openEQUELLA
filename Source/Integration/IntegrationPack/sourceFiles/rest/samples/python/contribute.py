import equellarest
import util
import settings
import uuid
import os, stat

# Create a new REST Helper which is a light wrapper around our REST api
# Requires an Oauth Client to be set up in EQUELLA using Client Credentials flow
equella = equellarest.EquellaRest(settings.INSTITUTION_URL, settings.OAUTH_CLIENT_ID, settings.OAUTH_CLIENT_SECRET, settings.PROXY_URL)

path = os.path.dirname(os.path.realpath(__file__))
localFileName = 'testAttachment.jpg'
localFilePath = os.path.join(path, localFileName)
fileSize = os.lstat ( localFilePath )[stat.ST_SIZE]

name = settings.contribute_settings['ITEM_NAME']
description = settings.contribute_settings['ITEM_DESCRIPTION']

# You cannot provide name and/or description except in the metadata.  
# The location of the name and description nodes will depend on your schema.					
item = {'metadata': settings.contribute_settings['ITEM_METADATA_TEMPLATE'] % (name, description, 'uuid:attach') }			
item['collection'] = { 'uuid' : settings.contribute_settings['COLLECTION_UUID'] }
item['attachments'] = [{	"type": "file",
					"uuid": "uuid:attach",
					"description": localFileName,
					"filename": localFileName
				}]


# Create a staging area that we can upload files to
staging = equella.createStaging()
print staging

# Upload the file to the staging area
localAttachmentFile = open (localFilePath, 'rb')
json = equella.uploadFile(staging['uuid'],  localFileName, localAttachmentFile, fileSize)
localAttachmentFile.close()
print json

# Create a new live EQUELLA item from our object then print it out
item = equella.newItem(item, draft = 'false', staging = staging['uuid'])
print item
