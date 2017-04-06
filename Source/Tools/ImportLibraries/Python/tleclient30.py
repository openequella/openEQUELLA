# Copyright Dytech Solutions, 2005.

# This module is provided 'commercial-in-confidence' and may not be reproduced nor redistributed without
# express written permission from the copyright holder.

# Author: Adam Eijdenberg, Dytech Solutions <adam.eijdenberg@dytech.com.au>
# Note: this library is a simple example of how a SOAP client may be written to access functionality provided by The Learning Edge.
# While this library will be sufficient for basic importing of content, for more robust applications a proper WSDL wrapping library
# is recommended. This has not been included in order to keep this example simple.

import httplib
from xml.dom.minidom import parse, parseString
from xml.dom import Node
from binascii import b2a_base64
import codecs
import os, os.path

ASCII_ENC = codecs.getencoder('us-ascii')

SOAP_REQUEST = """
		<SOAP-ENV:Envelope SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/">
			<SOAP-ENV:Body>
				<ns1:%(method)s xmlns:ns1="%(ns)s">
					%(params)s
				</ns1:%(method)s>
			</SOAP-ENV:Body>
		</SOAP-ENV:Envelope>
	"""
SOAP_PARAMETER = '<%(name)s xsi:type="%(type)s">%(value)s</%(name)s>'
HTTP_HEADERS = {"Content-type": "text/xml", "SOAPAction": ""}
SOAP_FACADE = 'services/SoapInterfaceV1'

def escape (s):
	return s.replace ('&', '&amp;').replace ('<', '&lt;').replace ('>', '&gt;').replace ('"', '&quot;').replace ("'", '&apos;')

def unzipFile (zipfile, directory, f):
	os.system ('unzip -o "%s" "%s" -d "%s"' % (zipfile, f, directory))
	
def zipFile (zipfile, f):
	os.system ('zip -j -9 "%s" "%s"' % (zipfile, f))

def getManifest (tempdir, zipfile):
	unzipFile (zipfile, tempdir, 'imsmanifest.xml')
	return file (os.path.join (tempdir, 'imsmanifest.xml'), 'rb').read ()
	
def copyFile (src, dest):
	file (dest, 'wb').write (file (src, 'rb').read ())
	
def updateManifest (tempdir, zipfile, manifest):
	file (os.path.join (tempdir, 'imsmanifest.xml'), 'wb').write (manifest)
	zipFile (zipfile, os.path.join (tempdir, 'imsmanifest.xml'))

def extractColumnResults (xpaths, xmls):
	xml = parseString (xmls)
	results = []
	for node in xml.getElementsByTagName ('result'):
		curResult = {}
		for xpath in xpaths:
			cur = node
			elements = xpath.split ('/')
			if elements [-1] [:1] == '@':
				elements, attr = elements [:-1], elements [-1] [1:]
			else:
				attr = None
			for element in elements [1:]:
				cur = get_named_child_element (xml, cur, element)
			if attr:
				curResult [xpath] = cur.getAttribute (attr)
			else:
				curResult [xpath] = value_as_string (cur)
		results.append (curResult)
	return results
	
def value_as_string (node):
	node.normalize ()
	return ''.join ([x.nodeValue for x in node.childNodes])
	
def get_named_child_value (node, name):
	return value_as_string (node.getElementsByTagName (name) [0])

def get_named_child_elements (dom, node, name):
	matches = [x for x in node.childNodes if x.nodeType == Node.ELEMENT_NODE and x.tagName == name]
	if len (matches):
		return matches
	else:
		child = dom.createElement (name)
		node.appendChild (child)
		return [child]
		
def get_named_child_element (dom, node, name):
	return get_named_child_elements (dom, node, name) [0]

