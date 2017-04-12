# Copyright Dytech Solutions, 2005.

# This module is provided 'commercial-in-confidence' and may not be reproduced nor redistributed without
# express written permission from the copyright holder.

# Author: Adam Eijdenberg, Dytech Solutions <adam.eijdenberg@dytech.com.au>

# Note: This is a very basic ODBC database access wrapper. It requires Python and the Win32 extensions to be installed.

import dbi, odbc
import re

DATE_MATCHER = re.compile ('[^ ]+ ([^ ]+) ([0-9]+) ([0-9]+):([0-9]+):([0-9]+) ([0-9]+)')
MONTHS = {
        'Jan': 1,
        'Feb': 2,
        'Mar': 3,
        'Apr': 4,
        'May': 5,
        'Jun': 6,
        'Jul': 7,
        'Aug': 8,
        'Sep': 9,
        'Oct': 10,
        'Nov': 11,
        'Dec': 12,
    }

def clean_field (field):
	if hasattr (field, '__class__') and field.__class__ == str:
		return unicode (field, 'cp1252')
	else:
		return field

def zp (s, i):
        while len (s) < i:
                s = '0' + s
        return s

class ODBCClient:
	# Create an ODBC client given the datasource name
	def __init__ (self, odbcSourceName):
		self.dbc = odbc.odbc (odbcSourceName)
		
	# Given a SQL statement, return a two dimensional array of unicode strings as a result set
	def fetch (self, q):
		cursor = self.dbc.cursor ()
		cursor.execute (q)
		res = [[clean_field (field) for field in row] for row in cursor.fetchall ()]
		cursor.close ()
		return res

        def date_to_iso (self, date):
                month, date, hour, minute, second, year = DATE_MATCHER.match (str (date)).groups ()
                return '%s-%s-%sT%s:%s:%s' % (zp (year, 4), zp (str (MONTHS [month]), 2), zp (date, 2), zp (hour, 2), zp (minute, 2), zp (second, 2))
