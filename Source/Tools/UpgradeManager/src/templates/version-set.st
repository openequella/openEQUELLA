$("templates/heading-content")(heading=heading, content={
	$if(versions)$
		<ul>
			$versions:{<li><input id="version$it.mmr$" class="version-item" type="radio" name="version" value="$it.filename$" /><label for="version$it.mmr$">$it.displayName$ <span style="color: #AAAAAA;"> ($it.mmr$) </span></label>  </li>}$
		</ul>
	$else$
		No versions found
	$endif$
})$