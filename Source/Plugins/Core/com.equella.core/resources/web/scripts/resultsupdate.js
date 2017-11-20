function showNewResults(oldDiv, newContents, onSuccess)
{
	var wrappedSuccess = function()
	{
		if (onSuccess)
		{
			onSuccess();
		}
		$(document).trigger('equella_ajaxeffectfinished');
	};
	if (newContents)
	{
		updateIncludes(newContents, function()
		{
			$.each(newContents.html, function(key, html)
			{
				var oldDiv = $("#" + key);
				oldDiv.fadeTo("normal", 0, function()
				{
					oldDiv.html(html.html).fadeTo("normal", 1, wrappedSuccess)
					$.globalEval(html.script);
				});
			});
		});
	}
	else
	{
		// $.scrollTo('#searchresults', 800);
	}
}