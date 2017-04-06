import re

ESCAPES = {"'": '&apos;',
		   '"': '&quot;',
		   '&': '&amp;',
		   '<': '&lt;',
		   '>': '&gt;'}

# States:  0 - at start of empty line
#          1 - still inside open tag
#          2 - text written outside tag

class XMLWriter:
	def __init__ (self, out):
		self.out = out
		self.state = 0
		self.elemstack = []
		
	def xml (self, xml):
		if self.state == 1:
			self.out.write ('>')
			self.state = 2
		elif self.state != 2:
			raise RuntimeError, 'Illegal state'
		
		self.out.write (re.sub (r'<\?[^>]*>', '', xml))
		
	def open (self, name):
		if self.state == 0:
			self.out.write ('\t' * len (self.elemstack))
		elif self.state == 1:
			self.out.write ('>\n')
			self.out.write ('\t' * len (self.elemstack))
		else:
			raise RuntimeError, 'Illegal state'
			
		self.out.write ('<')
		self._write_escaped_text (str (name))
		self.state = 1
		self.elemstack.append (name)

	def attribute (self, name, value):
		if self.state != 1:
			raise RuntimeError, 'Illegal state'
		self.out.write (' ')
		self._write_escaped_text (str (name))
		self.out.write ('="')
		self._write_escaped_text (str (value))
		self.out.write ('"')

	def close (self):
		name = self.elemstack.pop ()
		if self.state == 0:
			self.out.write ('\t' * len (self.elemstack))
			self.out.write ('</')
			self._write_escaped_text (str (name))
			self.out.write ('>\n')
		elif self.state == 1:
			self.out.write (' />\n')
			self.state = 0
		elif self.state == 2:
			self.out.write ('</')
			self._write_escaped_text (str (name))
			self.out.write ('>\n')
			self.state = 0
		else:
			raise RuntimeError, 'Illegal state'

	def _write_escaped_text (self, text):
		global ESCAPES
		for ch in text:
			if ESCAPES.has_key (ch):
				self.out.write (ESCAPES [ch])
			else:
				self.out.write (ch)

	def value (self, text):
		if self.state == 1:
			self.out.write ('>')
			self.state = 2
		elif self.state != 2:
			raise RuntimeError, 'Illegal state'
		self._write_escaped_text (str (text))

