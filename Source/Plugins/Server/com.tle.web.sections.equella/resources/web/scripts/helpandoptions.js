function blindDomResults(oldDiv, newContents, onSuccess)
{
	if (newContents)
	{
		updateIncludes(newContents, function()
		{
			var showNewContent = function(onSuccess)
			{
				var newhtml = newContents.html['helpAndOptions'];
				oldDiv.html(newhtml.html).children('.topblock').hide();
				$.globalEval(newhtml.script)
				oldDiv.children('.topblock').show("blind", {
					direction : "vertical",
					mode : "show"
				}, 500, onSuccess);
			};

			var foundChildren = oldDiv.children('.topblock');
			if (foundChildren.length > 0)
			{
				foundChildren.hide("blind", {
					direction : "vertical",
					mode : "hide"
				}, 500, function()
				{
					oldDiv.children('.topblock').remove();
					showNewContent();
				});
			}
			else
			{
				showNewContent(onSuccess);
			}
		});
	}
}
