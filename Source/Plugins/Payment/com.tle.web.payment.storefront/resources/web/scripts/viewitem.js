function delayReload($textbox, reloadFunc)
{
	var timer = $textbox.data("timer");
	clearTimeout(timer);
	timer=setTimeout(function(){reloadFunc();}, 400);
	$textbox.data("timer", timer);
}