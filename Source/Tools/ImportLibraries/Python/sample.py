# Copyright Dytech Solutions, 2005.

# This module is provided 'commercial-in-confidence' and may not be reproduced nor redistributed without
# express written permission from the copyright holder.

# Author: Adam Eijdenberg, Dytech Solutions <adam.eijdenberg@dytech.com.au>

import os, os.path

from tleclient import TLEClient
from odbclient import ODBCClient

tle = TLEClient (host='trial.thelearningedge.com.au', username='admin', password='insert.secret.password')
db = ODBCClient ('URLs')

# Iterate through a database and construct metadata based on each record...
for name, description, keywords, url in db.fetch ('SELECT Name, Description, Keywords, URL FROM URLs'):
	item = tle.createNewItem (tle.getItemdefUUID ('K12 - Digital Asset'))
	item.setNode ('item/name', name)
	item.setNode ('item/description', description)
	item.setNode ('item/keywords', keywords)
	item.addUrl (name, url)
	item.attachFile ('path/to/file/within/item.txt', file ('item.txt', 'rb'))
	item.addStartPage ('Name to appear in resource section', 'path/to/file/within/item.txt')
	item.createNode ('item/management', {
			'general': {
					'catalogue.entry': [
							{'catalogue': 'URL', 'entry': 'fdsfs'},
							{'catalogue': 'ISBN', 'entry': 'fdfs'},
						],
					'title': {'title.main': 'Title'},
					'description': 'Description',
					'keyword': ['a', 'b', 'c'],
					'resource.type': 'xxxx',
					'folder': 'xxxx',
				},
			'meta.metadata': {
					'contribute': {'role': 'Creator', 'entity': 'xxxxxx', 'date': 'yyyyyy'},
					'security': 'xxxxx',
				},
		})
	item.printXml ()
	item.submit ()

# Iterate through a directory structure, uploading each IMS package found...
for root, dirs, files in os.walk ('.'):
	for name in files:
		if name.endswith ('.zip'):
			item = tle.createNewItem (tle.getItemdefUUID ('Import IMS Test'))
			item.attachIMS (file (os.path.join (root, name), 'rb'))
			item.printXml ()
			item.submit ()