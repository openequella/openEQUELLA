function refresh($var)
{
	setTimeout(function()
	{
		$var.refresh();
	}, 50);
}

function save($var)
{
	$var.save();
}

function setText($var, text)
{
	if ($var !== undefined)
	{
		$var.setValue(text);
	}
}