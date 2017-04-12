function showHideActions(oldDiv, newContents, onSuccess)
{
	if (newContents)
	{
		updateIncludes(newContents, function()
		{
			var showNewContent = function(onSuccess)
			{
				var newhtml = newContents.html['searchresults-actions'];
				oldDiv.html(newhtml.html).children('.resulttopblock').hide();
				
				var newButtons = newContents.html['actionbuttons'];
				$("#actionbuttons").html(newButtons.html);				
				
				$.globalEval(newhtml.script)
				oldDiv.children('.resulttopblock').show("blind", {
					direction : "vertical",
					mode : "show"
				}, 500, onSuccess);
			};

			var foundChildren = oldDiv.children('.resulttopblock');
			if (foundChildren.length > 0)
			{
				foundChildren.hide("blind", {
					direction : "vertical",
					mode : "hide"
				}, 500, function()
				{
					oldDiv.children('.resulttopblock').remove();
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