function getValues(multi)
{
	var fullvalue = '';
	var controls = multi.controls;
	var ncont = controls.length;
	for ( var i = 0; i < ncont; i++)
	{
		var value = controls[i].value();
		if (controls[i].mandatory && value == "")
		{
			return "";
		}
		if (i > 0)
		{
			fullvalue += "⛐";
		}
		fullvalue += encodeURIComponent(value);
	}

	return fullvalue;
}

function setValues(multi, valuesStr)
{
	var controls = multi.controls;
	var ncont = controls.length;
	var values = valuesStr.split("⛐");
	for ( var i = 0; i < ncont; i++)
	{
		controls[i].edit(decodeURIComponent(values[i]));
	}
}

function getTexts(multi)
{
	var fulltext = '';
	var controls = multi.controls;
	var ncont = controls.length;
	var sep = multi.separator;
	for ( var i = 0; i < ncont; i++)
	{
		var text = controls[i].text();
		var value = controls[i].value();

		if (i > 0)
		{
			fulltext += value != "" ? sep : multi.separator.substr(0, sep.lastIndexOf(' '));
		}

		if (text != null && value != "")
		{
			fulltext += $.trim(text);
		}
	}

	return fulltext;
}

function resetControls(multi)
{
	var controls = multi.controls;
	var ncont = controls.length;
	for ( var i = 0; i < ncont; i++)
	{
		controls[i].reset();
	}
}

function validateControls(multi)
{
	var controls = multi.controls;
	var ncont = controls.length;
	for ( var i = 0; i < ncont; i++)
	{
		var val2 = controls[i].value();
		if (controls[i].mandatory && $.trim(controls[i].value()) == "")
		{
			return false;
		}
	}

	return true;
}