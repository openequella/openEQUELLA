<form method="post" enctype="multipart/form-data" action="/deploy/">
	$("templates/version-set")(heading="Newer Versions", versions=newer)$
	$("templates/version-set")(heading="Current Version", versions=current)$
	$("templates/version-set")(heading="Older Versions", versions=older)$
	
	<input class="button" type="button" name="action-deploy" value="Deploy now" onClick="deploy()" />
</form>

