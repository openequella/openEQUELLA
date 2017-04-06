import urllib
import urllib2
import json

# This is a thin wrapper around the EQUELLA REST API
# 
# Some sample functions are included below, others can be easily added by creating a call to '_call'.

OAUTH_TOKEN_URL='oauth/access_token'

class EquellaRest:
	def __init__ (self, institutionUrl, oauthClientId, oauthClientSecret, proxyUrl=None):
		if proxyUrl is not None:
			proxy= urllib2.ProxyHandler( { 'http' : proxyUrl } )
			opener = urllib2.build_opener( proxy )
			urllib2.install_opener(opener)
		self._institutionUrl = institutionUrl
		args = {'redirect_uri': 'default', 'response_type': 'token'}
		args['grant_type'] = 'client_credentials'
		args['client_id'] = oauthClientId
		args['client_secret'] = oauthClientSecret
		encoded_args = urllib.urlencode(args)
		url = '%s%s?%s' % (self._institutionUrl, OAUTH_TOKEN_URL, encoded_args)
		response = urllib2.urlopen(url)
		data = json.loads(response.read())
		self._oauthToken = data['access_token']
		
		
	# path - Required - The REST api path, eg 'api/search'
	# args - Optional map - This will get sent as a query string, eg {'q' : 'query'} becomes ?q=query
	# data - Optional POST data. - Usually JSON text or a file handle.
	# isJson - Optional boolean - Specifies whether the post data is in json format, defaults to True  
	# contentType - Optional string - Change the content type of the data, defaults to 'application/json'
	# method - Optional string - Specify the HTTP VERB, e.g POST, PUT, DELETE. Defaults to GET
	# forceRedirect - Optional boolean - If the Response has a Location header, then follow it and return the result. Defaults to False
	# contentLength - Optional long - Specify the content length, eg when sending a file.  Defaults to None
	def _call (self, path, args = {}, data=None,  isJson=True,  contentType='application/json',  method=None,  forceRedirect=False,  contentLength=None):
		headers = { 'X-Authorization' : 'access_token=%s' % (self._oauthToken), 
							'Accept': 'application/json', 
							'Content-Type': contentType
						}
		if contentLength is not None:
			headers['Content-Length'] = '%d' % contentLength
			
		encoded_args = urllib.urlencode(args, True)
		if len(encoded_args) > 0:
			url = u'%s%s?%s' % (self._institutionUrl, path, encoded_args)
		else:
			url = u'%s%s' % (self._institutionUrl, path)
		
		if isJson:
			if data is not None:
				postData = json.dumps(data)
			else:
				postData = None
		else:
			postData = data
			
		req = urllib2.Request(url, postData, headers)
		if method is not None:
			req.get_method = lambda: method
		
		response = urllib2.urlopen(req)
		if forceRedirect is True:
			url = response.headers['Location']
			req = urllib2.Request(url, '', headers)
			req.get_method = lambda: 'GET'
			response = urllib2.urlopen(req)
			
		contentType = response.headers['Content-Type']
		if contentType is not None and contentType.startswith('application/json'):
			responseData = response.read().encode('utf-8')
			if len(responseData) > 0:
				return json.loads(responseData, 'utf-8')
			else:
				return None
		else:
			responseData = response.read()
			return responseData

	def search (self, query='', collections = {}, start = 0, length = 10, order = '', reverse = 'false', where = '', showAll = 'false', info = ''):
		args={'q' : query,
			'collections' : collections,
			'start' : start,
			'length' : length,
			'order' : order,
			'reverse' : reverse,
			'where' : where,
			'showAll' : showAll,
			'info' : info
			}
		return self._call('api/search', args,  method='GET')

	def getItem (self, uuid, version, info=''):
		args={'info' : info }
		return self._call('api/item/%s/%s/' %(uuid, version), args,  method='GET')
		
	def newItem (self, item, draft = False, staging=None):
		args={'draft' : str(draft).lower()}
		if staging is not None:
			args['file'] = staging
		return self._call(path = 'api/item', args = args, data = item,  method='POST',  forceRedirect=True)
	
	def editItem (self, uuid,  version,   item,  submit=True,  staging=None):
		args={'submit' : str(submit).lower()}
		if staging is not None:
			args['file'] = staging
		return self._call(path = 'api/item/%s/%s/' % (uuid, version), args=args,  data = item,  method='PUT')
		
	def createStaging (self):
		return self._call(path = 'api/file',  method='POST',  forceRedirect=True)
		
	def uploadFile (self,  staging, name,  localFile,  fileSize):
		args={'append': 'false'}
		return self._call(path = 'api/file/%s/content/%s' % (staging,  name),  args=args,  data=localFile,  method='PUT', isJson=False,   contentType='application/octet-stream',  contentLength=fileSize)
