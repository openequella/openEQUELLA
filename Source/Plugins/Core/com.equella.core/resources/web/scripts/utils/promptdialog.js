function getText($elem)
{
	return $elem.val();
}

function textChange($elem, $okay)
{
	var val = $elem.val();
	if (_empty(val))
	{
		$okay.attr('disabled', 'disabled');
	}
	else
	{
		$okay.removeAttr('disabled');
	}
}

function _empty(val)
{
	if (val && $.trim(val) != '')
	{
		return false;
	}
	return true;
}