$("templates/heading-content")(heading="Official Upgrades", content={
	$if(error)$
		<span class="error">$error$</span>
	$else$
		$if(version)$
			Version $version.displayName$ <span style="color: #AAAAAA;"> ($version.mmr$) </span> is available <input type="button" class="downloadButton" name="action-download" value="Download Now" onclick="document.location= '/download/download'" />
		$else$
			No upgrades available
		$endif$
	$endif$
})$
<form method="post" enctype="multipart/form-data" action="/upload/">
	$("templates/heading-content")(heading="Manually Provided Versions", content={
		<input type="file" name="file" /> <input type="submit" class="upload" name="action-upload" value="Upload" />
	})$
</form>