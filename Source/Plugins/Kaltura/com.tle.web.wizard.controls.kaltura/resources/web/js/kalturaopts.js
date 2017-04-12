function setupOpts(nextButton)
{
	var $list = $(".choice-list");
	var $choices = $list.find('.choice');
	$choices.on("click", function(event)
	{
		var $choice = $(this);
		// Deselect any exisiting (thats right I said 'deselect' mofo)
		$choices.removeClass('selected');
		$("input", $choices).attr("checked", false);

		// Select new option
		$choice.addClass('selected');
		$("input", $choice).attr("checked", true);
	});

	$choices.dblclick(function(){
		$(nextButton).click();
	});

	$choices.disableSelection();

	$choices.first().click();
}