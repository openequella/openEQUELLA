from xml.dom.minidom import parse, parseString
from binascii import b2a_base64
from util import *
import urllib2
import uuid


HTTP_HEADERS = {
'Content-type': 'text/xml', 
'SOAPAction': '', 
'Accept':'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8', 'User-Agent':'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.6) Gecko/2009011913 Firefox/3.0.6',
'Accept-Language':'en-us,en;q=0.5',
'Accept-Charset':'ISO-8859-1,utf-8;q=0.7,*;q=0.7',
'Keep-Alive':'300'
}

STANDARD_INTERFACE = {'url':'services/SoapService51', 'namespace':'http://soap.remoting.web.tle.com'}
TAXONOMY_INTERFACE = {'url':'services/taxonomyTerm.service', 'namespace':'http://taxonomy.core.tle.com'}
COURSE_INTERFACE = {'url':'services/calcourses.service', 'namespace':'http://soap.remoting.web.tle.com'} #Not CAL?
ACTIVATION_INTERFACE = {'url':'services/calactivation.service', 'namespace':'http://service.web.cal.tle.com'}

STRING = 'xsd:string'
INT = 'xsd:int'
BOOLEAN = 'xsd:boolean'


# Class designed to make communicating with Equella easier!
class EquellaSoap:
	
	# First, instantiate an instance of this class, then invoke the methods on the object
	# e,g:
	#
	# client = EquellaSoap ('http://yourinstitution.edu.au/inst1/', 'admin', 'youradminpasssword')
	# results = client.searchItems('test')
	#
	def __init__ (self, institutionUrl, username, password, proxyUrl=None):
		if proxyUrl is not None:
			self._proxies = { 'servertype' : proxyUrl }
		else:
			self._proxies = None
			
		self._proxyHandler = urllib2.ProxyHandler( self._proxies )
		self._conn = urllib2.build_opener( self._proxyHandler )
		self._institutionUrl = institutionUrl
		#self._ns = 'http://soap.remoting.web.tle.com'
		self._loggedIn = False
		self._cookie = []
		self._loginWithTokenNextCall = None
		
		if username is not None and password is not None:
			self.login(username, password)
	
	def _call (self, name, args, returns=1, interface = STANDARD_INTERFACE):
		
		token = self._loginWithTokenNextCall
		self._loginWithTokenNextCall = None
		
		url = '%s%s' % (self._institutionUrl, interface['url'])
		requestContent = generate_soap_envelope (name, args, interface['namespace'], token)
		responseContent = ''
		
		try:
			response = self._conn.open( urllib2.Request(url, data=requestContent, headers=self._getHeaders()) )
			responseInfo = response.info()
			headers = {}
			cookie = responseInfo.getheader ('set-cookie')
			if cookie is not None:
				for cookie_part in cookie.split(','):
				     for cookie_name in cookie_part.split(','):
				        if not cookie_name.upper().split("=")[0].strip() in ["PATH", "DOMAIN", "EXPIRES", "SECURE", "HTTPONLY"]:
				            # save cookie
				            self._cookie.append( cookie_name )
			
			responseContent = response.read ()
			
		except urllib2.HTTPError, h:
			#print 'A HTTP error occurred.  The request content was: ', requestContent
			soapResponse = parseString( h.read() )
			faults = soapResponse.getElementsByTagName ('faultstring')
			faultsArray = [x.firstChild.data for x in faults]
			raise Exception, 'Server returned: (%i) \n*******************\n %s \n*******************' % (h.code, faultsArray)
		
		except urllib2.URLError, e:
			#print 'A connection error occurred.  Could not connect to %s (%s)' % (url, e.reason[1])
			errMsg = 'Connection error: \n*******************\n %s \n*******************\n' % e.reason[1]
			raise Exception, errMsg
			
		finally:
			self._conn.close ()
		
		try:
			dom = parseString( responseContent )
		except:
			raise Exception, 'Cannot parse server response as XML: %s' % responseContent
			
		if len (dom.getElementsByTagNameNS ('http://schemas.xmlsoap.org/soap/envelope/', 'Fault')):
			raise Exception, 'Server returned following SOAP error: %s' % dom.toxml ()
		elif returns: # then return a result, otherwise do nothing
			return dom.getElementsByTagNameNS('http://schemas.xmlsoap.org/soap/envelope/', 'Body')[0].firstChild.firstChild;
	
	
	def _getHeaders (self):
		headers = {}
		headers.update (HTTP_HEADERS)
		if len(self._cookie) > 0:
		    headers['Cookie'] = "; ".join(self._cookie)
		return headers

	def loginWithTokenOnNextCall(self, token):
		self._loginWithTokenNextCall = token
	
	def loginWithToken (self, token):
		result = self._call ('loginWithToken', (
				('token', STRING, token),
			))
		self._loggedIn = True
		return value_as_string (result)
		
		
	def login (self, username, password):
		result = self._call ('login', (
				('username', STRING, username),
				('password', STRING, password),
			))
		self._loggedIn = True
		return value_as_string (result)
	
	
	def logout (self):
		self._call ('logout', ())
		self._loggedIn = False
		
		
	def _unzipFile (self, stagingId, zipfile, outpath):
		self._call ('unzipFile', (
				('stagingId', STRING, stagingId),
				('zipfile', STRING, zipfile),
				('outpath', STRING, outpath),
			), returns=0)
		
		
	def getContributableCollections (self):
		result = self._call ('getContributableCollections', ())
		return dict ([(get_named_child_value (itemdef, 'name'), {'uuid': get_named_child_value (itemdef, 'uuid')}) for itemdef in parseString (value_as_string (result)).getElementsByTagName ('itemdef')])


	def getCollection(self, collectionUuid):
		result = self._call ('getCollection', (('collectionUuid', STRING, collectionUuid),) )
		return XmlWrapper(value_as_string (result))
		
		
	def getSchema(self, schemaUuid):
		result = self._call ('getSchema', (('schemaUuid', STRING, schemaUuid),) )
		return XmlWrapper(value_as_string (result))
		

	def _newItem (self, collectionUuid):
		result = self._call ('newItem', (
				('collectionUuid', STRING, collectionUuid),
			))
		return parseString (value_as_string (result))
		
		
	def _newVersionItem (self, itemUuid, itemVersion, copyAttachments):
		result = self._call ('newVersionItem', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str(itemVersion)),
				('copyAttachments', BOOLEAN, str (copyAttachments))
			))
		return parseString (value_as_string (result))
	
		
	def _editItem (self, itemUuid, itemVersion, copyAttachments):
		result = self._call ('editItem', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str (itemVersion)),
				('copyattachments', BOOLEAN, str (copyAttachments))	
			)	)
		return parseString (value_as_string (result))
		
		
	def _unlock (self, itemUuid, itemVersion):
		result = self._call ('unlock', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str (itemVersion))
			), returns=0)
		return result
	
	
	def _saveItem (self, xml, submit):
		result = self._call ('saveItem', (
				('itemXML', STRING, xml),
				('bSubmit', BOOLEAN, submit),
			))
		return result
	
	
	def _cancelItemEdit (self, itemUuid, itemVersion):
		result = self._call ('cancelItemEdit', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str (itemVersion))
			))
		return result
		
		
	def _uploadFile (self, stagingId, filename, base64data, overwrite):
		result = self._call ('uploadFile', (
				('stagingId', STRING, stagingId),
				('filename', STRING, filename),
				('base64data', STRING, base64data),
				('overwrite', BOOLEAN, overwrite),
			))
		return result
		
		
	def _deleteFile (self, stagingId, filename):
		result = self._call ('deleteFile', (
				('stagingId', STRING, stagingId),
				('filename', STRING, filename),
			))
		return result
		
		
	def _deleteItem (self, itemUuid, itemVersion):
		result = self._call ('deleteItem', (
				('itemid', STRING, itemUuid),
				('version', INT, str (itemVersion))
			))
		return result
		
		
	def getItem (self, itemUuid, itemVersion, select=''):
		result = self._call ('getItem', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str (itemVersion)),
				('select', STRING, select),
			))
		return XmlWrapper(value_as_string(result))
	
	
	def queryCount (self, collectionUuids, where):
		result = self._call ('queryCount', (
				('collectionUuids', STRING, collectionUuids),
				('where', STRING, where),
			))
		return int(value_as_string(result))

	def facetCount(self, query, collectionUuids = [], where = '', facetXpaths = []):
		result = self._call ('facetCount', (
				('freetext', STRING, query),
				('collectionUuids', STRING, collectionUuids),
				('where', STRING, where),
				('facetXpaths', STRING, facetXpaths)
			))
		return XmlWrapper(value_as_string (result))

	def searchItems(self, query, collectionUuids = [], where = '', onlyLive = True, orderType = 0, reverseOrder = False, offset = 0, length = 10):
		result = self._call ('searchItems', (
				('freetext', STRING, query),
				('collectionUuids', STRING, collectionUuids),
				('where', STRING, where),
				('onlyLive', BOOLEAN, str(onlyLive)),
				('orderType', INT, str(orderType)),
				('reverseOrder', BOOLEAN, str(reverseOrder)),
				('offset', INT, str(offset)),
				('length', INT, str(length))
			))
		return XmlWrapper(value_as_string (result))
		
	# Check to see if a user exists (given a user's userName).  Returns boolean.  Local users only - not LDAP.
	def userNameExists(self,userName):
		return value_as_string(self._call('userNameExists', (
				('userName', STRING, userName),
			))) == 'true'

	def addUser (self, uuid, username, password, firstname, lastname, email):
		return value_as_string(self._call ('addUser', (
				('uuid', STRING, uuid),
				('name', STRING, username),
				('password', STRING, password),
				('first', STRING, firstname),
				('last', STRING, lastname),
				('email', STRING, email),
			)))
			
	def getUser (self, uuid):
		result = self._call ('getUser', (
				('uuid', STRING, uuid),
			))
		return XmlWrapper(value_as_string (result))

	def editUser (self, uuid, username, password, firstname, lastname, email):
		return value_as_string(self._call ('editUser', (
				('uuid', STRING, uuid),
				('name', STRING, username),
				('password', STRING, password),
				('first', STRING, firstname),
				('last', STRING, lastname),
				('email', STRING, email),
			)))
			

	def deleteUser (self, uuid):
		self._call ('deleteUser', (
				('uuid', STRING, uuid),
			))
			

	def addUserToGroup (self, uuid, groupId):
		self._call ('addUserToGroup', (
				('uuid', STRING, uuid),
				('groupid', STRING, groupId),
			))
			
			
	def removeUserFromGroup (self, uuid, groupId):
		self._call ('removeUserFromGroup', (
				('uuid', STRING, uuid),
				('groupid', STRING, groupId),
			))			


	def removeUserFromAllGroups (self, userId):
		self._call ('removeUserFromAllGroups', (
				('userUuid', STRING, userId),
			))			
			
			
	def isUserInGroup (self, userId, groupId):
		return value_as_string(self._call ('isUserInGroup', (
				('userUuid', STRING, userId),
				('groupUuid', STRING, groupId),
			))) == 'true'


	# Check to see if a user exists (given a user's UUID).  Returns boolean.  Local users only - not LDAP.
	def userExists (self, userUUID):
		return value_as_string(self._call ('userExists', (
				('userUuid', STRING, userUUID),
			))) == 'true'
			
	
	# Check to see if a group (as given by group's UUID) exists.  Returns boolean.
	def groupExists (self, groupUUID):
		return value_as_string(self._call ('groupExists', (
				('groupUuid', STRING, groupUUID),
			))) == 'true'
	
	
	# Given a group name, returns the group's UUID
	def getGroupUuidForName (self, groupName):
		return value_as_string(self._call ('getGroupUuidForName', (
				('groupName', STRING, groupName),
			), ))
	
	
	# Given a group UUID and name, add a group
	def addGroup (self, groupUuid, groupName):
		self._call('addGroup', ( 
				('groupId', STRING, groupUuid),
				('groupName', STRING, groupName)
				) )
	
	
	# Given a group UUIUD, remove the group
	def deleteGroup (self, groupUuid):
		self._call('deleteGroup', (('groupId', STRING, groupUuid),) )
	
	
	# Given a group UUID, remove all users from a group.
	def removeAllUsersFromGroup(self, groupUuid):
		self._call('removeAllUsersFromGroup', (('groupId', STRING, groupUuid),) )
	
	
	def getTaskFilterNames (self):
		result = self._call ('getTaskFilterNames', ())
		return value_as_string_array(result)
		
	
	def getTaskList (self, filterName, start, numResults):
		result = self._call ('getTaskList', (
				('filtername', STRING, filterName),
				('start', INT, str(start)),
				('numresults', INT, str(numResults)),
			))
		return XmlWrapper(value_as_string (result))
	
	
	def archiveItem(self, itemUuid, itemVersion):
		self._call('archiveItem',
				( ('itemUuid', STRING, itemUuid),
				('itemVersion', INT, str(itemVersion)), )
			)


	# Create a new repository item of the type specified. See ItemEditingSession for methods that can be called on the return type.
	# e.g. item = client.newItem (itemdefUUID)
	def newItem (self, collectionUuid):
		rv = ItemEditingSession (self, self._newItem (collectionUuid))
		#rv.setNode ('item/@itemdefid', itemdefid)
		return rv
		
		
	# Edit particular item. See ItemEditingSession for methods that can be called on the return type.
	# e.g. item = client.editItem (itemdefUUID)
	def editItem (self, itemid, version, copyattachments):
		dom = self._editItem (itemid, version, copyattachments)
		if not len (dom.getElementsByTagName ('item')):
			self._unlock (itemid, version)
			dom = self._editItem (itemid, version, copyattachments)
		rv = ItemEditingSession (self, dom, newversion=0, copyattachments=(copyattachments == 'true'))
		return rv
		
		
	def newVersionItem(self, itemUuid, itemVersion, copyAttachments=True):
		return ItemEditingSession(self, self._newVersionItem(itemUuid, itemVersion, copyAttachments))
		
		
	
	# Return a collection UUID given a human displayable name.
	# e.g. collectionUUID = client.getCollectionUUID ('K-12 Educational Resource')
	def getCollectionUUID (self, itemdefName):
		return self.getContributableCollections() [itemdefName] ['uuid']
		
		
		
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	#Ownership functions
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	
	def setOwner(self, itemUuid, itemVersion, ownerUuid):
		self._call('setOwner', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, itemVersion),
				('ownerUuid', STRING, ownerUuid)
				) )
	
	def addSharedOwner(self, itemUuid, itemVersion, ownerUuid):
		self._call('addSharedOwner', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, itemVersion),
				('ownerUuid', STRING, ownerUuid)
				) )
				
	def removeSharedOwner(self, itemUuid, itemVersion, ownerUuid):
		self._call('removeSharedOwner', (
				('itemUuid', STRING, itemUuid),
				('itemVersion', INT, itemVersion),
				('ownerUuid', STRING, ownerUuid)
				) )
		
		
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	#Taxonomy functions
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	
	def deleteTerm(self, taxonomyUuid, termFullPath):
		self._call('deleteTerm',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('termFullPath', STRING, termFullPath),)
				, interface = TAXONOMY_INTERFACE
			)
	
	# Retrieve an stored data value for a key against a term. 
	
	def getTermData(self, taxonomyUuid, termFullPath, key):
		result = self._call('getData',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('termFullPath', STRING, termFullPath), 
				('dataKey', STRING, key),)
				, interface = TAXONOMY_INTERFACE
			)
		return value_as_string(result)
		
	# Insert a new term into the taxonomy.
	# This method requires an editing lock to have been aquired for the taxonomy. 
	
	def insertTerm(self, taxonomyUuid, parentFullPath, term, index=-1):
		self._call('insertTerm',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('parentFullPath', STRING, parentFullPath), 
				('term', STRING, term),
				('index', INT, str(index)),)
				, interface = TAXONOMY_INTERFACE
			)
		
	# List the child terms for a parent term. This will only list the immediate children of the parent, ie,
	# not grand-children, great grand-children, etc...
	
	def listTerms(self, taxonomyUuid, termFullPath):
		result = self._call('listTerms',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('parentFullPath', STRING, termFullPath), )
				, interface = TAXONOMY_INTERFACE
			)
		return value_as_string_array(result)
	
	# Before terms can be edited, this method must be invoked to aquire an editing lock on the taxonomy.
	# An error will be raised if the lock cannot be aquired, most likely because it has already been
	# locked by a different user.
	
	def lockTaxonomyForEditing(self, taxonomyUuid):
		self._call('lockTaxonomyForEditing',
				( ('taxonomyUuid', STRING, taxonomyUuid), )
				, interface=TAXONOMY_INTERFACE
			)
			
			
	# Moves a term to a (possibly) new parent term and child index. Children of the term are also moved.
	# The user should always remember that changing the lineage of a term will also change the lineage of
	# child terms, and depending on the number of terms that require modification, could be an expensive
	# operation. Leaving the term under the same parent term, but changing the index, does not change the
	# lineage of the term or its children.
	
	# This method requires an editing lock to have been aquired for the taxonomy.
	
	def moveTerm(self, taxonomyUuid, termToMove, index, newParent = None):
		self._call('move',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('termToMove', STRING, termToMove), 
				('newParent', STRING, newParent),
				('index', INT, str(index)),)
				, interface = TAXONOMY_INTERFACE
			)
	
	# Renames a term. The user should always remember that renaming a term will change the lineage of child terms,
	# and depending on the number of terms that require modification, could be an expensive operation.
	
	# This method requires an editing lock to have been aquired for the taxonomy. 
			
	def renameTermValue(self, taxonomyUuid, termToRename, newValue):
		self._call('renameTermValue',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('termToRename', STRING, termToRename), 
				('newValue', STRING, newValue),)
				, interface = TAXONOMY_INTERFACE
			)
		
	# Set an arbitrary data value for a key against a term.
	
	# This method requires an editing lock to have been aquired for the taxonomy. 
			
	def setTermData(self, taxonomyUuid, termFullPath, key, value):
		self._call('setData',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('termFullPath', STRING, termFullPath), 
				('dataKey', STRING, key),
				('dataValue', STRING, value),)
				, interface = TAXONOMY_INTERFACE
			)
	
	# Unlock a taxonomy that you previously aquired a lock on. You can also choose to forcefully unlock the taxonomy,
	# which will remove any existing lock, even if it has been made by another user. It is recommended that the
	# force option is used with care. 
	
	def unlockTaxonomy(self, taxonomyUuid, force=0):
		self._call('unlockTaxonomy',
				( ('taxonomyUuid', STRING, taxonomyUuid), 
				('force', BOOLEAN, str(force)), )
				, interface=TAXONOMY_INTERFACE
			)
		
		
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	#CAL functions
	#----------------------------------------------------------------------------------------------------------------------------------------------------------
	
	# @return An XmlWrapper object
	def getCourse(self, courseCode):
		result = self._call('getCourse',
				( ('courseCode', STRING, courseCode), )
				, interface=COURSE_INTERFACE
			)
		return XmlWrapper( value_as_string(result) )
		
		
	def addCourse(self, courseXml):
		self._call('addCourse', ( ('courseXml', STRING, courseXml), ) 
				, interface = COURSE_INTERFACE
			)
			
	
	def editCourse(self, courseXml):
		self._call('editCourse', ( ('courseXml', STRING, courseXml), ) 
				, interface = COURSE_INTERFACE
			)


	def bulkCourseImport(self, courseCsvData):
		self._call('bulkImport', ( ('csvText', STRING, courseCsvData), ) 
				, interface = COURSE_INTERFACE
			)
			
			
	def listCourseCodes(self):
		result = self._call('enumerateCourseCodes', ( ) 
				, interface = COURSE_INTERFACE
			)
		return value_as_string_array( result )
		
			
	def deleteCourse(self, courseCode):
		self._call('delete',
				( ('courseCode', STRING, courseCode), )
				, interface=COURSE_INTERFACE
			)
			
			
	def archiveCourse(self, courseCode):
		self._call('archiveCourse',
			( ('courseCode', STRING, courseCode), )
			, interface=COURSE_INTERFACE
			)
			
	def unarchiveCourse(self, courseCode):
		self._call('unarchiveCourse',
			( ('courseCode', STRING, courseCode), )
			, interface=COURSE_INTERFACE
			)
			
			
	# @param attachmentUuids An array of UUIDs
	def activateItemAttachments(self, itemUuid, itemVersion, courseCode, attachmentUuids):
		self._call('activateItemAttachments',
				( ('uuid', STRING, itemUuid), 
				('version', INT, str(itemVersion)), 
				('courseCode', STRING, courseCode), 
				('attachments', STRING, attachmentUuids), )
				, interface=ACTIVATION_INTERFACE
			)
			
			


