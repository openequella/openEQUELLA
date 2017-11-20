function handleSubmit(elem, cb)
{
	var name = '';
	var value = '';
	if (elem)
	{
		var $elem = $(elem);
		name = $elem.attr("name");
		value = $elem.val();
	}
	cb(name,value);
	return false;
}