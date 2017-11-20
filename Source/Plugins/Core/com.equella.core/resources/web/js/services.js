var attempts = 0;

function refresh(callback, noResponseStatus)
{
	if ($("td.waiting").length != 0)
	{
		var timer = $.timer(2000, function()
		{
			if ($("td.waiting").length != 0 && attempts < 10)
			{
				callback(null);
				attempts++;
			}
			else
			{
				finish(noResponseStatus);
				timer.stop();
				ellipses.stop();
			}
			
		});
	}
	else
	{
		finish(noResponseStatus);
		return;
	}
	
	var ellipses = $.timer(975, function()
	{
		$("td.waiting").append(".")
	});
}

function finish(noResponseStatus)
{
	$("td.waiting").toggleClass("unknown");
	$("td.waiting").toggleClass("waiting");
	$("td.unknown").html(noResponseStatus);
	$("a.droparrow").toggleClass("wait-hide");
	$("table.services-table").parent().toggleClass("wait-hide");
	setupAccordian();
	$("a.droparrow").click();
}

function setupAccordian()
{
	$("a.droparrow").click(function(e)
	{
		$(this).toggleClass("down");
		// a -> td -> tr -> next tr
		var row = $(this).parent().parent().next("tr");
		if(e.isTrigger)
		{
			row.toggle(0);
		}else
		{
			row.toggle("fast");
		}
	});

}