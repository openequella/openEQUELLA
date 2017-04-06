from urllib import urlopen
from xmldoc import xmldoc
from os import popen
from re import findall
from XMLWriter import XMLWriter
from StringIO import StringIO


JIRA_ISSUE_URL = 'http://janus/jira/browse/%(issue-id)s?view=rss&decorator=none&os_username=%(username)s&os_password=%(password)s'
SVN_URL = 'svn log --xml -r "%(start-rev)s:%(end-rev)s" "%(repository)s%(branch)s"'
SVN_VERBOSE_URL = 'svn log -v -r "%(start-rev)s" "%(repository)s%(branch)s"'
SVN_DIFF_URL = 'svn diff -r "%(start-rev)s:%(end-rev)s" "%(repository)s%(branch)s"'

def get_all_rev_info (customer_username, svn_repository, svn_branch, svn_start, svn_end, jira_extensions, jira_username, jira_password):
	svn_info = {	'start-rev': str (svn_start),
				'end-rev': str (svn_end),
				'repository': svn_repository,
				'branch': svn_branch}
					
	jira_info = {	'extensions': jira_extensions,
				'username': jira_username,
				'password': jira_password}
	
	svn = get_reduced_svn_list (svn_info, jira_info)

	bugs = []
	for item in svn:
		for bug in item ['jira']:
			if bug not in bugs:
				bugs += [bug]
	hash = {}
	for issue in bugs:
		hash [issue] = get_reduced_jira (issue, jira_info)
	for item in svn:
		item ['jira'] = [hash [x] for x in item ['jira']]

	return svn


def get_reduced_jira (iid, jira_info):
	issue = get_jira_bug (iid, jira_info)
	
	scope = str (issue.get ("customfields/customfield[@id='customfield_10080']/customfieldvalues/customfieldvalue"))
	customer = [str (x) for x in issue.match ("customfields/customfield[@id='customfield_10083']/customfieldvalues/customfieldvalue")]
	
	public_title = str (issue.get ("customfields/customfield[@id='customfield_10081']/customfieldvalues/customfieldvalue"))
	public_description = str (issue.get ("customfields/customfield[@id='customfield_10082']/customfieldvalues/customfieldvalue"))
	key = str (issue.get ("key"))
	
	title = str (issue.get ("summary"))
	description = str (issue.get ("description"))
	
	return {'scope': scope,
		'customer': customer,
		'public_title': public_title,
		'public_description': public_description,
		'key': key,
		'title': title,
		'description': description}
	
def reduce_svn_item (item, jira_info):
	rev = str (item.match ('@revision') [0])
	message = str (item.match ('msg') [0])
	author = str (item.match ('author') [0])
	jira = extract_refs (message, jira_info)
	
	return {'rev': rev, 'message': message, 'jira': jira, 'author': author}
	
def get_reduced_svn_list (svn_info, jira_info):
	return [reduce_svn_item (x, jira_info) for x in get_svn_list (svn_info)]

def extract_refs (message, jira_info):
	bugs = []
	message = message.upper ()
	for extension in jira_info ['extensions']:
		for bug in findall ('(%s-[0-9]+)' % extension, message):
			if bug not in bugs:
				bugs += [bug]
	return bugs
	
def get_jira_bug (iid, jira_info):
	new_info = jira_info.copy ()
	new_info.update ({'issue-id': iid})
	print 'Connecting... ', JIRA_ISSUE_URL % new_info
	return xmldoc (urlopen (JIRA_ISSUE_URL % new_info).read ()).match ('/rss/channel/item') [0]

def get_svn_list (svn_info):
	return xmldoc (popen (SVN_URL % svn_info).read ()).match ('/log/logentry')

def find_bug_refs_from_rev (revision, svn_info, jira_info, bugs):
	message = str (revision.get ('msg')).upper ()
	for bug in extract_refs (message, jira_info):
		if bug not in bugs:
			bugs += [bug]
				
		
def harvest_jira_issues (svn_repository, svn_branch, svn_start, svn_end, jira_extensions):
	svn_info = {	'start-rev': str (svn_start),
				'end-rev': str (svn_end),
				'repository': svn_repository,
				'branch': svn_branch}
					
	jira_info = {	'extensions': jira_extensions }
	
	svn = get_reduced_svn_list (svn_info, jira_info)
	return set (reduce (lambda a, b: a + b, [item ['jira'] for item in svn]))



def get_fixed_issues (customer_username, svn_repository, svn_branch_old, svn_branch_old_rev, svn_branch_new, svn_branch_new_rev, svn_branch_start_map, jira_extensions, jira_username, jira_password):
	jira_info = {	'username': jira_username,
				'password': jira_password}
	
	if svn_branch_old == svn_branch_new:
		release_set = harvest_jira_issues (svn_repository, svn_branch_old, svn_branch_old_rev, svn_branch_new_rev, jira_extensions)
	else:
		old_bug_fixes = harvest_jira_issues (svn_repository, svn_branch_old, svn_branch_start_map [svn_branch_old], svn_branch_old_rev, jira_extensions)
		new_bug_fixes = harvest_jira_issues (svn_repository, svn_branch_new, svn_branch_start_map [svn_branch_new], svn_branch_new_rev, jira_extensions)
	
		new_features = harvest_jira_issues (svn_repository, '/trunk', svn_branch_start_map [svn_branch_old], svn_branch_start_map [svn_branch_new], jira_extensions)
	
		release_set = (new_features | new_bug_fixes) - old_bug_fixes

	rv = []
	for idx, bug in enumerate (release_set):
		print "Processing:", idx, bug
		issue = get_jira_bug (bug, jira_info)
		issue_scopes = issue.match ("customfields/customfield[@id='customfield_10080']/customfieldvalues/customfieldvalue")
		if len (issue_scopes):
			if (str (issue_scopes [0]) == 'Public') or ((str (issue_scopes [0]) == 'Protected') and len (issue.match ("customfields/customfield[@id='customfield_10083']/customfieldvalues/customfieldvalue[text()='%s']" % customer_username))):
				title = str (issue.match ("customfields/customfield[@id='customfield_10081']/customfieldvalues/customfieldvalue") [0])
				desc = str (issue.match ("customfields/customfield[@id='customfield_10082']/customfieldvalues/customfieldvalue") [0])
				key = str (issue.match ("key") [0])
				rv.append ({'title': title, 'description': desc, 'key': key})
	return rv

def get_rev_log_info (svn_repository, svn_branch, svn_rev):
	svn_info = {	'start-rev': str (svn_rev),
				'repository': svn_repository,
				'branch': svn_branch}
	return popen (SVN_VERBOSE_URL % svn_info).read ()

def get_rev_diff_info (svn_repository, svn_branch, svn_rev):
	svn_info = {	'start-rev': str (int (svn_rev) - 1),
				'end-rev': str (svn_rev),
				'repository': svn_repository,
				'branch': svn_branch}
	return popen (SVN_DIFF_URL % svn_info).read ()



#print py2xml.py2xml (issue=get_fixed_issues ('imb', 'svn://tle-svn/tle', '/branches/Tamar', 4000, 4100, ['TLE', 'TLX'], 'tleautobuild', 'tleautobuild'))

#print get_fixed_issues ('tafetas', 'svn://tle-svn/tle', '/branches/Tamar', 4033, '/branches/Gordon', 5218, {'/branches/Tamar': 3112, '/branches/Gordon': 4292}, ['TLE', 'TLX'], 'tleautobuild', 'tleautobuild')
