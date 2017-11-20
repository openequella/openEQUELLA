function checkQuota($quota)
{
	var q = $quota.val();
	if (q == '')
	{
		return false;
	}
	
	var iq = parseInt(q,10);
	
	if (iq != q)
	{
		return false;
	}
	
	if (iq < 0)
	{
		return false;
	}
	
	return true;
}