function resizeListeners(width, height)
{		
	$("#player-container").css("left",(($(window).width()/2) - (width /2)) + "px");
	$("#player-container").css("top",(($(window).height()/2) - (height / 2)) + "px");
	
	$(window).resize(function()
	{
		$("#player-container").css("left",(($(window).width()/2) - ( width /2)) + "px");
		$("#player-container").css("top",(($(window).height()/2) - ( height /2)) + "px");
	});		
}