function focusOnLoad(callBack)
{
	if(callBack)
	{
		callBack();
	}
	
	// close modal dialog on Esc press
	$(document).keyup(function(event)
	{
		if(event.which == 27 && $("img.modal_close").length > 0)
		{
			$("img.modal_close").click();
		}
	});

	
//	firstInput = $('html.accessibility #fancybox-inner .focus');
	firstInput = $('html #fancybox-inner .focus');
	// timeout so a focus isn't attempted before dialog loaded
	if(firstInput.length > 0)
	{
		setTimeout(function() {
			firstInput[0].focus();
			//alert($(":focus").attr("id")); //debug
		}, 1000);
	}
	else if($("html.accessibility").length > 0)
	{
		console.log("Need to set `focus` class on first input for accessibility");
	}
}