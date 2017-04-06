
import os
import re

regex = re.compile(r'<import plugin-id="(.*?)"')

output = open("graph.dot", "w")
output.write("digraph name\n{\n")

for subpath in ["Generic", "Server", "Admin"]:
	for plugin in os.listdir("./" + subpath):
		xmlpath = os.path.join(".", subpath, plugin, "plugin-jpf.xml")
		if os.path.exists(xmlpath):
			input = open(xmlpath, 'r')
			for line in input:
				for match in regex.finditer(line):
					if match:
						output.write("\t")
						output.write(plugin.replace('.', '_'))	
						output.write(" -> ")
						output.write(match.group(1).replace('.', '_'))
						output.write(";\n")
			input.close();

output.write("}")
output.close()

