
<form method="post" enctype="multipart/form-data" action="/upload/">
	$("templates/heading-content")(heading="Manually Provided Versions", content={
		<input type="file" name="file" /> <input type="submit" class="upload" name="action-upload" value="Upload" />
	})$
</form>