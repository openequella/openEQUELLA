
function showhide(element, selvalue)
{
	if (selvalue == 'BETWEEN')
	{
		$(element).show('blind', 125);
	}
	else if (!$(element).is(':hidden'))
	{
		$(element).hide('blind', 250);
	}

}
