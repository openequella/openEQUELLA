#~ 1) On line 50 set the server name, at line 54 set hostname, institution, sharedsecret and id (from user management / shared secret)
#~ 2) From cmd c:\python25\python.exe impersonate_generic.py (or F5 from SciTE)
#~ - this will set up a webserver on your machine on port 8000 for the impersonation
#~ 3) Start up a browser session and go to: http://localhost:8000/
#~ 4) You should get a short HTML page with a box and a button. Type the username in the box and press the button.
#~ 5) A hyperlink should appear saying "log in as <username>". Click it.
#~ 6) When you have finished, just close the DOS session and the impersonatron will go away...

import md5
from binascii import b2a_base64
from urllib import urlencode
import time
import BaseHTTPServer
import sys
import cgi
from tleclient30 import createSSOToken

def md5this (s):
	m = md5.new ()
	m.update (s)
	return m.digest ()

class OurHandler (BaseHTTPServer.BaseHTTPRequestHandler):
	def do_GET(self):
		self.send_response (200)
		self.send_header("Content-type", "text/html")
		self.end_headers ()
		try:
			# redirect stdout to client
			stdout = sys.stdout
			sys.stdout = self.wfile
			self.makepage ()
		finally:
			sys.stdout = stdout # restore
	
	def makepage(self):
		idx = self.path.find ('?')
		username = None
		if idx > 0:
			qs = self.path [idx + 1:]
			vals = cgi.parse_qs (qs)
			if vals.has_key ('username'):
				usernames = vals ['username']
				if len (usernames):
					username = usernames [0]
		
		print "<html>"
		print "<body>"
		print "<form action='/' method='get'>"
		print "Username for <server>: <input type='text' name='username' />"
		print "<input type='Submit' value='impersonate' />"
		print "</form>"
		if username:
			print '<a href="%s" target="_blank">Log in as %s</a>' % ('<host>/<institution>/access/Tasks.jsp?%s' % urlencode ({'token': createSSOToken (username, '<sharedsecret>', '<id>')}), username)
		print "</body>"
		print "</html>"

httpd = BaseHTTPServer.HTTPServer(('', 8000), OurHandler)
httpd.serve_forever()