def create_actual_node (dom, nodename, value):
	if type (value) == list:
		return reduce (lambda a, b: a + b, [create_actual_node (dom, nodename, item) for item in value], [])  
	else:
		rv = dom.createElement (nodename)
		if type (value) == dict:
			for item in reduce ((lambda a, b: a + b), [create_actual_node (dom, name, val) for name, val in value.items ()]):
				rv.appendChild (item)
		elif type (value) == tuple: # allow tuples instead of dictionaries to allow ordering of keys
			for item in reduce ((lambda a, b: a + b), [create_actual_node (dom, name, val) for name, val in value]):
				rv.appendChild (item)			
		else:
			rv.appendChild (dom.createTextNode (unicode (value)))
		return [rv]

def clean_unicode (s):
	if s.__class__ == unicode:
		return ASCII_ENC (s, 'xmlcharrefreplace') [0]
	else:
		return s

def generate_soap_envelope (name, params, ns):
	return SOAP_REQUEST % {'ns': ns, 'method': name, 'params': ''.join ([SOAP_PARAMETER % {'name': name, 'type': type, 'value': escape (clean_unicode (value))} for name, type, value in params])}

import time, md5, urllib

def createSSOToken (username, secret, id=''):
	seed = str (int (time.time ())) + '000'
	id2 = urllib.urlencode ({'q': id}) [2:]
	if(not(id == '')):
		id2 += ':'
	return '%s:%s%s:%s' % (urllib.urlencode ({'q': username}) [2:], id2, seed, b2a_base64 (md5.new (username + id + seed + secret).digest ()))

