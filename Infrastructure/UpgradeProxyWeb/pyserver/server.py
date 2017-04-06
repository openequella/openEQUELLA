#!/usr/bin/python

import random, re, os, os.path, BaseHTTPServer, cgi, threading, datetime, smtplib, StringIO, md5, MimeWriter
import zippy
import sys
from xml.dom import minidom
from py2xml import py2xml
import urllib

sys.stderr = file ('server.err', 'w')
sys.stdout = file ('server.out', 'w')

print "----- STARTING -----------------------------------------------"
print "Current working directory is %s" % os.getcwd()

BASE = '/mnt/build'
PORT = 5252
RE_PRODUCT_VERSION_REVISION_DISPLAY = re.compile (r'(?P<product>[^-]+)-upgrade-(?P<version>\d+\.\d+)\.r(?P<revision>[0-9]+) \((?P<displayname>[^)]*)')
RE_BUILD_VERSION = re.compile (r'Build-[^r]*r([0-9]+)')

def escape (s):
	return s.replace ('&', '&amp;').replace ('<', '&lt;').replace ('>', '&gt;')


def SendEmail (toAddr, fromAddr, subject, msg):
	message = StringIO.StringIO ()
	writer = MimeWriter.MimeWriter (message)
	writer.addheader ('Subject', subject)
	writer.addheader ('From', fromAddr)
	writer.addheader ('To', ','.join (toAddr))
	writer.startmultipartbody ('mixed')
	
	part = writer.nextpart ()
	body = part.startbody ('text/html')
	body.write (msg)
	writer.lastpart ()
	
	try:
		s = smtplib.SMTP (host='mail.equella.com.au', port=25)
		s.login(user='support', password='Tle123$')
		s.sendmail (fromAddr, toAddr, message.getvalue ())
		s.close ()
	except:
		pass # who cares?

def ListProperUpgrades (product, version, revision):
	dir = os.path.join (BASE, "Releases", version)
	if os.path.exists (dir):
		# get build dirs where revision is higher than current	
		dirs = [x for x in os.listdir (dir) if x.startswith ('Build-') and int (RE_BUILD_VERSION.findall(x) [0]) > revision ]
		filenames = []
		for y in dirs:
			updir = os.path.join(dir, y)
			filenames.append(os.listdir(updir)[0])
		filenames = [x for x in filenames if x.startswith('tle-upgrade-') and int (RE_PRODUCT_VERSION_REVISION_DISPLAY.search(x).group('revision'))  > revision ]
		filenames.sort (lambda a, b: cmp (b, a)) # may need tweaking
		return filenames
	else:
		return []

	
def ListOverrideUpgrades (product, version, revision, user):
	dir = os.path.join (BASE, 'Override', user)
	if os.path.exists (dir):
		filenames = [x for x in os.listdir (dir) if x.startswith (product + '-upgrade-'+ version) and int (RE_PRODUCT_VERSION_REVISION_DISPLAY.search(x).group('revision'))  > revision]
		filenames.sort (lambda a, b: cmp (b, a))
		return filenames
	else:
		return []
	

def ListUpgrades (product, version, revision, displayname, user, old):
	all_files = ListOverrideUpgrades (product, version, revision, user) + ListProperUpgrades (product, version, revision)
	all_files.sort (lambda a, b: cmp (b, a))
	if old in all_files or not len (all_files):
		return []
	elif len (all_files):
		return [all_files [0]]


def FindPathForFile (product, version, revision, displayname, user):
	first = os.path.join (BASE, "Releases", version, 'Build-%s.r%i (%s)' % (version, revision, displayname))
	second = os.path.join (BASE, "Releases", version, 'Build-%s.r%i (%s) QA' % (version, revision, displayname))
	third = os.path.join (BASE, "Override", user)

	for path in [first, second, third]:
		if os.path.exists(path):
			test = os.path.join (path, '%s-upgrade-%s.r%i (%s).zip' % (product, version, revision, displayname))
			if os.path.join(test):
				return test
	return ''




