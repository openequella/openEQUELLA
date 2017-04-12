function makeClickable($ul)
{
	var $lineItems = $ul.find('.lineItem');
	$lineItems.live('click',
			function(event)
			{
				var $checkbox = $(this).find('.selection');
			    $checkbox.attr('checked', !$checkbox.attr('checked'));
			}
		);
	
	$checkboxes = $ul.find('.selection');
	$checkboxes.live('click',
			function(event)
			{
			    event.stopPropagation();
			}
		);
}