# Class designed to make communicated with TLE very easy!
class TLEClient:
	# First, instantiate an instance of this class.
	# e,g, client = TLEClient ('lcms.yourinstitution.edu.au', 'admin', 'youradminpasssword')
	def __init__ (self, host, username, password, port=80, context='', sso=0):
		self.host = host
		self.port = port
		self.context = context
		self.cookie = None
		if sso:
			self.sessionid = self._createSoapSessionFromToken (createSSOToken (username, password))
		else:
			self.sessionid = self._createSoapSession (username, password)
	
	def _call (self, name, args, returns=1, facade=SOAP_FACADE, ns="http://www.dytech.com.au"):
		failCount = 0
		done = 0
		while not done:
			try:
				conn = httplib.HTTPConnection (self.host, self.port)
				headers = {}
				headers.update (HTTP_HEADERS)
				if self.cookie:
					headers ['Cookie'] = self.cookie
				conn.request ("POST", '%s%s' % (self.context, facade), generate_soap_envelope (name, args, ns), headers)
				response = conn.getresponse ()
				cookie = response.getheader ('set-cookie')
				if cookie:
					self.cookie = cookie [:cookie.index (';')]
				s = response.read ()
				conn.close ()
				done = 1
			except:
				if failCount < 10:
					failCount += 1
				else:
					raise Exception, 'Cannot communicate with server.'
		if response.status != 200:
			print "Request:", generate_soap_envelope (name, args, ns)
			raise Exception, 'Server returned: %i %s %s' % (response.status, response.reason, s)
		try:
			dom = parseString (s)
		except:
			raise Exception, 'Cannot parse server response as XML: %s' % s
		if len (dom.getElementsByTagNameNS ('http://schemas.xmlsoap.org/soap/envelope/', 'Fault')):
			raise Exception, 'Server returned following SOAP error: %s' % dom.toprettyxml ()
		elif returns: # then return a result, otherwise do nothing
			return parseString (s).firstChild.firstChild.firstChild.firstChild;
	
	def _createSoapSessionFromToken (self, token):
		result = self._call ('loginWithToken', (
				('token', 'xsd:string', token),
			))
		return value_as_string (result)
		
	def _createSoapSession (self, username, password):
		result = self._call ('login', (
				('username', 'xsd:string', username),
				('password', 'xsd:string', password),
			))
		return value_as_string (result)
	
	def logout (self):
		self._call ('logout', (
				('ssid', 'xsd:string', self.sessionid),
			))
		
	def _unzipFile (self, stagingid, zipfile, outpath):
		self._call ('unzipFile', (
				('ssid', 'xsd:string', self.sessionid),
				('item_uuid', 'xsd:string', stagingid),
				('zipfile', 'xsd:string', zipfile),
				('outpath', 'xsd:string', outpath),
			), returns=0)
		
	def _enumerateItemDefs (self):
		result = self._call ('enumerateWritableItemDefs', (
				('ss_uuid', 'xsd:string', self.sessionid),
			))
		return dict ([(get_named_child_value (itemdef, 'name'), {'uuid': get_named_child_value (itemdef, 'uuid'), 'ident': get_named_child_value (itemdef, 'identifier')}) for itemdef in parseString (value_as_string (result)).getElementsByTagName ('itemdef')])

	def _newItem (self, itemdefid):
		result = self._call ('newItem', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('ss_uuid', 'xsd:string', itemdefid),
			))
		return parseString (value_as_string (result))
		
	def _startNewVersion (self, itemid, itemversion, itemdefid, copyattachments):
		result = self._call ('startNewVersion', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', itemversion),
				('itemdefid', 'xsd:string', itemdefid),
				('copyattachments', 'xsd:boolean', copyattachments),
			))
		return parseString (value_as_string (result))
		
	def _startEdit (self, itemid, itemversion, itemdefid, copyattachments):
		result = self._call ('startEdit', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
				('copyattachments', 'xsd:boolean', str (copyattachments)),
			))
		return parseString (value_as_string (result))
		
	def _forceUnlock (self, itemid, itemversion, itemdefid):
		result = self._call ('forceUnlock', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
			), returns=0)
		return result
	
	def _stopEdit (self, xml, submit):
		result = self._call ('stopEdit', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemXML', 'xsd:string', xml),
				('bSubmit', 'xsd:boolean', submit),
			))
		return result
	
	def _forceUnlock (self, itemid, itemversion, itemdefid):
		result = self._call ('forceUnlock', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
			))
		return result
	
	def _cancelEdit (self, itemid, itemversion, itemdefid):
		result = self._call ('cancelEdit', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
			))
		return result
		
	def _uploadFile (self, stagingid, filename, data, overwrite):
		result = self._call ('uploadAttachment', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('item_uuid', 'xsd:string', stagingid),
				('filename', 'xsd:string', filename),
				('data', 'xsd:string', data),
				('overwrite', 'xsd:boolean', overwrite),
			))
		return result
		
	def _deleteAttachmentFile (self, stagingid, filename):
		result = self._call ('deleteAttachment', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('item_uuid', 'xsd:string', stagingid),
				('filename', 'xsd:string', filename),
			))
		return result
		
	def _deleteItem (self, itemid, itemversion, itemdefid):
		result = self._call ('deleteItem', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
			))
		return result
		
	def getItem (self, itemid, itemversion, itemdefid, select=''):
		result = self._call ('getItem', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemid', 'xsd:string', itemid),
				('version', 'xsd:int', str (itemversion)),
				('itemdefid', 'xsd:string', itemdefid),
				('select', 'xsdxsd:string', select),
			))
		return XmlDoc(value_as_string(result)).getSubtree('xml')
	
	def queryCount (self, itemdefs, where):
		result = self._call ('queryCount', (
				('ss_uuid', 'xsd:string', self.sessionid),
				('itemdefs', 'ns1:Array', itemdefs),
				('where', 'xsd:string', where),
			))
		return int(value_as_string(result))

	# Return an itemdef UUID given a human displayable name.
	# e.g. itemdefUUID = client.getItemdefUUID ('K-12 Educational Resource')
	def getItemdefUUID (self, itemdefName):
		return self._enumerateItemDefs () [itemdefName] ['uuid']
		
	# Return an ident given a human displayable name.
	# e.g. itemdefUUID = client.getItemdefUUID ('K-12 Educational Resource')
	def getItemdefIdent (self, itemdefName):
		return self._enumerateItemDefs () [itemdefName] ['ident']
		
	# Create a new repository item of the type specified. See NewItemClient for methods that can be called on the return type.
	# e.g. item = client.createNewItem (itemdefUUID)
	def createNewItem (self, itemdefid):
		rv = NewItemClient (self, self._newItem (itemdefid))
		#rv.setNode ('item/@itemdefid', itemdefid)
		return rv

	# Create a new repository item of the type specified. See NewItemClient for methods that can be called on the return type.
	# e.g. item = client.createNewItem (itemdefUUID)
	def createNewVersion (self, itemid, version, itemdefid, copyattachments):
		rv = NewItemClient (self, self._startNewVersion (itemid, version, itemdefid, copyattachments), newversion=1, copyattachments=(copyattachments == 'true'))
		return rv
		
	# Edit particular item. See NewItemClient for methods that can be called on the return type.
	# e.g. item = client.createNewItem (itemdefUUID)
	def editItem (self, itemid, version, itemdefid, copyattachments):
		dom = self._startEdit (itemid, version, itemdefid, copyattachments)
		if not len (dom.getElementsByTagName ('item')):
			self._forceUnlock (itemid, version, itemdefid)
			dom = self._startEdit (itemid, version, itemdefid, copyattachments)
		rv = NewItemClient (self, dom, newversion=0, copyattachments=(copyattachments == 'true'))
		return rv

	def search(self, offset=0, limit=10, select='*', itemdefs=[], where='', query=''):
		request = XmlDoc('<com.dytech.edge.common.valuebean.SearchRequest></com.dytech.edge.common.valuebean.SearchRequest>')
		if len(where):
			itemfilter = """
				<whereQuery class="com.dytech.edge.itemfilters.RawBaseItemFilter">
					<andFilters>
						<com.dytech.edge.queries.RawQuery>
							<xoql>%s</xoql>
							<type> AND </type>
							<not>false</not>
							<and>true</and>
						</com.dytech.edge.queries.RawQuery>
					</andFilters>
					<type> AND </type>
					<not>false</not>
					<and>true</and>
				</whereQuery>
			""" % where
			request.createNodeFromNode('com.dytech.edge.common.valuebean.SearchRequest',parseString(itemfilter));
		request.setNode('com.dytech.edge.common.valuebean.SearchRequest/query',query);
		request.setNode('com.dytech.edge.common.valuebean.SearchRequest/select',select);
		request.setNode('com.dytech.edge.common.valuebean.SearchRequest/orderby','/xml/item/name');
		request.setNode('com.dytech.edge.common.valuebean.SearchRequest/where',where);
		request.setNode('com.dytech.edge.common.valuebean.SearchRequest/onlyLive','true');
		if len(itemdefs):
			request.setNode('com.dytech.edge.common.valuebean.SearchRequest/itemdefs/@class','list')
			for i in itemdefs:
				request.setNode('com.dytech.edge.common.valuebean.SearchRequest/itemdefs/string',i)
		request = request.toXml()
		result = self._call ('searchItems', (
				('session', 'xsd:string', self.sessionid),
				('request', 'xsd:string', request),
				('offset', 'xsd:int', str(offset)),
				('limit', 'xsd:int', str(limit)),
			))
		return XmlDoc(value_as_string (result)).getSubtree('results')

