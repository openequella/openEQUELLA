function logonReady()
{
	document.getElementById("username").focus();
	if( !document.cookie )
	{
		document.getElementById("cookieWarning").style.dispay = 'block';
	}
}