class ItemEditingSession:
	def __init__ (self, parClient, newDom, newversion=0, copyattachments=1):
		self.parClient = parClient
		self.newDom = newDom
		self.xml = get_named_child_element (self.newDom, self.newDom, 'xml')
		self.prop = XmlWrapper(self.xml)
		
		if copyattachments:
			self.stagingid = get_named_child_value (get_named_child_element (self.newDom, self.xml, 'item'), 'staging').strip ()
		
		self.uuid = get_named_child_element (self.newDom, self.xml, 'item').attributes ['id'].value
		self.version = get_named_child_element (self.newDom, self.xml, 'item').attributes ['version'].value
		
		# remove old version references to non-existent start-pages
		if newversion and not copyattachments:
			attachmentsNode = get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments')
			while attachmentsNode.hasChildNodes ():
				attachmentsNode.removeChild (attachmentsNode.firstChild)
				

	def getUUID (self):
		return self.uuid
		
		
	def getVersion (self):
		return self.version
		
		
	def getCollectionUUID (self):
		return get_named_child_element (self.newDom, self.xml, 'item').attributes ['itemdefid'].value

	
	# Upload a file as an attachment to this item. path is where the item will live inside of the repository, and should not contain a preceding slash.
	# e.g. item.attachFile ('support/song.wav', file ('c:\\Documents and Settings\\adame\\Desktop\\song.wav', 'rb'))
	# Parent directories are automatically created as required.
	def uploadFile (self, path, file, showstatus=None):
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
		

	# Uploads an IMS package
	def uploadIMSAttachment (self, file, filename, title='', showstatus=None):
		imsfilename = '_IMS/' + filename
		self.uploadFile (imsfilename, file, showstatus)
		self.parClient._unzipFile (self.stagingid, imsfilename, filename)
		package = self.newDom.createElement ('packagefile')
		package.setAttribute ('name', title)
		package.setAttribute ('stored', 'true')
		package.appendChild (self.newDom.createTextNode (filename))
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'itembody').appendChild (package)
			
	# Uploads an SCORM package
	def uploadSCORMAttachment (self, file, filename, resourceDescription='',showstatus=None):
		imsfilename = '_SCORM/' + filename
		self.uploadFile (imsfilename, file, showstatus)
		self.parClient._unzipFile (self.stagingid, imsfilename, filename)
		attachmentUuid = uuid.uuid4()
		attachment = self.newDom.createElement('attachment')
		attachment.setAttribute ('type', 'custom')
		type = self.newDom.createElement('type')
		type.appendChild(self.newDom.createTextNode('scorm'))
		attachment.appendChild(type)
		theUuid = self.newDom.createElement('uuid')
		theUuid.appendChild(self.newDom.createTextNode(str(attachmentUuid)))
		attachment.appendChild(theUuid)
		fileNode = self.newDom.createElement('file')
		fileNode.appendChild (self.newDom.createTextNode (filename))
		attachment.appendChild(fileNode)
		description = self.newDom.createElement('description')
		description.appendChild(self.newDom.createTextNode(resourceDescription))
		attachment.appendChild(description)
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments').appendChild (attachment)
		
	
	# Mark an attached file as a start page to appear on the item summary page.
	# e.g. item.addAttachment ('Great song!', 'support/song.wav')
	def addAttachment (self, description, path, size=0):
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
		
		
	### It has only been tested on attachments with a type of "item".  It should also work fine on
	### attachments of type "custom" (for resource attachments).  It has been partially tested on local items, but should probably
	### have some code to actually delete the file ... which isn't in yet.
	
	def deleteAttachment(self, attachmentType, attachmentUuid):
		matches = [node for node in self.newDom.getElementsByTagName('attachment') if node.attributes["type"].value == attachmentType]
		for attachment in matches:
			for e in attachment.childNodes:
				if (e.nodeType == e.ELEMENT_NODE) and (e.localName == 'uuid') and (e.firstChild.nodeValue == attachmentUuid):
					for atts in self.newDom.getElementsByTagName('attachments'):
						if atts.hasChildNodes():
							for child in atts.childNodes:
								if attachment == child:
									atts.removeChild(child)
									
	
	def addResourceAttachment (self, resourceUuid, resourceVersion, resourceDescription, attachmentUuid=''):
		if attachmentUuid == '':
			attachmentUuid = uuid.uuid4()
		
		attachment = self.newDom.createElement('attachment')
		attachment.setAttribute ('type', 'custom')
		
		type = self.newDom.createElement('type')
		type.appendChild(self.newDom.createTextNode('resource'))
		attachment.appendChild(type)
		
		attributes = self.newDom.createElement('attributes')
				
		entry = self.newDom.createElement('entry')
		uuidString = self.newDom.createElement('string')
		uuidString.appendChild(self.newDom.createTextNode('uuid'))
		uuidValue = self.newDom.createElement('string')
		uuidValue.appendChild(self.newDom.createTextNode(str(resourceUuid)))
		entry.appendChild(uuidString)
		entry.appendChild(uuidValue)
		attributes.appendChild(entry)
		
		entry2 = self.newDom.createElement('entry')
		typeString = self.newDom.createElement('string')
		typeString.appendChild(self.newDom.createTextNode('type'))
		entry2.appendChild(typeString)
		pString = self.newDom.createElement('string')
		pString.appendChild(self.newDom.createTextNode('p'))
		entry2.appendChild(pString)
		attributes.appendChild(entry2)
		
		entry3 = self.newDom.createElement('entry')
		versionString = self.newDom.createElement('string')
		versionString.appendChild(self.newDom.createTextNode('version'))
		entry3.appendChild(versionString)
		rvString = self.newDom.createElement('int')
		rvString.appendChild(self.newDom.createTextNode(str(resourceVersion)))
		entry3.appendChild(rvString)
		attributes.appendChild(entry3)
		
		attachment.appendChild(attributes)
		
		theUuid = self.newDom.createElement('uuid')
		theUuid.appendChild(self.newDom.createTextNode(str(attachmentUuid)))
		attachment.appendChild(theUuid)
		
		file = self.newDom.createElement('file')
		attachment.appendChild(file)
		
		description = self.newDom.createElement('description')
		description.appendChild(self.newDom.createTextNode(resourceDescription))
		attachment.appendChild(description)
		
		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'attachments').appendChild (attachment)
		
	
	def addCollaborativeOwner (self, ownerid):
		collab = self.newDom.createElement ('collaborator')
		collab.appendChild (self.newDom.createTextNode (ownerid))

		get_named_child_element (self.newDom, get_named_child_element (self.newDom, self.xml, 'item'), 'collaborativeowners').appendChild (collab)
		
	
	def deleteAttachments(self):
		self.getXml().removeNode('item/attachments')
		self.parClient._deleteAttachmentFile(self.stagingid,'')
		
	
	# Add a URL as a resource to this item.
	# e.g. item.addUrlAttachment ('Interesting link', 'http://www.thelearningedge.com.au/')
	def addUrlAttachment (self, description, url):
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


	# Print tabbed XML for this item, useful for debugging.
	def toXml (self, encoding='ASCII'):
		if encoding=='UTF8':
			return self.newDom.toprettyxml (encoding='utf-8')
		else:
			return self.newDom.toprettyxml ()


	def unlock(self):
		self.parClient._unlock(self.getUUID (), self.getVersion ())
		

	def cancelItemEdit(self):
		self.parClient._cancelItemEdit(self.getUUID (), self.getVersion ())
	

	# Save this item into the repository.
	# e.g. item.submit ()
	#  if you use submit(0) = will force it to skip moderation.
	def submit (self, workflow=1):
		self.newDom =  parseString (value_as_string ( self.parClient._saveItem (self.newDom.toxml (), ('false', 'true') [workflow]) ))
		self.xml = get_named_child_element (self.newDom, self.newDom, 'xml')
		self.prop = XmlWrapper(self.xml)
		self.uuid = get_named_child_element (self.newDom, self.xml, 'item').attributes ['id'].value
		self.version = get_named_child_element (self.newDom, self.xml, 'item').attributes ['version'].value


	def getXml(self):
		return self.prop





