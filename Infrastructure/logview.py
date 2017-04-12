
import sys, re, csv, cgi
from optparse import OptionParser

class CsvOutput:
	def __init__(self):
		self.writer = csv.writer(sys.stdout)

	def header(self, headerRow):
		self.writer.writerow(headerRow)

	def footer(self):
		pass

	def row(self, rowdata):
		self.writer.writerow(rowdata)

class HtmlOutput:
	def header(self, headerRow):
		print '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">'
		print '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">'
		print '<body><table><tr>'
		for header in headerRow[:-1]:
			print "<th>" + cgi.escape(header) + "</th>"
		print "</tr>"

	def footer(self):
		print "</table></body></html>";

	def row(self, rowdata):
		print "<tr>"
		for data in rowdata[:-1]:
			print "<td>" + cgi.escape(data) + "</td>"
		print "</tr>"

		stacktrace = rowdata[-1]
		if stacktrace:
			lines = stacktrace.splitlines()
			print '<tr><td colspan="' + (len(rowdata) - 1) + '">'
			print cgi.escape(lines[0])
			for line in lines[1:]:
				print '<br/>&#160;&#160;&#160;&#160;'
				print cgi.escape(line)
			print '</td></tr>'

def xor(a, b):
	return (a and not b) or (b and not a)

def checkTimeFormat(optionsParser, time):
	if time and not re.match(r"^[0-2]\d(?::[0-5]\d(?::[0-5]\d)(?:\.\d\d\d)?)?$", time):
		print "\nERROR: Times must be in the format HH[:MM[:SS[.MMM]]]\n"
		optionsParser.print_help()
		sys.exit(2)

def parseArguments():
	parser = OptionParser(usage="usage: %prog [options] FILE", version="%prog 3.0")
	parser.add_option("-m", "--maxrows", type="int", default=sys.maxint,
			help="maximum number of logging rows to return")
	parser.add_option("-a", "--after", type="string", metavar="TIME",
			help="only show logs after the given time in the format HH[:MM[:SS[.MMM]]]")
	parser.add_option("-b", "--before", type="string", metavar="TIME",
			help="only show logs before the given time in the format HH[:MM[:SS[.MMM]]]")
	parser.add_option("-e", "--equals", action="append", nargs=2, metavar="COLUMN VALUE",
			help="only show logs where the given column (by name or zero-based number) matches the given value")
	parser.add_option("-n", "--notequals", action="append", nargs=2, metavar="COLUMN VALUE",
			help="only show logs where the given column (by name or zero-based number) does not match the given value")
	parser.add_option("--empty", action="append", metavar="COLUMN",
			help="only show logs where the given column (by name or zero-based number) is empty")
	parser.add_option("--notempty", action="append", metavar="COLUMN",
			help="only show logs where the given column (by name or zero-based number) is not empty")
	parser.add_option("-v", "--invert", action="store_true", default=False,
			help="print out results that do not match any specified filters")
	parser.add_option("--xhtml", action="store_true", default=False,
			help="print out results in HTML format")

	(options, args) = parser.parse_args()

	if len(args) != 1:
		parser.print_help()
		sys.exit(2)

	checkTimeFormat(parser, options.after)
	checkTimeFormat(parser, options.before)

	return (options, args[0]);

def createOutputter(options):
	if options.xhtml:
		return HtmlOutput()
	else:
		return CsvOutput()


def getColumnIndex(headerRow, column):
	columnNum = -1
	try:
		columnNum = int(column)
	except ValueError:
		try:
			columnNum = headerRow.index(column)
		except:
			pass

	if columnNum < 0 or columnNum >= len(headerRow):
		print "Column '" + column + "' does not exist in the data"
		sys.exit(2)
	else:
		return columnNum


def createFilters(options, headerRow):
	filters = []

	timeColumn = getColumnIndex(headerRow, "Time")

	if options.after:
		filters.append(lambda rowdata: rowdata[timeColumn] > options.after)

	if options.before:
		filters.append(lambda rowdata: rowdata[timeColumn] < options.before)

	if options.equals:
		for (column, value) in options.equals:
			columnNum = getColumnIndex(headerRow, column)
			filters.append(lambda rowdata: rowdata[columnNum] == value)

	if options.notequals:
		for (column, value) in options.notequals:
			columnNum = getColumnIndex(headerRow, column)
			filters.append(lambda rowdata: rowdata[columnNum] != value)

	if options.empty:
		for column in options.empty:
			columnNum = getColumnIndex(headerRow, column)
			filters.append(lambda rowdata: not rowdata[columnNum])

	if options.notempty:
		for column in options.notempty:
			columnNum = getColumnIndex(headerRow, column)
			filters.append(lambda rowdata: rowdata[columnNum])

	return filters

def main():
	(options, logfile) = parseArguments()

	reader = csv.reader(open(logfile))
	headerRow = reader.next()
	filters = createFilters(options, headerRow)

	outputter = createOutputter(options)
	outputter.header(headerRow)

	rowsOutput = 0
	for rowdata in reader:

		if rowsOutput >= options.maxrows:
			break

		filtersSayYes = True
		for filter in filters:
			if not filter(rowdata):
				filtersSayYes = False
				break

		# Exclusive OR
		if xor(filtersSayYes, options.invert):
			rowsOutput = rowsOutput + 1
			outputter.row(rowdata)

	outputter.footer()

if __name__ == "__main__":
	main()
