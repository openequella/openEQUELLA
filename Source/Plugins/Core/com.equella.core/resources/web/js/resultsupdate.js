function showNewResults(imageUrl, oldDiv, newContents, onSuccess)
{
	var wrappedSuccess = function()
	{
		if (onSuccess)
		{
			onSuccess();
		}
		$(document).trigger('equella_ajaxeffectfinished');
		$(document).trigger('equella_searchresults');
	};
	if (newContents)
	{
		updateIncludes(newContents, function()
		{
			$.each(newContents.html, function(key, html)
			{
				var upDiv = $("#" + key);
				upDiv.hide(0, function()
				{
					if (upDiv.attr("id") == oldDiv.attr("id"))
					{
						oldDiv.css('text-align', '');
					}
					upDiv.html(html.html);
					$.globalEval(html.script);
					upDiv.show(0, wrappedSuccess);
				});
			});
		});
	}
	else
	{
		if ($("div.modal-content").scrollTop() > 0)
		{
			var activeDiv = $("div.modal-content");
			var targetTop = '#searchform';
		}
		else
		{
			var activeDiv = $(window);
			var targetTop = '#searchresults';
		}
		var scrollTop = activeDiv.scrollTop();
		var offset = oldDiv.offset().top;
		if (scrollTop > offset)
		{
			activeDiv.scrollTo(targetTop, 0, { axis:'y' });
		}
		oldDiv.css("text-align", "center").html("<img src=\"" + imageUrl + "\">").show(0);
	}
}