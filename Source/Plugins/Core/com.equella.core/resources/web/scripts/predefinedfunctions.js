function ensureInput(input, msg)
{
	if (input.value == null || input.value == "")
	{
		alert(msg);
		return false;
	}
	return true;
}

function ensureSelected(box, msg)
{
	if (box && box.options.length > 0)
	{
		return true;
	}
	alert(msg);
	return false;
}