def value_as_node_or_string (cur):
	cur.normalize ()
	if len (cur.childNodes) == 1:
		if cur.firstChild.nodeType == cur.TEXT_NODE:
			return value_as_string (cur)
	return cur


class XmlDoc:
	def __init__ (self, s):
		self.newDom = parseString (s)
		
	# Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# Will happily accept dictionaries to create nested nodes, and array to create multiple nodes.
	# Will also accept any nested combination of the above!
	# e.g.
	#item.createNode ('item/management', {
	#    'general': {
	#	    'catalogue.entry': [
	#		    {'catalogue': 'URL', 'entry': 'fdsfs'},
	#		    {'catalogue': 'ISBN', 'entry': 'fdfs'},
	#		],
	#	    'title': {'title.main': 'Title'},
	#	    'description': 'Description',
	#	    'keyword': ['a', 'b', 'c'],
	#	    'resource.type': 'xxxx',
	#	    'folder': 'xxxx',
	#	},
	#    'meta.metadata': {
	#	    'contribute': {'role': 'Creator', 'entity': 'xxxxxx', 'date': 'yyyyyy'},
	#	    'security': 'jjjjjj',
	#	},
	# })
	# This will automatically create parent nodes as required.
	def createNode (self, xpath, value):
		cur = self.newDom
		elements = xpath.split ('/')
		elements, last = elements [:-1], elements [-1]
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		for item in create_actual_node (self.newDom, last, value):
			cur.appendChild (item)
			
	def createNodeFromNode (self, xpath, node):
		cur = self.newDom
		elements = xpath.split ('/')
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		for child in node.childNodes:
			cur.appendChild (child.cloneNode (deep=1))
		
	# Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.setNode ('item/description', 'This item describes ....')
	# To create multiple or complexnodes, use the createNode method.
	# This will automatically create parent nodes as required.
	def setNode (self, xpath, value):
		cur = self.newDom
		elements = xpath.split ('/')
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		if attr:
			cur.setAttribute (attr, value)
		else:
			for child in cur.childNodes:
				cur.removeChild (child)
			cur.appendChild (self.newDom.createTextNode (unicode (value)))

	# Get an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.getNode ('item/description', 'This item describes ....')
	def getNodes (self, xpath):
		elements = xpath.split ('/')
		cur = self.newDom
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
		curNodes = [cur]
		for element in elements:
			newCur = []
			for cur in curNodes:
				newCur += get_named_child_elements (self.newDom, cur, element)
			curNodes = newCur
		if attr:
			return [cur.getAttribute (attr) for cur in curNodes]
		else:
			return [value_as_node_or_string (cur) for cur in curNodes]
			
	# Get an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.getNode ('item/description', 'This item describes ....')
	def getNode (self, xpath):
		return self.getNodes (xpath) [0]

	def getSubtree(self, xpath):
		return XmlDocDoc(self.getNodes(xpath) [0])

	def removeNode (self, xpath): 
		cur = self.newDom 
		elements = xpath.split ('/') 
		if elements [-1] [:1] == '@': 
			raise "Can't just remove an attribute." 
		for element in elements [:-1]: 
			cur = get_named_child_element (self.newDom, cur, element) 
		for child in cur.childNodes: 
			if child.nodeName == elements [-1]: 
				cur.removeChild (child)


	# Print tabbed XML for this item, useful for debugging.
	def printXml (self):
		print ASCII_ENC (self.newDom.toprettyxml (), 'xmlcharrefreplace') [0]
	
	# Print tabbed XML for this item, useful for debugging.
	def toXml (self):
		return self.newDom.toxml ().encode ('utf-8')
	
	def nodeCount(self, xpath):
		return len(self.getNodes(xpath))

