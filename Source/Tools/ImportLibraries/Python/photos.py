# Copyright Dytech Solutions, 2005.

# This module is provided 'commercial-in-confidence' and may not be reproduced nor redistributed without
# express written permission from the copyright holder.

# Author: Adam Eijdenberg, Dytech Solutions <adam.eijdenberg@dytech.com.au>

import os, os.path

from tleclient import TLEClient

tle = TLEClient (host='203.24.107.36', username='netspot', password='netspot')

# Iterate through a directory structure, uploading each JPG package found...
for root, dirs, files in os.walk ('.'):
	for name in files:
		if name.lower ().endswith ('.jpg'):
			print 'Uploading %s...' % name
			item = tle.createNewItem (tle.getItemdefUUID ('WebCT conference 2005'))
			item.setNode ('item/name', 'NetSpot Photo')
			item.setNode ('item/description', 'NetSpot Cairns Trip')
			item.setNode ('item/keywords', 'netspot cairns webct photo')
			item.createNode ('item/itembody', {
					'type': 'Image',
					'imagetype': 'Colour Photo',
				})
			item.attachFile (name, file (os.path.join (root, name), 'rb'))
			item.addStartPage (name, name)
			item.submit ()

