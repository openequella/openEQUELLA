function toggleHint($hint)
{
	$hint.slideToggle(400);
}

$(document).ready(function()
{
	if (window != top)
	{
		$("body").addClass("qtiplayvieweriframe");
	}
})
