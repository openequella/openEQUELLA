//
//Sets it all up.  Use ProgressRenderer.SHOW_PROGRESS_FUNCTION 
//
// @param progressUrl A URL for the progress servlet.  Must include the id parameter e.g. .../progress?id=blah
// @param $bar A jquery selector for the DIV to use
// @param callback Method to call when this upload finishes.  Takes one parameter: uploadsInProgress (boolean)
//			e.g. if uploadsInProgress = true you probably won't want to refresh the page.
//
function showProgress(progressUrl, $bar, callback)
{
	var $progElem = null;
	if (typeof ($bar) == 'undefined')
	{
		//this comes from the file upload component when renderBar=true
		$progElem = $('.progressbar');
	}
	else
	{
		$progElem = $bar;
	}
	setupProgression(progressUrl, $progElem, null, callback);
}

var allUploads = [];

function _registerUpload($progElem)
{
	//don't duplicate registered uploads...
	var uploads = _getUploads();
	for (var i = 0; i < uploads.length; i++)
	{
		var upload = uploads[i];
		if (upload.id == $progElem.attr('id'))
		{
			return;
		}
	}
	uploads.push({
		id : $progElem.attr('id')
	});
}

function unregisterUpload($progElem)
{
	var uploads = _getUploads();
	for (var i = 0; i < uploads.length; i++)
	{
		var upload = uploads[i];
		if (upload.id == $progElem.attr('id'))
		{
			uploads.splice(i, 1);
			return;
		}
	}
}

//override in the webkit hack
function _getUploads()
{
	return allUploads;
}

function _uploadsInProgress($progElem)
{
	return _getUploads().length !== 0;
}

// dodgy iframe hack for webkits inability to process ajax after a submit
function setupWebkitFrame(jquery)
{
	if ($.browser.safari)
	{
		var crossOrigin = false;
		try
		{
			var $webkitProgressFrame = $('#webkitProgressFrame', window.parent.document);
		}
		catch (exception)
		{
			if (exception.code == 18)
			{
				//same-origin policy error, continue anyway (hax on hax on hax)
				crossOrigin = true;
			}
			else
			{
				throw exception;
			}
		}

		if (crossOrigin || $webkitProgressFrame.length === 0)
		{
			$webkitProgressFrame = $('<iframe id="webkitProgressFrame" name="webkitProgressFrame"></iframe>');
			var css = {
				width : '0',
				height : '0',
				position : 'absolute',
				top : -3000,
				visibility : 'hidden'
			};
			$webkitProgressFrame.css(css);

			$('BODY').append($webkitProgressFrame);

			var doc = $webkitProgressFrame[0].contentWindow.document;
			var base = document.getElementsByTagName("head")[0].baseURI;

			doc.open();
			doc.write('<html><head><base href="' + base + '"> </head><body></body></html>');
			doc.close();

			var docBody = doc.body;

			var jqueryInclude = doc.createElement('script');
			jqueryInclude.src = jquery;
			jqueryInclude.type = "text/javascript";
			docBody.appendChild(jqueryInclude);

			var progressFuncs = doc.createElement('script');
			progressFuncs.text = "function _getUploads(){return window.parent.allUploads;}\n" + updateProgression
					+ "\n" + unregisterUpload + "\n" + _uploadsInProgress + "\n";
			//debug('setupWebkitFrame: scripts -> ' + progressFuncs.text);
			docBody.appendChild(progressFuncs);
		}
		return $webkitProgressFrame;
	}
	return null;
}

function updateProgression(progressUrl, $progElem, timer, callback)
{
	function stopProgress()
	{
		timer.stop();
		$progElem.data('progressTimer', null);
	}
	
	function onError(data)
	{
		stopProgress();
		unregisterUpload($progElem);
		if (data.errorMessage != 'PROGRESSID_NOT_FOUND')
		{
			alert("Error uploading file: " + data.errorMessage);
		}
	}
	
	function jsonCallback(data, textStatus)
	{
		if (textStatus != "success" || data.errorMessage)
		{
			onError(data);
		}
		else
		{
			var percent = data.percent; 
			if (percent == -1)
			{
				//good enough for now
				percent = 50;
			}
			
			var progress = {
				Current : data.percent,
				AnimateTimeOut : 600
			};
			$progElem.progression(progress);
			
			if (data.finished)
			{
				stopProgress();
				unregisterUpload($progElem);
				
				var inProgress = _uploadsInProgress($progElem);
				
				if (data.forwardUrl !== '')
				{
					if (!inProgress)
					{
						parent.window.location = data.forwardUrl;
					}
				}
				else if (typeof (callback) == 'function')
				{
					callback(inProgress);
				}
			}
		}
	}
	
	$.getJSON(progressUrl, jsonCallback);
}

function setupProgression(progressUrl, $progElem, $fileElem, callback)
{
	if ($fileElem)
	{
		if ($fileElem.val() === '')
		{
			return;
		}
	}
	_registerUpload($progElem);
	function timerFunc(timer)
	{
		if ($.browser.safari)
		{
			var wkFrame = frames.webkitProgressFrame;
			if (typeof (wkFrame) == 'undefined')
			{
				throw 'Dev error: need to call setupWebkitFrame upon page load!';
			}
			wkFrame.updateProgression(progressUrl, $progElem, timer, callback);
		}
		else
		{
			updateProgression(progressUrl, $progElem, timer, callback);
		}
	}

	$progElem.css('display', 'block');
	$progElem.data('progressTimer', $.timer(600, timerFunc, $progElem));
}
