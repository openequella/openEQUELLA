import libxml2, libxslt
import urllib
import datetime, time       
       
def to_local_date_time (s):
	s = str (s)
	
	year = int (s [0:4])
	month = int (s [5:7])
	day = int (s [8:10])
	hour = int (s [11:13])
	minute = int (s [14:16])
	second = int (s [17:19])
	zulu = (s [-1] == 'Z')
	
	return datetime.datetime (year, month, day, hour, minute, second) - (zulu * datetime.timedelta (seconds=time.timezone))

def to_local_time (ctx, s):
	return to_local_date_time (s).isoformat ()
	

def transform (stylesheet, xml, out, preprocess={}):
	file_xsl = file (stylesheet).read ()
	for key in preprocess.keys ():
		file_xsl = file_xsl.replace (key, preprocess [key])
	
	xslDoc = libxml2.parseDoc (file_xsl)
	styleXSLT = libxslt.parseStylesheetDoc (xslDoc)
		
	sourceDoc = libxml2.parseDoc (xml)
	result1 = styleXSLT.applyStylesheet (sourceDoc, None)
	out.write (styleXSLT.saveResultToString (result1))
	result1.freeDoc ()
	sourceDoc.freeDoc ()
	styleXSLT.freeStylesheet ()

libxslt.registerExtModuleFunction ("urlencode", "http://www.dytech.com.au/util", lambda ctx, s: urllib.quote_plus (str (s)))
libxslt.registerExtModuleFunction ("stringrepeat", "http://www.dytech.com.au/util", lambda ctx, s, n: str (s) * int (n))
libxslt.registerExtModuleFunction ("localtime", "http://www.dytech.com.au/util", to_local_time)

