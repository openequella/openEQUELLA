other() ::= <<
<form method="post" enctype="multipart/form-data" action="/upload/">
	$heading-content(
	  heading="Manually Provided Versions",
	  content={
		<input type="file" name="file" />
		<input type="submit" class="upload" name="action-upload" value="Upload" />
	  }
	)$
</form>
>>
