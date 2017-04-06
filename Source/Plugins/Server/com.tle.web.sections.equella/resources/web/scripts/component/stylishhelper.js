function setValue(elem, value)
{
	var $elem = $(elem);
	var isStylish = $elem.data('ssOpts');
	if ( isStylish )
	{
		$elem.resetSS();
		$elem.setSSValue(value);
	}
	else
	{
		$elem.val(value);
	}
}

function getValue(elem)
{
	var $elem = $(elem);
	var isStylish = $elem.data('ssOpts');
	if ( isStylish )
	{
		return $elem.getSSValue();
	}
	else
	{
		return $elem.val();
	}
}

function reset(elem)
{
	var $elem = $(elem);
	var isStylish = $elem.data('ssOpts');
	if ( isStylish )
	{
		// Check for default selected
		var selIndex = hasDefaultSelected(elem);
		if(selIndex >= 0 )
		{
			elem.options[selIndex].selected = true;
		}
		else // If there is no default select the first option
		{
			elem.options[0].selected = true;
		}
				
		// Rebuild Stylish select
		$elem.resetSS();
	}
	else
	{
		elem.options[i].selected = true;
	}
}

function hasDefaultSelected(elem)
{
	var defaultSelectedIndex = -1;
	var len = elem.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = elem.options[i];
		if(opt.defaultSelected)
		{
			defaultSelectedIndex = i;
			break;
		}
	}
	
	return defaultSelectedIndex;
}