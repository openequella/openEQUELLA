import codecs
import os
import os.path
import time
import hashlib
import urllib
import binascii
from xml.dom import Node

ASCII_ENC = codecs.getencoder('us-ascii')

SOAP_HEADER_TOKEN = '''<s:Header>
	<equella><token>%(token)s</token></equella>
</s:Header>'''

SOAP_REQUEST = '''<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:se="http://schemas.xmlsoap.org/soap/encoding/" se:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	%(header)s
	<s:Body>
		<ns1:%(method)s xmlns:ns1="%(ns)s">
			%(params)s
		</ns1:%(method)s>
	</s:Body>
</s:Envelope>'''

SOAP_PARAMETER = '<ns1:%(name)s xsi:type="%(type)s"%(arrayType)s>%(value)s</ns1:%(name)s>'


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
	
def value_as_string_array (node):
	node.normalize ()
	return [x.firstChild.nodeValue for x in node.childNodes]
	
def get_named_child_value (node, name):
	return value_as_string (node.getElementsByTagName (name) [0])

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

def get_named_child_elements (dom, node, name, create=True):
	matches = [x for x in node.childNodes if x.nodeType == Node.ELEMENT_NODE and x.tagName == name]
	if len (matches):
		return matches
	elif create:
		child = dom.createElement (name)
		node.appendChild (child)
		return [child]
	else:
		return []
		
def get_named_child_element (dom, node, name, create=True):
	return get_named_child_elements (dom, node, name, create) [0]

def value_as_node_or_string (cur):
	cur.normalize ()
	if len (cur.childNodes) == 1:
		if cur.firstChild.nodeType == cur.TEXT_NODE:
			return value_as_string (cur)
	# Empty node
	return ''


def generate_soap_envelope (name, params, ns, token=None):
	# Need to handle arrays
	def p(value):
		if isinstance(value, list):
			buf = ''
			for i in value:
				buf += '<input>'+ escape (clean_unicode (i)) + '</input>'
			return buf
		else:
			return escape (clean_unicode (value))
	def arrayType(value, type):
		if isinstance(value, list):
			return ' ns1:arrayType="%s[%s]"' % (type, len(value))
		else:
			return ''
	def t(value, type):
		if isinstance(value, list):
			return 'ns1:Array'
		else:
			return type

	return SOAP_REQUEST % {
		'ns': ns,
		'method': name,
		'params': '' if len(params) == 0 else ''.join([SOAP_PARAMETER % {
				'name': 'in' + str(i),
				'type': t(v[2], v[1]),
				'value': p(v[2]),
				'arrayType': arrayType(v[2], v[1])
			} for i, v in enumerate(params)]),
		'header': '' if token == None or len(token) == 0 else SOAP_HEADER_TOKEN % {'token': token}
	}

def urlEncode(text):
	return urllib.urlencode ({'q': text}) [2:]

def generateToken(username, sharedSecretId, sharedSecretValue):
	seed = str (int (time.time ())) + '000'
	id2 = urlEncode (sharedSecretId)
	if(not(sharedSecretId == '')):
		id2 += ':'

	return '%s:%s%s:%s' % (
			urlEncode(username),
			id2,
			seed,
			binascii.b2a_base64(hashlib.md5(
				username + sharedSecretId + seed + sharedSecretValue
			).digest())
		)
