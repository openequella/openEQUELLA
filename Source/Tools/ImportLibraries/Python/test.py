# Copyright Dytech Solutions, 2005.

from tleclient import *

SERVER = "localhost"
USERNAME = "cofarrell"
PORT = 8888
CONTEXT = 'http://192.168.106.118:8988/dev/first/'
PASSWORD = "plunket1"
SECRET = "TLE"

tle = TLEClient (host=SERVER, username=USERNAME, port=PORT, context=CONTEXT, password=PASSWORD)
tle.logout()
try:
    tle = TLEClient (host=SERVER, username=USERNAME, port=PORT, context=CONTEXT, password=SECRET, sso=1)
    itemdefID = tle.getItemdefUUID ("K12 - Resource")
    
    item = tle.createNewItem(itemdefID)
    
    uuid = item.getUUID()
    item.setNode ('item/name', "testest")
    item.setNode ('item/description', "description")
    item.submit()
    
    try:
        item = tle.editItem(uuid,1,itemdefID,'true')
        item.attachFile('test.txt',file ('test.txt', 'rb'))
        item.attachFile('test2.txt',file ('test.txt', 'rb'))
        item.addStartPage('test.txt','test.txt')
        item.addStartPage('test2.txt','test2.txt')
        item.submit()
    
        item = tle.editItem(uuid,1,itemdefID,'false')
        item.cancelEdit()
        
        item = tle.editItem(uuid,1,itemdefID,'true')
        item.deleteAttachments()
        item.submit()
        
        xml = tle.getItem(uuid,1,itemdefID)
        assert xml.getNode('item/name') == 'testest'
        
        results = tle.search(query='testest')
        assert results.getNode('result/xml/item/name') == 'testest'
        assert results.nodeCount('result') == 1
        
        #this doesn't work
        #assert tle.queryCount([itemdefID], "/xml/item/name = 'testest'") == 1
    
    finally:
        item.forceUnlock()
        item.delete()
finally:
    tle.logout()