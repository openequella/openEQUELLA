def serialize (name, obj, writer):
	if hasattr (obj, '__class__') and obj.__class__ == xmlwrap:
		writer.open (name)
		writer.xml (obj.xml)
		writer.close ()
	elif hasattr (obj, '__dict__'):
		writer.open (name)
		for (attr_name, attr_value) in obj.__dict__.iteritems ():
			if attr_name.__class__ == str:
				serialize (attr_name, attr_value, writer)
			else:
				writer.open ('item')
				serialize ('key', attr_name, writer)
				serialize ('value', attr_value, writer)
				writer.close ()
		writer.close ()
	elif obj.__class__ == list:
		for x in obj:
			serialize (name, x, writer)
	elif obj.__class__ == tuple:
		for x in obj:
			serialize (name, x, writer)
	elif obj.__class__ == dict:
		writer.open (name)
		for (attr_name, attr_value) in obj.iteritems ():
			if attr_name.__class__ == str:
				serialize (attr_name, attr_value, writer)
			else:
				writer.open ('item')
				serialize ('key', attr_name, writer)
				serialize ('value', attr_value, writer)
				writer.close ()
		writer.close ()
	else:
		writer.open (name)
		writer.value (str (obj))
		writer.close ()
		
class xmlwrap:
	def __init__ (self, xml):
		self.xml = xml

def py2xml (**kargs):
	from StringIO import StringIO
	from XMLWriter import XMLWriter
	io = StringIO ()
	serialize ('xml', kargs, XMLWriter (io))
	rv = io.getvalue ()
	io.close ()
	return rv
