function popup(href, target, width, height)
{
	width = _calcSize(width, screen.availWidth);
	height = _calcSize(height, screen.availHeight);
	return _popup(href, target, width, height, 0, 0, "");
}

function _calcSize(size, fullsize)
{
	var size;
    if (size.substring(size.length - 1) == "%") 
    {
    	size = size.substring(0, size.length - 1);
    	size = fullsize * (size/100);
    }
    return size;
}

function _popup(mylink, windowname, width, height, toolbar, menubar, strparam)
{
    var href;
    if (typeof(mylink) == 'string')
        href=mylink;
    else
        href=mylink.href;
    
    //This is required by IE
   	if( href.charAt(0) != '/' && href.indexOf("://") < 0)
    {
    	href=document.getElementsByTagName('base')[0].href + href;
    }

    var str = "";

    str += "resizable=1,";
	str += "scrollbars=1,";

	str += "width=" + Math.floor(width) + ",";
	str += "height=" + Math.floor(height) + ",";

    // work out x and y position of centre
	if ( window.screen ) {
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;

		var xc = Math.floor(( aw - width ) / 2);
		var yc = Math.floor(( ah - height ) / 2);

		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}
	
	var params = str;
	if (toolbar)
	{
		params += ",toolbar=" + toolbar;
	}
	if (menubar)
	{
		params += ",menubar=" + menubar;
	}
	if (strparam)
	{
		params += strparam;
	}
    oChildWindow=window.open('', windowname, params);

	var windowOpen = !oChildWindow.closed;
	var urlEmpty = false;
	var urlAbout = false;
	// IE has security about looking at the document of 3rd party links
	try
	{
		urlEmpty = !oChildWindow.document.URL;
		urlAbout = oChildWindow.document.URL.indexOf("about") == 0;
	}
	catch (err)
	{
		windowOpen = false;
	}
	
	var alreadyFocussed = false;
	if( windowOpen && !(urlEmpty || urlAbout) )
	{
		alreadyFocussed = true; 
		oChildWindow.focus();
	}
	
	oChildWindow.location = href;
	
	if (!alreadyFocussed)
	{
		oChildWindow.focus();
	}
	
	return false;
}