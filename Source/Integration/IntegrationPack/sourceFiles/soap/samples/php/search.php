<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>EQUELLA SOAP Searching Example</title>
<link rel="stylesheet" type="text/css" href="equellasoap.css"/>
</head>
<body>
<form method="post" action="search.php">
<div>
<?php
	require 'common.php';
	require 'settings.php';

	$query = getPost('query', '');
	$where = getPost('where', '');
	$onlylive = getBooleanPost('onlylive', '1');
	$sorttype = getPost('sorttype', '2');
	$reversesort = getBooleanPost('reversesort', '0');
	$offset = getPost('offset', '0');
	$maxresults = getPost('maxresults', '10');
?>

<fieldset>
	<legend>Search Details</legend>
	<div class="formfield">
		<label for="query">Freetext query</label>
		<div class="help">A simple text query.  E.g. <code>course*</code></div>
		<input type="text" id="query" name="query" value="<?php echo($query); ?>"/>
	</div>
	<div class="formfield">
		<label for="where">Where</label>
		<div class="help">Example:&nbsp;&nbsp;<code>where /xml/my/metadatanode like 'val%'</code>&nbsp;&nbsp;See the SOAP API documentation (SoapService41.searchItems) for the complete documentation on the where clause.</div>
		<input type="text" id="where" name="where" value="<?php echo($where); ?>"/>
	</div>
	<div class="formfield">
		<label for="onlylive">Only Live Items?</label>
		<div class="help">Include on LIVE items in the search results.  I.e. not DRAFT.</div>
		<input type="checkbox" id="onlylive" name="onlylive" <?php if ($onlylive == '1') { echo('checked="checked"'); } ?>/>
	</div>
	<div class="formfield">
		<label for="sorttype">Sort Type</label>
		<div class="help">Order the results by</div>
		<select id="sorttype" name="sorttype">
			<option value="0" <?php if ($sorttype == '0') { echo('selected="selected"'); } ?>>Search result relevance</option>
			<option value="1" <?php if ($sorttype == '1') { echo('selected="selected"'); } ?>>Date modified</option>
			<option value="2" <?php if ($sorttype == '2') { echo('selected="selected"'); } ?>>Item name</option>
		</select>
	</div>
	<div class="formfield">
		<label for="reversesort">Reverse Sort?</label>
		<div class="help">Reverses the order of the Sort Type</div>
		<input type="checkbox" id="reversesort" name="reversesort" <?php if ($reversesort == '1') { echo('checked="checked"'); } ?>/>
	</div>
	<div class="formfield">
		<label for="offset">Offset</label>
		<div class="help">The index of the first result to retrieve (zero based, i.e. zero is the first result).  E.g. if your search returns 200 results, you could retrieve results 50 to 100 using an Offset of 50 and a Maximum Results of 50.</div>
		<input type="text" id="offset" name="offset" value="<?php echo($offset); ?>"/>
	</div>
	<div class="formfield">
		<label for="maxresults">Maximum Results</label>
		<div class="help">The maximum number of results to return.</div>
		<input type="text" id="maxresults" name="maxresults" value="<?php echo($maxresults); ?>"/>
	</div>
</fieldset>

<div>
	<input type="submit" name="search" value="Search" />
</div>

<?php
	if (getPost('search', null) != null)
	{
		###################################################################################
		# EQUELLA SOAP Searching Code
		###################################################################################
		$equella = new EQUELLA($endpoint, $username, $password, $proxyHost, $proxyPort, $proxyUsername, $proxyPassword);
		$searchResultsXml = $equella->searchItems( $query, null, $where, $onlylive, $sorttype, $reversesort, $offset, $maxresults );
		
		echo('<hr/><h3>Searching EQUELLA for "'.$query.'"</h3>');
		echo('<br/>results returned: '.$searchResultsXml->nodeValue('/results/@count') );
		echo('<br/>results available: '.$searchResultsXml->nodeValue('/results/available') );
		echo('<br/>results: <ul>');
		
		#single-sign-on tokens
		if ($useTokens)
		{
			$tokenPostfix = '?token='.generateToken($tokenUser, $sharedSecretId, $sharedSecretValue);
		}
		else
		{
			$tokenPostfix = '';
		}
		
		foreach ($searchResultsXml->nodeList('/results/result') as $result)
		{
			echo('<li><a href="'.$searchResultsXml->nodeValue('xml/item/url', $result).$tokenPostfix.'">'
				.$searchResultsXml->nodeValue('xml/item/name', $result).'</a></li>');
		}
		echo('</ul>');
	}
?>
</div>
</form>
</body>
</html>