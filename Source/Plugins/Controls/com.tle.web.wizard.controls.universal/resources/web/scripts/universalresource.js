function setupPickResourcesType(list, nextButton)
{
	var $tds = list.find('tr td');
	$tds.click(function()
	{
		var $t = $(this);

		$tds.removeClass('selected');
		$tds.data('sel', true);
		$t.addClass('selected');
		$t.data('sel', true);

		$(nextButton).removeAttr('disabled');
	});
	$tds.dblclick(function(){
		$(nextButton).click();
	});
	$tds.disableSelection();

	//TODO: arrow key, mouse wheel and double click..
	// At the moment works by users tabbing through the list and pressing enter on the attachment type they want,
	//instructions should be put in place for screen readers to read. Focus is lost after they press Next
	$tds.each(function(i) {
		if(i == 0)
		{
			$(this).attr("class"," focus");
		}
		$(this).attr("tabIndex", "0");
	});
	
	$tds.keyup(function(event)
	{
		var $t = $(this);
		if (event.which == 13) //enter
		{
			$t.click();
		} 
	});

	$('div.modal-footer input[value="next"]').attr("tabIndex", $tds.length + 1);
	$tds.first().click();
}