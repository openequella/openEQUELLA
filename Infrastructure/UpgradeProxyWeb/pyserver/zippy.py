import os, re, zipfile, datetime, XMLWriter

ZIP_EXTENSIONS = ['zip', 'jar', 'ear', 'war']
SIGNED_MATCH = re.compile (r'/?META-INF/.*?\.SF')

def process_entry (zip, entry, signed):
	ext_index = entry.filename.rfind ('.')
	if not signed and ext_index >= 0 and entry.filename [ext_index + 1:].lower () in ZIP_EXTENSIONS:
		yield ('container', entry.filename, entry.CRC, lambda: zip.read (entry.filename))
		tmp = os.tmpfile ()
		tmp.write (zip.read (entry.filename))
		tmp.seek (0)
		for thing in process_file (tmp):
			yield thing
		tmp.close ()
	else:
		yield ('entry', entry.filename, entry.CRC, lambda: zip.read (entry.filename))

def process_file (file_object):
	zip = zipfile.ZipFile (file_object)
	sorted = list (zip.infolist ())
	signed = len ([x for x in sorted if SIGNED_MATCH.match (x.filename)])
	sorted.sort (lambda a, b: cmp (a.filename, b.filename))
	prev = None
	for item in sorted:
		if item.filename != prev:
			for thing in process_entry (zip, item, signed):
				yield thing
			prev = item.filename
	yield ('end',)
	
def finish_container (iter):
	count = 1
	while count > 0:
		cur = iter.next ()
		if cur [0] == 'container':
			count += 1
		elif cur [0] == 'end':
			count -= 1

def process_containers (old_iter, new_iter):
	old, new = old_iter.next (), new_iter.next ()
	while not (old [0] == 'end' and new [0] == 'end'):
		#print old, new
		if old [0] == 'end' or (not (new [0] == 'end') and old [1] > new [1]):
			yield ('add', new [1],  new [3] ())
			if new [0] == 'container':
				finish_container (new_iter)
			new = new_iter.next ()
		elif new [0] == 'end' or (not (old [0] == 'end') and new [1] > old [1]):
			yield ('delete', old [1])
			if old [0] == 'container':
				finish_container (old_iter)
			old = old_iter.next ()
		else:
			if old [2] != new [2]:
				if old [0] == 'container' and new [0] == 'container':
					dirty = 0
					for thing in process_containers (old_iter, new_iter):
						if not dirty:
							yield ('descend', new [1])
							dirty = 1
						yield thing
					if dirty:
						yield ('ascend',)
				else:
					yield ('update', new [1], new [3] ())
			elif new [0] == 'container':
				finish_container (old_iter), finish_container (new_iter)
			old, new = old_iter.next (), new_iter.next ()

def create_upgrade (old_filename, new_filename, script_filename):
	old_iter = process_file (file (old_filename, 'rb'))
	new_iter = process_file (file (new_filename, 'rb'))
	temp_steps_name = os.tmpnam ()
	temp_steps = file (temp_steps_name, 'wb')
	xml = XMLWriter.XMLWriter (temp_steps)
	xml.open ('upgrade')
	xml.attribute ('old-file', os.path.basename (old_filename))
	xml.attribute ('new-file', os.path.basename (new_filename))
	out = zipfile.ZipFile (script_filename, 'w', zipfile.ZIP_DEFLATED)
	id = 0
	for thing in process_containers (old_iter, new_iter):
		xml.open ('step')
		xml.attribute ('type', thing [0])
		if thing [0] in ['add', 'update', 'delete', 'descend']:
			xml.attribute ('file', thing [1])
		if thing [0] in ['update', 'add']:
			id += 1
			temp_name = os.tmpnam ()
			temp = file (temp_name, 'wb')
			temp.write (thing [2])
			temp.close ()
			out.write (temp_name, 'contents/%i.data' % id)
			os.remove (temp_name)
			xml.attribute ('data', 'contents/%i.data' % id)
		xml.close ()
	xml.close ()
	temp_steps.close ()
	out.write (temp_steps_name, 'manifest.xml')
	os.remove (temp_steps_name)
	out.close ()

#create_upgrade ('tle-upgrade-2.2.r3680 (branches+Tamar).zip', 'tle-upgrade-2.2.r3791 (branches+Tamar).zip', 'upgrade.zip')
