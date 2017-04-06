# Copyright Dytech Solutions, 2007

# This module is provided 'commercial-in-confidence' and may not be reproduced nor redistributed without
# express written permission from the copyright holder.

# Author: Adam Eijdenberg, The Learning Edge International Pty Ltd <adam.eijdenberg@thelearningedge.com.au>

"""
To run:

python los.py --server=tle.testing.tafevc.com.au --port=80 --context=/toolbox/ --username=aeijdenberg --password=password --itemdef="Toolbox learning objects" --directory="..\LO by Toolbox"

"""

from __future__ import with_statement

from tleclient import TLEClient, MockClient

from sys import argv, exc_info
from os import listdir, stat, system, makedirs, walk
from os.path import join, normpath, realpath, split
import re, traceback, zipfile

import threading

from getpass import getpass
from optparse import OptionParser

import xml.etree.ElementTree as ET

def getToMetadata (path):
	zip = zipfile.ZipFile (path, 'r')
	metadataFileName = ET.fromstring (zip.read ('imsmanifest.xml')).find ('{http://www.imsproject.org/xsd/imscp_rootv1p1p2}metadata/{http://www.adlnet.org/xsd/adlcp_rootv1p2}location').text
	metadata = ET.fromstring (zip.read (metadataFileName))
	return metadata
	
def extractCodes (path):
	return [elem.text [:3] for elem in getToMetadata (path).findall ('{http://www.imsglobal.org/xsd/imsmd_rootv1p2p1}classification/{http://www.imsglobal.org/xsd/imsmd_rootv1p2p1}taxonpath/{http://www.imsglobal.org/xsd/imsmd_rootv1p2p1}taxon/{http://www.imsglobal.org/xsd/imsmd_rootv1p2p1}id') if elem.text]

def fullsplit (path):
	head = path
	tail = 'dummy'
	rv = []
	while head and tail:
		head, tail = split (head)
		rv += [tail]
	return rv [::-1]

def addFileToSet (s, paths):
	s.add ('\\'.join (paths))


class SafeList:
	def __init__ (self, s):
		self.l = list (s)
		self.lock = threading.RLock ()
		
	def next (self):
		with self.lock:
			if len (self.l):
				return self.l.pop ().split ('\\')
			else:
				raise StopIteration
		
	def __iter__ (self):
		return self
		


def processUpload (i, tle, rootdir, itemdefID, allItems, url, options):
	for relpath in allItems:
		try:
			fullpath = join (rootdir, join (*relpath))
							
			name = relpath [-1]
			
			print "%i: Creating item for %s..." % (i, name)

			
			item = tle.createNewItem (itemdefID)
			item.setNode ('item/toolbox/toolbox.code', relpath [0])
			item.setNode ('item/toolbox/source', 'toolbox')
			item.createNode ('item/toolbox/training.package', extractCodes (fullpath))
			
			finalurl = '%sitems/%s/1/' % (url, item.getUUID ())
	
			if options.nosubmit:
				item.printXml ()
			else:
				print "%i: Uploading %s" % (i, name)
				item.attachIMS (file (fullpath, 'rb'), name, name, showstatus='%i: ' % i)
				print "%i: Submitting %s" % (i, name)
				item.submit ()
				file ('%s.complete.txt' % fullpath, 'wb').write (finalurl)
				print "%i: Finished %s" % (i, name)
		except:
			traceback.print_exc ()

def main ():
	parser = OptionParser ("usage: %prog [options]\r\nsee: %prog --help")
	parser.add_option ("-s", "--server", dest="server", type="string", help="uploads data to SERVER", metavar="SERVER")
	parser.add_option ("-c", "--context", dest="context", type="string", help="uploads data to CONTEXT on server", metavar="CONTEXT", default='/')
	parser.add_option ("-p", "--port", dest="port", type="int", help="uploads data to PORT on server", metavar="PORT", default=80)
	parser.add_option ("-u", "--username", dest="username", type="string", help="upload data as USERNAME", metavar="USERNAME")
	parser.add_option ("-r", "--directory", dest="directory", type="string", help="root DIRECTORY", metavar="DIRECTORY")
	parser.add_option ("-w", "--password", dest="password", type="string", help="password PASSWORD", metavar="PASSWORD")
	parser.add_option ("-d", "--no-submit", dest="nosubmit", action="store_true", help="don't submit actual content")
	parser.add_option ("-i", "--itemdef", dest="itemdef", type="string", help="upload items under ITEMDEF name", metavar="ITEMDEF")

	options, args = parser.parse_args ()
	if len (args) != 0:
		parser.error ("incorrect number of arguments")
	if not options.server:
		parser.error ("Please specify a SERVER")
	if not options.username:
		parser.error ("Please specify a USERNAME")
	if not options.itemdef:
		parser.error ("Please specify an ITEMDEF name")
	if not options.directory:
		parser.error ("Please specify a root DIRECTORY")
		
	passw = options.password
	if not passw:
		passw = getpass ()

	tle = TLEClient (host=options.server, username=options.username, port=options.port, context=options.context, password=passw)
	
	url = 'http://%s:%i%s' % (options.server, options.port, options.context)
	
	itemdefID = tle.getItemdefUUID (options.itemdef)
	
	rootdir = realpath (options.directory)
	rootdirsplit = fullsplit (rootdir)
	print 'Processing directory: %s' % rootdir
	
	READY = set ()
	DONE = set ()
	IGNORED = set ()
	ALL = set ()

	for root, dirs, files in walk (rootdir):
		for name in files:
			path = realpath (join (root, name))
			pathsplit = fullsplit (path)
			relpath = pathsplit [len (rootdirsplit):]
		
			addFileToSet (ALL, relpath)
			
			if len (relpath) == 2:
				if re.match ('^[0-9][0-9][0-9]$', relpath [0]) and not (relpath [0] == '999') and re.match ('^.*?\\.zip$', relpath [1].lower ()):
					addFileToSet (READY, relpath)
			if len (relpath):
				if re.match ('^.*?\\.complete\\.txt$', relpath [-1]):
					addFileToSet (IGNORED, relpath)
					addFileToSet (DONE, relpath [:-1] + [relpath [-1][:-len ('.complete.txt')]])
	
	ALL -= IGNORED
	READY -= DONE
	DONE &= ALL
			
	UNKNOWN = ALL - (READY | IGNORED | DONE)
	print '----------------------------------------------------'
	print 'TOTAL files: %i' % (len (ALL))
	print 'Files already uploaded: %i (%0.2f%%)' % (len (DONE), (len (DONE) * 100.0) / len (ALL))
	print 'Files ready to go: %i (%0.2f%%)' % (len (READY), (len (READY) * 100.0) / len (ALL))
	print 'UNKNOWN files: %i (%0.2f%%)' % (len (UNKNOWN), (len (UNKNOWN) * 100.0) / len (ALL))
	print '----------------------------------------------------'
	if len (UNKNOWN):
		print 'UNKNOWN files:'
		for f in UNKNOWN:
			print f
		print
		print 'Please remove unknown files, then re-run.'
	elif len (READY):
		print 'Ready to process %i files, press <enter> to continue...' % len (READY)
		raw_input ()
		
		threads = []
		sl = SafeList (READY)
		for i in range (5):
			threads.append (threading.Thread (target=processUpload, args=(i, tle, rootdir, itemdefID, sl, url, options)))
		for thread in threads:
			thread.start ()
		for thread in threads:
			thread.join ()
	else:
		print 'No files to process!'

# Test if this is being run or imported
if __name__ == "__main__":
	main ()

	

	
