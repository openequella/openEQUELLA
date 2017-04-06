function printReport(id)
{
	var printFn = document.createElement('script');
	printFn.type = 'text/javascript';
	printFn.text = printPage;

	var iframe = document.frames ? document.frames[id] : document
			.getElementById(id);
	if (iframe)
	{
		var ifWin = iframe.contentWindow || iframe;
		iframe.focus();
		ifWin.document.getElementsByTagName("head")[0].appendChild(printFn);
		ifWin.printPage();
	}
	return false;
}

function printPage( )
{
	print();
};

function popupReport(url)
{
	if (window.screen)
	{
		w = window.screen.availWidth * 85 / 100;
		h = window.screen.availHeight * 85 / 100;
		cw = (window.screen.availWidth - w) / 2;
		ch = (window.screen.availHeight - h) / 2;
	}
	var name = "report" + new Date().getTime();
	window.open(url,name,'scrollbars=yes,menubar=no,height='+h+',width='+w+',top='+ch+',left='+cw+',resizable=yes,toolbar=no,status=no');
}

