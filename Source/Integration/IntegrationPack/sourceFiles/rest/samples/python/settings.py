#global settings

#institution URL must end in a '/'
INSTITUTION_URL = 'http://www.myhost.com/path/to/institution/'
OAUTH_CLIENT_ID = 'python'
OAUTH_CLIENT_SECRET = 'xxxxxxxxxxxxx'


#proxy settings
#use this if your network connection requires use of proxy.  If the proxy requires authentication the url will be of the form http://username:password@proxyurl:proxyport
PROXY_URL = None




#contribute.py settings

# ITEM_METADATA_TEMPLATE placeholders are: name, description and attachment UUID
contribute_settings = {
	'COLLECTION_UUID': '6e85ce64-9a11-c5e7-69a4-bd30ec61007f',
	'ITEM_NAME': 'EQUELLA Python REST Client Test Item',
	'ITEM_DESCRIPTION': 'Uploaded via the Python REST client',
	'ITEM_METADATA_TEMPLATE': '<xml><item><itembody><name>%s</name><description>%s</description><attachments><attachment>%s</attachment></attachments></itembody></item></xml>'
}



#search.py settings

# Replace ITEM_UUID with an known item UUID to test the item API
search_settings = { 
	'ITEM_UUID': None,
	'ITEM_VERSION': 'latestlive'
}
