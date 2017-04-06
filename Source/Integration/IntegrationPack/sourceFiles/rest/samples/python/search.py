import equellarest
import util
import settings


# Create a new REST Helper which is a light wrapper around our REST api
# Requres an Oauth Client to be set up in EQUELLA using Client Credentials flow
equella = equellarest.EquellaRest(settings.INSTITUTION_URL, settings.OAUTH_CLIENT_ID, settings.OAUTH_CLIENT_SECRET, settings.PROXY_URL)

print 'Searching for items...\n'

# The search method has a number of optional parameters:
# query - The search query
# collections - An array of collection uuids
# start - The index of the first result to return
# length - The number of results, max of 100
# order - The order of the results. Can be one of 'relevance', 'modified', 'name' or 'rating'
# reverse - reverse the order of results
# where - The where clause for advanced searching
# showAll - Include items that aren't live
# info - A comma separated list of the information returned with each result, can be  'basic', 'metadata', 'detail', 'attachment' and 'all'.
results = equella.search(query='*', info='basic')

print 'Items found (Showing %d to %d of %d results): ' % (results['start']+1,  results['length']+results['start'], results['available'] )
for item in results['results']:
	print '%s - %s' % ((item['name'] if 'name' in item  else item['uuid']), item['links']['view'])




# Gets information about an item
# Version can be an actual version number, 'latestlive', or 'latest'
itemUuid = settings.search_settings['ITEM_UUID']
itemVersion = settings.search_settings['ITEM_VERSION']
if itemUuid is not None:
	item = equella.getItem(itemUuid, itemVersion, info='all')

	print '\nItem name: %s' % item['name'] 
	print 'Item description: %s' % item['description']  if 'description' in item else ''
	print 'Item version: %d' % item['version'] 
	print 'Item status: %s' % item['status'] 
	print 'Item xml: %s' % item['metadata'] 
	for attachment in item['attachments']:
		print "Attachment: %s" % attachment['uuid']
		print "\tName: %s" % attachment['description']
		print "\tType: %s" % attachment['type']
		if 'filename' in attachment:
		    print "\tFilename: %s" % attachment['filename'] 
		if 'url' in attachment:
		    print "\tURL: %s" % attachment['url'] 
