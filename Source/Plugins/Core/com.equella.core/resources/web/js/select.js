
function getSelectedText(select)
{
	var ind = select.selectedIndex;
	if (ind == -1)
	{
		return '';
	}
	return select.options[ind].text;
}

function getSelectedValue(select)
{
	var ind = select.selectedIndex;
	if (ind == -1)
	{
		return '';
	}
	return select.options[ind].value;
}

function getSelectedValues(select)
{
	var res = [];
	var len = select.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = select.options[i];
		if (opt.selected)
		{
			res[res.length] = opt.value;
		}
	}
	return res;
}

function getAllSelectValues(select)
{
	var res = [];
	var len = select.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = select.options[i];
		res[res.length] = opt.value;
	}
	return res;
}

function getSelectedTexts(select)
{
	var res = [];
	var len = select.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = select.options[i];
		if (opt.selected)
		{
			res[res.length] = opt.text;
		}
	}
	return res;
}

function changeSelectOptions(elem, options)
{
	elem.options.length = 0;
	for (var i=0; i<options.length; i++)
	{
		var option = options[i];
		elem.options[elem.options.length] = new Option(option.name, option.value, false);
	}
}

function setSelectedValue(select, value)
{
	var len = select.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = select.options[i];
		if (opt.value == value)
		{
			opt.selected = true;
		}
	}
}

function resetSelectedValues(select)
{
	var len = select.options.length;
	for (var i=0; i<len; i++)
	{
		var opt = select.options[i];
		opt.selected = opt.defaultSelected;
	}
}

function removeSelected(source)
{
	for ( var i = 0; i < source.options.length; i++) {
		if (source.options[i].selected)
		{
			source.options[i] = null;
			i--;
		}
	}
}

function moveSelected(source, dest)
{
	for ( var i = 0; i < source.options.length; i++) {
		var opt = source.options[i];
		if (opt.selected)
		{
			dest.options[dest.options.length] = new Option(opt.text, opt.value, false, false);
			source.options[i] = null;
			i--;
		}
	}
}

function moveAll(source, dest)
{
	for ( var i = 0; i < source.options.length; i++) {
		var opt = source.options[i];
		dest.options[dest.options.length] = new Option(opt.text, opt.value, false, false);
		source.options[i] = null;
		i--;
	}
}

function selectAll(select)
{
	for ( var i = 0; i < select.options.length; i++) {
		select.options[i].selected = true;
	}
}

function addOption(select, name, value)
{
	if (name == '' || value == '')
	{
		return;
	}
	select.options[select.options.length] = new Option(name, value, false, false);
}

function removeOption(select, value)
{
	if (value == '')
	{
		return;
	}

	for ( var i = 0; i < select.options.length; i++)
	{
		var opt = select.options[i];
		if (opt.value == value || opt.text == value)
		{
			select.options[i] = null;
			i--;
		}
	}
}

function removeOptionByIndex(select, index)
{
	if (index == -1)
	{
		return;
	}

	var opt = select.options[index];
	if(opt != null || opt != 'undefined')
	{
		select.options[index] = null;
	}
}

function editOption(select, value, newvalue)
{
	if (value == '' || newvalue == '')
	{
		return;
	}
	for ( var i = 0; i < select.options.length; i++) {
		if (select.options[i].value == value)
		{
			select.options[i].text = newvalue;
			select.options[i].value = newvalue;
		}
	}
}

function editOptionByIndex(select, index, text, value)
{
	if (value == '')
	{
		return;
	}
	var opt = select.options[index];
	if(opt != null || opt != 'undefined')
	{
		opt.text = text;
		opt.value = value;
	}
}