class RequestHandler (BaseHTTPServer.BaseHTTPRequestHandler):

	def send_failure_header (self, code=404):
		self.send_error (code, "Unlucky")
		self.end_headers ()
	
	def send_success_header (self, type='text/html', length=None):
		self.send_response (200)
		self.send_header ("Content-Type", type)
		if length != None:
			self.send_header ("Content-Length", str (length))
		self.send_header ("Cache-Control", 'no-cache')
		self.send_header ("Pragma", 'no-cache')
		self.end_headers ()
		
	def send_redirect (self, dest='/upgrade.do'):
		self.send_response (302)
		self.send_header ("Location", dest)
		self.end_headers ()
	
	def form_value (self, name, default=None):
		if self.form.has_key (name):
			vals = self.form [name]
			if len (vals) < 1:
				return default
			else:
				return vals [0]
		else:
			return default
	
	def authenticate (self):
		username = self.form_value ('username', '')
		password = self.form_value ('password', '')
		
		xmldoc = minidom.parse ('passwords/web.config')
		users = xmldoc.getElementsByTagName ('user')
		for user in users:
			if user.attributes ['name'].value == username:
				hash = md5.new ()
				hash.update (user.attributes ['password'].value)
				if hash.hexdigest () == password:
					return 1
				else:
					if user.attributes ['password'].value == password:
						return 1
					else:
						print "Authentication failed: password incorrect for user %s" % username
						return 0

		print "Authentication failed: Username not defined: %s" % username
		return 0

	


	def handle_upgrade (self):

		action = self.form_value ('action', '')
		old = self.form_value ('old', '')
		new = self.form_value ('new', '')
		username = self.form_value ('username', '')

		product, version, revision, displayname = RE_PRODUCT_VERSION_REVISION_DISPLAY.findall(old) [0]
		revision = int (revision)
		
		if action == 'list':
			self.send_success_header (type="text/plain")
			self.wfile.write ('\r\n'.join (ListUpgrades (product, version, revision, displayname, username, old)))
		elif action == 'notes':
			self.wfile.write ('Please view the product release notes at http://support.thelearningedge.com.au/')
		elif action == 'get':
			new_product, new_version, new_revision, new_displayname = RE_PRODUCT_VERSION_REVISION_DISPLAY.findall(new) [0]
			new_revision = int (new_revision)
						
			old = FindPathForFile (product, version, revision, displayname, username)
			new = FindPathForFile (new_product, new_version, new_revision, new_displayname, username)
			
			temp_upgrade = os.tmpnam ()
			zippy.create_upgrade (old, new, temp_upgrade)
			
			f = file (temp_upgrade, 'rb')
			data = f.read ()
			f.close ()
			
			os.remove (temp_upgrade)
			
			self.send_success_header (type="application/zip", length=len (data))
			self.wfile.write (data)
			
			html = '''
				<html>
					<body>
						%(username)s successfully upgraded from %(old)s to %(new)s.
					</body>
				</html>
			'''
			
			SendEmail (['tim.rattle@thelearningedge.com.au',], 'tim.rattle@thelearningedge.com.au', '[autoupgrade] TLE Upgraded', html % self.form)


		
	def dispatch_to_method (self):
		if not (self.path.find ('.') < 0) and self.path.find ('.do') < 0:
			raise ValueError, self.path		
		else:
			method_name = 'handle_' + ''.join ([x for x in self.path [1:self.path.find ('.do')] if x in 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_'])
			if hasattr (self, method_name):
				method = getattr (self, method_name)
	
				try:
					#GLOBAL_LOCK.acquire ()
					if self.authenticate ():
						method ()
					else:
						self.send_failure_header (code=401)
				finally:
					#GLOBAL_LOCK.release ()
					sys.stderr.flush ()
					sys.stdout.flush ()
					pass
			else:
				self.send_redirect ()
	
	def do_GET (self):
		idx = self.path.find ('?')
		if idx < 0:
			self.form = {}
		else:
			self.form = cgi.parse_qs (self.path [idx + 1:])
		self.dispatch_to_method ()
		
	def do_POST (self):
		data = self.rfile.read (int (self.headers.getheader ('Content-Length')))
		self.form = cgi.parse_qs (data)
		self.dispatch_to_method ()
			

GLOBAL_LOCK = threading.Semaphore ()
BaseHTTPServer.HTTPServer (('', PORT), RequestHandler).serve_forever ()	