# PropBag classes - was propbag.py
class XmlWrapper:
	'''
	>>> xml = XmlWrapper('<xml></xml>')
	>>> xml.createNode('test/@att2','blah2')
	>>> xml.getNode('test/@att2')
	'blah2'
	>>> xml.setNode('test/@att','blah')
	>>> xml.getNode('test/@att')
	'blah'
	>>> xml = XmlWrapper('<xml></xml>')
	>>> xml.removeNode('blah/not/exist')
	>>> xml.getNode('blah/not/exist')
	
	>>> node = 'hello'
	>>> xml.setNode(node,'test')
	>>> xml.getNode(node)
	u'test'
	>>> xml.removeNode(node)
	>>> xml.setNode(node,'')
	>>> xml.getNode(node)
	''
	>>> xml.removeNode(node)
	>>> xml.createNode('create/node1','test')
	>>> xml.createNode('create/node1','test2')
	>>> sub = xml.getSubtree('create')
	>>> print sub.getNode('node1')
	test
	>>> for n in xml.iterate('create/node1'):
	... 	print(n.getNode(''))
	... 	n.setNode('t','t') # Shouldn't error
	... 	n.removeNode('t')
	test
	test2
	>>> xml.removeNode('create/node1')
	>>> print xml.toXml() #doctest: +NORMALIZE_WHITESPACE
	<xml><create><node1>test2</node1></create></xml>
	>>> xml=XmlWrapper('<xml><item>test</item></xml>')
	>>> print xml.iterate('item')[0].toXml()
	<item>test</item>
	'''
	def __init__ (self, s):
		if isinstance (s, XmlWrapper) :
			self.document = s.document
			self.root = s.root
		elif isinstance (s, str) or isinstance(s, unicode):
			self.document = parseString (s)
			self.root = self.document.childNodes[0]
		elif isinstance (s, file) :
			self.document = parse (s)
			self.root = self.document.childNodes[0]
		else:
			self.document = s.ownerDocument
			self.root = s
	
	def createNodeFromNode (self, xpath, node):
		cur = self.root
		elements = xpath.split ('/')
		for element in elements:
			cur = get_named_child_element (self.document, cur, element, True)
		for child in node.childNodes:
			cur.appendChild (child.cloneNode (deep=1))

	def getNodes (self, xpath, string=True):
		elements = xpath.split ('/')
		cur = self.root
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
		curNodes = [cur]
		for element in elements:
			newCur = []
			for cur in curNodes:
				#Handles current node
				if element == '':
					newCur += [cur]
				else:
					newCur += get_named_child_elements (self.document, cur, element, False)
			curNodes = newCur
		if attr:
			return [cur.getAttribute (attr) for cur in curNodes]
		else:
			if string:
				return [value_as_node_or_string (cur) for cur in curNodes]
			else:
				return curNodes
	
	# Get an XML node on this item. xpath should begin with item, but should not have a preceding slash.
	# e.g. item.getNode ('item/description', 'This item describes ....')
	def getNode (self, xpath):
		nodes = self.getNodes (xpath)
		if len(nodes) > 0:
			return nodes[0]
		return None

	def getSubtree(self, xpath):
		return XmlWrapper(self.getNodes(xpath, string=False) [0])
	
	def newSubtree(self, xpath):
		cur = self.root
		elements = xpath.split ('/')
		elements, last = elements [:-1], elements [-1]
		for element in elements:
			cur = get_named_child_element (self.document, cur, element, True)
		child = self.document.createElement (last)
		cur.appendChild (child)
		return XmlWrapper(child)

	def removeNode (self, xpath): 
		cur = self.root 
		elements = xpath.split ('/') 
		if elements [-1] [:1] == '@': 
			raise "Can't just remove an attribute."
		for element in elements [:-1]: 
			cur = get_named_child_elements (self.document, cur, element, False) 
			if not cur:
				return
			cur = cur[0]
		
		child = cur.firstChild
		while child is not None:
			next = child.nextSibling
			if child.nodeName == elements [-1]:
				cur.removeChild (child)
			child = next

	def removeDOMNode(self, node):
		cur = self.root
		cur.removeChild (node.root)

	# Print tabbed XML for this item, useful for debugging.
	def printXml (self):
		print ASCII_ENC (self.root.toprettyxml (), 'xmlcharrefreplace') [0]
	
	def toXml (self, encoding='ASCII'):
		if encoding=='UTF8':
			return self.root.toxml (encoding='utf-8')
		else:
			return self.root.toxml ()
	
	def nodeCount(self, xpath):
		return len(self.getNodes(xpath))
	
	def createNode (self, xpath, value):
		'''
		Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
		Will happily accept dictionaries to create nested nodes, and array to create multiple nodes.
		Will also accept any nested combination of the above!
		e.g.
			item.createNode ('item/management', {
				'general': {
				'catalogue.entry': [
					{'catalogue': 'URL', 'entry': 'fdsfs'},
					{'catalogue': 'ISBN', 'entry': 'fdfs'},
				],
				'title': {'title.main': 'Title'},
				'description': 'Description',
				'keyword': ['a', 'b', 'c'],
				'resource.type': 'xxxx',
				'folder': 'xxxx',
			},
				'meta.metadata': {
				'contribute': {'role': 'Creator', 'entity': 'xxxxxx', 'date': 'yyyyyy'},
				'security': 'jjjjjj',
			},
			})
		This will automatically create parent nodes as required.
		'''
		self.setNode(xpath, value, createNew=True)
	
	def setNode (self, xpath, value, encodingType='latin1', createNew=False):
		'''
		Set an XML node on this item. xpath should begin with item, but should not have a preceding slash.
		e.g. item.setNode ('item/description', 'This item describes ....')
		To create multiple or complexnodes, use the createNode method.
		This will automatically create parent nodes as required.
		'''
		cur = self.root
		elements = xpath.split ('/')
		if elements [-1] [:1] == '@':
			elements, attr = elements [:-1], elements [-1] [1:]
		else:
			attr = None
			if createNew:
				elements, last = elements [:-1], elements [-1]
		
		for element in elements:
			cur = get_named_child_element (self.document, cur, element, True)
		if attr:
			cur.setAttribute (attr, value)
		elif createNew:
			for item in create_actual_node (self.document, last, value):
				cur.appendChild (item)
		else:
			for child in cur.childNodes:
				cur.removeChild (child)
			#~ cur.appendChild (self.document.createTextNode (unicode (value)))
			cur.appendChild (self.document.createTextNode (unicode(value.decode(encodingType))))
	
	def iterate(self,xpath):
		nodes = self.getNodes(xpath, string=False)
		return [XmlWrapper(x) for x in nodes]

	def nodeExists(self, xpath):
		return self.nodeCount(xpath) > 0


