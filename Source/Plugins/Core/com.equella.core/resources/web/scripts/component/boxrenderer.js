function toggleMinimise(id, html, script)
{
	var oldDiv = $('#' + id);
	
	var showNewContent = function()
	{
		oldDiv.html(html);
		$.globalEval(script);
		oldDiv.children('.box_content').show("blind", {
			direction : "vertical",
			mode : "show"
		}, 500, null);
	};

	var foundChildren = oldDiv.children('.box_content');
	if (foundChildren.length > 0)
	{
		oldDiv.children('.box_head').replaceWith(html);
		
		foundChildren.hide("blind", {
			direction : "vertical",
			mode : "hide"
		}, 500, function()
		{
			showNewContent();
		});
	}
	else
	{
		showNewContent();
	}
}
