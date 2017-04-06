import libxml2, libxslt

class nodewrapper:
	def __init__ (self, child, doc):
		self.child = child
		self.doc = doc
		
	def __str__ (self):
		return self.child.get_content ()
		
	def __repr__ (self):
		return self.__str__ ().__repr__ ()
		
	def match (self, q):
		return [nodewrapper (x, self.doc) for x in self.child.xpathEval (q)]
		
	def get (self, q):
		test = self.match (q)
		if len (test):
			return test [0]
		else:
			return None

def xmlescape (s):
	s = s.replace ('&', '&amp;')
	s = s.replace ("'", '&apos;')
	s = s.replace ('<', '&lt;')
	s = s.replace ('>', '&gt;')
	s = s.replace ('"', '&quot;')
	return s

class xmldoc:
	def __init__ (self, s):
		self.doc = libxml2.parseDoc (s)
		
	def match (self, q):
		ctxt = self.doc.xpathNewContext ()
		rv = ctxt.xpathEval (q)
		ctxt.xpathFreeContext ()
		return [nodewrapper (x, self) for x in rv]
		
	def get (self, q):
		test = self.match (q)
		if len (test):
			return test [0]
		else:
			return None
		
	def __del__ (self):
		self.doc.freeDoc ()
		
