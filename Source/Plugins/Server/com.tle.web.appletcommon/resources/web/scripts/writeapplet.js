// id param is optional.  If omitted then applet_object (for IE) or applet_embed will be used
function writeAppletTags(placeholder, jarurl, mainclass, locale, rtl, endpoint, height, width, options, id)
{
	if( options == undefined )
	{
		options = {};
	}

	// A bunch of default options that must be set
	options['jnlp.LOCALE'] = locale;
	options['jnlp.ENDPOINT'] = endpoint;
	options['jnlp.COOKIE'] = document.cookie;
	options['jnlp.RTL'] = rtl;
	options['mayscript'] = "true";
	options['scriptable'] = "false";
	
	var obj;
	if ($.browser.msie)
	{
		var appletId = (id ? id : 'applet_object');
		obj = '<OBJECT id="' + appletId + '" name="' + appletId + '" ' + 
		'classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" ' + 
		'codebase="http://java.sun.com/update/1.7.0/jinstall-7u21-windows-i586.cab#Version=1,7,0,21" ' +
		'width="' + width + '" height="' + height + '">' +
		'		    <PARAM NAME="ARCHIVE" VALUE="' + jarurl + '">' +
		'		    <PARAM NAME="CODE" VALUE="' + mainclass + '">' +
		'		    <PARAM NAME="type" VALUE="application/x-java-applet;version=1.7">' +
		'		    <PARAM NAME="HEIGHT" VALUE="' + height + '">' +
		'		    <PARAM NAME="WIDTH" VALUE="' + width + '">' +
		'		    <PARAM NAME="CODEBASE_LOOKUP" VALUE="false">' +
					getParamsString(false, options) +
		'		</OBJECT>';
	}
	else
	{
		var appletId = (id ? id : 'applet_embed');
		obj = '<EMBED id="' + appletId + '" name="' + appletId + '" ' +
		'	        type="application/x-java-applet;version=1.7" ' + 
		'	        ARCHIVE="' + jarurl + '" ' +
		'	        CODE="' + mainclass + '" ' +
		'			HEIGHT="' + height + '" ' +
		'			WIDTH="' + width + '" ' +
		'           CODEBASE_LOOKUP="false"' +
					getParamsString(true, options) +
		'		    pluginspage="http://www.oracle.com/technetwork/java/index-jsp-141438.html#download">' +	
		'		</EMBED>';
	}
	
	placeholder.append(obj);
}

function writeAppletTagsNew(placeholder, attributes, parameters)
{
	parameters['jnlp.COOKIE'] = document.cookie;
		
	//apparently this is how you do it...
	//http://www.oracle.com/technetwork/java/javase/index-142562.html
	var $obj = $('<applet ' + getParamsString(true, attributes) + 
			'>' +
        	'<param name="type" value="application/x-java-applet;version=1.7">' +
        	getParamsString(false, parameters) +
			'</applet>');
	placeholder.append($obj);
	return $obj;
}

function getParamsString(embed, options)
{
	var rv = " ";
	if( embed )
	{
		for( var key in options ) 
		{
			rv += key + '="' + options[key] + '" ';
		}
	}
	else
	{
		for( var key in options ) 
		{
			rv += '<param name="'+ key + '" value="' + options[key] + '" />';
		}
	}
	return rv;
}

function getApplet(id)
{
	if (id)
	{
		return document.getElementById(id);
	}
	else
	{
		var app = document.applet_embed; 
		if (app)
		{
			return app;
		}
		return document.applet_object;
	}
}

function getAppletNew(id)
{
	if (id)
	{
		return document.getElementById(id);
	}
	return document.applet_ref; 
}
