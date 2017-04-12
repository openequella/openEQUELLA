
import os
import re
import string
import sys

for root, dirs, files in os.walk('../Source'):
	for name in files:
		if( name.endswith('.ftl') and name.count('debug') == 0 ):
			fullname = os.path.join(root, name)
			ftl = open(fullname, "r")
			c = ''.join(ftl.readlines()).join(' ')
			ftl.close()

			# Remove any style and script tags and content
			c = re.sub(r'(?s)<style.+?</style>', '', c)
			c = re.sub(r'(?s)<@?script.+?</@?script>', '', c)
			c = re.sub(r'(?s)<#assign (?:PART_(?:HEAD|READY|FUNCTION_DEFINITIONS)|MCE_).+?</#assign>', '', c)
			c = re.sub(r'(?s)<#macro.+?</#macro>', '', c)

			# Remove all XML tags - Make sure nested tags are removed first
			r = 1
			while r > 0:
				(c, r) = re.subn(r'<[^<>]+>', '', c)

			# Remove all ${...} interpolated values
			c = re.sub(r'\$\{[^}]+\}', '', c)

			# Remove &nbsp and other characters that we will allow
			c = c.replace('&nbsp;', '')
			c = c.replace(':', '')
			c = c.replace('*', '')
			c = c.replace('(', '')
			c = c.replace(')', '')
			c = c.replace('|', '')

			# Remove multiple empty spaces and strip the ends
			c = ' '.join(c.split()).strip()

			if len(c) > 0:
				print ("**************************************************************************")
				print ("Garbage found in ", fullname)
				print (c)


