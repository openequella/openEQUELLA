function inPlaceCreateApplet($placeholder, width, height, parameters, id)
{
	var $elem = $placeholder;
	var colour = $elem.css('background-color');
	//Chrome gives rgba(0, 0, 0, 0)
	while (colour == 'transparent' || colour == 'rgba(0, 0, 0, 0)')
	{
		$elem = $elem.parent();
		if (parent == null)
		{
			break;
		}
		colour = $elem.css('background-color');
	}
	parameters['jnlp.BACKGROUND'] = colour;
	parameters['jnlp.WIDTH'] = width;
	parameters['jnlp.HEIGHT'] = height;

	var attributes = 
	{
			id: id, 
			name: id,
			code: parameters.code, /* Chrome sucks */
			/*codebase:parameters.codebase,*/ 
			archive: parameters.archive, /* Safari sucks */
			width: width, 
			height: height
	};

	var $applet = writeAppletTagsNew($placeholder, attributes, parameters);
}


/**
 * 
 * @param appletId (string)
 * @param cb (function) A callback to load the applet if none is available
 * @param openWith (boolean) 
 */
function inPlaceOpen(appletId, cb, openWith)
{
	var applet = getAppletNew(appletId);
	
	if (applet)
	{
		try
		{
			if (openWith)
			{
				debug('inPlaceOpen: invoking openWith');
				applet.openWith();
			}
			else
			{
				debug('inPlaceOpen: invoking open');
				applet.open();
			}
		}
		catch (e)
		{
			if (e.message)
			{
				debug('inPlaceOpen: error invoking applet method: ' + e.message);
			}
			throw e;
		}
	}
	else
	{
		debug('inPlaceOpen: no applet available.  checking callback...');
		if (cb)
		{
			debug('inPlaceOpen: invoking callback');
			cb();
		}
	}
}

var callbackInvoked = false;

function inPlaceCheckSynced(appletId, submitCallback, beingUploadedMessageCallback, changesDetectedConfirmationMessageCallback)
{
	$('BODY').addClass('waitcursor');

	var doSubmit = function()
	{
		if (callbackInvoked)
		{
			debug('inPlaceCheckSynced: callback already invoked');
		}

		if (submitCallback && !callbackInvoked)
		{
			callbackInvoked = true;
			debug('inPlaceCheckSynced: invoking callback');
			submitCallback();

			//$('BODY').removeClass('waitcursor'); or not??

			// assumed that no user interaction can actually happen now
			callbackInvoked = false;
		}
	}

	var applet = getAppletNew(appletId);

	if (applet)
	{
		// ensures the applet is not still synchronising before the callback is invoked
		var delayedSubmit = function()
		{
			//set a timer...
			var timer = setInterval(function()
					{
						debug('inPlaceCheckSynced: timer check...');
						if (!applet.hasPendingSync())
						{
							clearInterval(timer);
							debug('inPlaceCheckSynced: no pending sync, form submitting');
							doSubmit();
						}
					}, 300);
		}

		if (applet.isSynchronising())
		{
			alert(beingUploadedMessageCallback());
			delayedSubmit(); //will not double submit if the user clicks twice
			return false;
		}

		else if (applet.hasPendingSync())
		{
			var answer = confirm(changesDetectedConfirmationMessageCallback())
			if (answer)
			{
				debug('inPlaceCheckSynced: has pending sync, invoking syncFile');
				applet.syncFile();
				delayedSubmit();
				return false;
			}
		}
	}

	debug('inPlaceCheckSynced: no applet available OR no pending sync OR user selected no to sync changes, simply invoking submit');
	doSubmit();

	return false;
}

/**
 *  $editLinks is the div which holds both the edit and editWith links (as well as the applet)
 *  $openWithLink the 'open with another editor' link
 */  
function initInPlace($editLinks, $openWithLink)
{
	if (navigator.javaEnabled())
	{
		$editLinks.show();
		/*
		 * Ongoing issues with Applets on the Firefox-on-Mac configuration compels us to
		 * hide the "Open file with ..." function for those clients.  Chrome browser on Mac seems okay with applets.
		 */
		var isMac = /Mac/.test(navigator.platform);
		if (isMac && $.browser.mozilla)
		{
			$openWithLink.hide();
		}
	}
}
