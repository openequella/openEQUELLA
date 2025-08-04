versions(newer,older,current) ::= <<
<form method="post" enctype="multipart/form-data" action="/deploy/">
	$version-set(heading="Newer Versions", versions=newer)$
	$version-set(heading="Current Version", versions=current)$
	$version-set(heading="Older Versions", versions=older)$

	<input class="button" type="button" name="action-deploy" value="Deploy now" onClick="deploy()" />
</form>
>>