class XmlDocDoc(XmlDoc):
	def __init__ (self, s):
		self.newDom = s

class NewItemClient:
	def __init__ (self, parClient, newDom, newversion=0, copyattachments=1):
		self.parClient = parClient
		self.newDom = newDom
		self.xml = get_named_child_element (self.newDom, self.newDom, 'xml')
		
		if copyattachments:
			self.stagingid = get_named_child_value (get_named_child_element (self.newDom, self.xml, 'item'), 'staging').strip ()
		
		self.uuid = get_named_child_element (self.newDom, self.xml, 'item').attributes ['id'].value
		self.version = get_named_child_element (self.newDom, self.xml, 'item').attributes ['version'].value
		
		# remove old version references to non-existent start-pages
		if newversion and not copyattachments:
			attachmentsNode = get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments')
			while attachmentsNode.hasChildNodes ():
				attachmentsNode.removeChild (attachmentsNode.firstChild)
			

	# Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# Will happily accept dictionaries to create nested nodes, and array to create multiple nodes.
	# Will also accept any nested combination of the above!
	# e.g.
	#item.createNode ('item/management', {
	#    'general': {
	#	    'catalogue.entry': [
	#		    {'catalogue': 'URL', 'entry': 'fdsfs'},
	#		    {'catalogue': 'ISBN', 'entry': 'fdfs'},
	#		],
	#	    'title': {'title.main': 'Title'},
	#	    'description': 'Description',
	#	    'keyword': ['a', 'b', 'c'],
	#	    'resource.type': 'xxxx',
	#	    'folder': 'xxxx',
	#	},
	#    'meta.metadata': {
	#	    'contribute': {'role': 'Creator', 'entity': 'xxxxxx', 'date': 'yyyyyy'},
	#	    'security': 'jjjjjj',
	#	},
	# })
	# This will automatically create parent nodes as required.
	def createNode (self, xpath, value):
		self.removeNode(xpath)
		cur = self.xml
		elements = xpath.split ('/')
		elements, last = elements [:-1], elements [-1]
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		for item in create_actual_node (self.newDom, last, value):
			cur.appendChild (item)

	def getUUID (self):
		return self.uuid
		
	def getVersion (self):
		return self.version
		
	def getItemdefUUID (self):
		return get_named_child_element (self.newDom, self.xml, 'item').attributes ['itemdefid'].value
		
	def createNewVersion (self, copyattachments=1):
		return self.parClient.createNewVersion (self.getUUID (), self.getVersion (), self.getItemdefUUID (), ('false', 'true') [copyattachments])
		
	# Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.setNode ('item/description', 'This item describes ....')
	# To create multiple or complexnodes, use the createNode method.
	# This will automatically create parent nodes as required.
	def setNode (self, xpath, value):
		cur = self.xml
		elements = xpath.split ('/')
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		if attr:
			cur.setAttribute (attr, value)
		else:
			for child in cur.childNodes:
				cur.removeChild (child)
			cur.appendChild (self.newDom.createTextNode (unicode (value)))
	
	def createNodeFromNode (self, xpath, node):
		cur = self.xml
		elements = xpath.split ('/')
		for element in elements:
			cur = get_named_child_element (self.newDom, cur, element)
		for child in node.childNodes:
			cur.appendChild (child.cloneNode (deep=1))
	
	# Get an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.getNode ('item/description', 'This item describes ....')
	def getNodes (self, xpath):
		cur = self.xml
		elements = xpath.split ('/')
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
		curNodes = [cur]
		for element in elements:
			newCur = []
			for cur in curNodes:
				newCur += get_named_child_elements (self.newDom, cur, element)
			curNodes = newCur
		if attr:
			return [cur.getAttribute (attr) for cur in curNodes]
		else:
			return [value_as_string (cur) for cur in curNodes]
			
	# Get an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.getNode ('item/description', 'This item describes ....')
	def getNode (self, xpath):
		return self.getNodes (xpath) [0]
		
	def removeNode (self, xpath): 
		cur = self.xml 
		elements = xpath.split ('/') 
		if elements [-1] [:1] == '@': 
			raise "Can't just remove an attribute." 
		for element in elements [:-1]: 
			cur = get_named_child_element (self.newDom, cur, element) 
		for child in cur.childNodes: 
			if child.nodeName == elements [-1]: 
				cur.removeChild (child)

	# Upload a file as an attachment to this item. path is where the item will live inside of the repository, and should not contain a preceding slash.
	# e.g. item.attachFile ('support/song.wav', file ('c:\\Documents and Settings\\adame\\Desktop\\song.wav', 'rb'))
	# Parent directories are automatically created as required.
	def attachFile (self, path, file, showstatus=None):
		self.parClient._uploadFile (self.stagingid, path, '', 'true')
		encoded = b2a_base64 (file.read ()) [:-1]
		while len (encoded):
			if showstatus:
				print '%s%i remaining...' % (showstatus, len (encoded))
			send = encoded [:(1024 * 1024)]
			encoded = encoded [len (send):]
			self.parClient._uploadFile (self.stagingid, path, send, 'false')
			
	def unzipFile (self, path, name):
		self.parClient._unzipFile (self.stagingid, path, name)

	# Uploads an IMS/SCORM package
	def attachIMS (self, file, filename='package.zip', title='', showstatus=None):
		imsfilename = '_IMS/' + filename
		self.attachFile (imsfilename, file, showstatus)
		self.parClient._unzipFile (self.stagingid, imsfilename, filename)
		package = self.newDom.createElement ('packagefile')
		package.setAttribute ('name', title)
		package.setAttribute ('stored', 'true')
		package.appendChild (self.newDom.createTextNode (filename))
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'itembody').appendChild (package)
	
	# Mark an attached file as a start page to appear on the item summary page.
	# e.g. item.addStartPage ('Great song!', 'support/song.wav')
	def addStartPage (self, description, path, size=1024):
		attachment = self.newDom.createElement ('attachment')
		file = self.newDom.createElement ('file')
		desc = self.newDom.createElement ('description')
		sizeNode = self.newDom.createElement ('size')
		conversion = self.newDom.createElement ('conversion')
		attachment.setAttribute ('type', 'local')
		file.appendChild (self.newDom.createTextNode (path))
		desc.appendChild (self.newDom.createTextNode (description))
		sizeNode.appendChild (self.newDom.createTextNode (str (size)))
		conversion.appendChild (self.newDom.createTextNode ('true'))
		attachment.appendChild (file)
		attachment.appendChild (desc)
		attachment.appendChild (sizeNode)
		attachment.appendChild (conversion)
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments').appendChild (attachment)
	
	def addCollaborativeOwner (self, ownerid):
		collab = self.newDom.createElement ('collaborator')
		collab.appendChild (self.newDom.createTextNode (ownerid))

		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'collaborativeowners').appendChild (collab)
		
	
	def deleteAttachments(self):
		self.removeNode('item/attachments')
		self.parClient._deleteAttachmentFile(self.stagingid,"")
	
	# Add a URL as a resource to this item.
	# e.g. item.addUrl ('Interesting link', 'http://www.thelearningedge.com.au/')
	def addUrl (self, description, url):
		attachment = self.newDom.createElement ('attachment')
		file = self.newDom.createElement ('file')
		desc = self.newDom.createElement ('description')
		attachment.setAttribute ('type', 'remote')
		file.appendChild (self.newDom.createTextNode (url))
		desc.appendChild (self.newDom.createTextNode (description))
		attachment.appendChild (file)
		attachment.appendChild (desc)
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments').appendChild (attachment)

	# Print tabbed XML for this item, useful for debugging.
	def printXml (self):
		print ASCII_ENC (self.newDom.toprettyxml (), 'xmlcharrefreplace') [0]

	def forceUnlock(self):
		self.parClient._forceUnlock(self.getUUID (), self.getVersion (), self.getItemdefUUID ())

	def cancelEdit(self):
		self.parClient._cancelEdit(self.getUUID (), self.getVersion (), self.getItemdefUUID ())
	
	def delete (self):
		self.parClient._deleteItem(self.getUUID (), self.getVersion (), self.getItemdefUUID ())

	# Save this item into the repository.
	# e.g. item.submit ()
	def submit (self, workflow=1):
		self.parClient._stopEdit (self.newDom.toxml (), ('false', 'true') [workflow])

class MockClient:
	def __init__ (self, host, username, password, context, port):
		pass
		
	def getItemdefUUID (self, name):
		return ''
		
	def createNewItem (self, itemdef):
		return MockItem ()
	
class MockItem:
	def createNode (self, name, value):
		pass
		
	def attachFile (self, name, path):
		pass
		
	def addStartPage (self, description, path, size=1024):
		pass
		
	def printXml (self):
		pass
		
	def submit (self):
		pass
		
	def createNewVersion (self, copyattachments=1):
		return MockItem ()
	
	def setNode (self, xpath, value):
		pass
		
	def addUrl (self, description, url):
		pass
