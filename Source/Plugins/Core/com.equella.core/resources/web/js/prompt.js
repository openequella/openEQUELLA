function promptCallback(v, m, sub)
{
	var pageName = m.children('#alertName').val();
	if(!(v && pageName))
	{
		return false;
	}
	else
	{
		sub(pageName);
	}
}