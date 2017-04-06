function isEnabled($elem)
{
	return !$elem.hasClass('disabled');
}

function setDisabled($elem, dis)
{
	if ( dis )
	{
		$elem.addClass('disabled');
		$elem.attr("aria-disabled", "true");
		$elem.attr("tabIndex", "-1");
	}
	else
	{
		$elem.attr("tabIndex", "0");
		$elem.attr("aria-disabled", "false");
		$elem.removeClass('disabled');
	}
}