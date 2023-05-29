function openWebDav(id, webdavurlmsg, webdavurlie) 
{
	if (window.screen)
	{
		w = window.screen.availWidth * 50 / 100;
		h = window.screen.availHeight * 10 / 100;
		cw = (window.screen.availWidth - w) / 2;
		ch = (window.screen.availHeight - h) / 2;
	}
	var oChildWindow = window.open('_blank','webdavdetails','scrollbars=yes,menubar=no,height='+h+',width='+w+',top='+ch+',left='+cw+',resizable=yes,toolbar=no,status=no');
	
	oChildWindow.document.write("<span style='font-family: arial; font-size: small;'>" + webdavurlmsg + "</span>");
    oChildWindow.document.close();
}