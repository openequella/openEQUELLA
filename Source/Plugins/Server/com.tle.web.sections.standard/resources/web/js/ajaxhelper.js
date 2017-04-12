var g_bSubmitting;
var g_updates = 0;
var focusOnClose;

$(function()
{
	// FIXME: these are english strings...
	$('BODY').ajaxError(function(event, response, settings, exception)
	{
		var mimetype = response.getResponseHeader("content-type") || "";
		$('BODY').removeClass("waitcursor");

		if (mimetype.indexOf('json') > -1)
		{
			var jsonstring = response.responseText;
			try
			{
				var ex = jQuery.parseJSON(jsonstring);
				alert('An error occurred on the server: ' + ex.message);
			} catch (e)
			{
				alert(e);
			}
		}
		else
		{
			// everything else including text/html
			var cause;
			var status = response.status;
			if (status == 0)
			{
				return;
			}
			if (status == 503)
			{
				alert('The server is unavailable');
				return;
			}
			else if (status == 500)
			{
				cause = 'Server error';
			}
			else if (status == 404)
			{
				cause = 'File not found';
			}
			else if (status == 403)
			{
				cause = 'Forbidden';
			}
			else if (status >= 200 && status < 400)
			{
				cause = exception;
			}
			else
			{
				cause = status;
			}

			var msg = 'An error occurred during the AJAX request';
			if (cause)
			{
				msg += ': ' + cause;
			}
			alert(msg);
		}
	});
});

function postAjaxEvent(action, formData, name, params, callback)
{
	setNameValue(formData, "event__", name);
	var progressHandler = function(e) {};
	var loadHandler = function(e) {};
	var hasFiles = false;
	var successCallback;
	var errorCallback;
	
	if($.isArray(callback))
	{
		successCallback = callback[0];
		errorCallback = callback[1];
	}
	else
	{
		successCallback = callback;
	}
	
	for ( var i = 0; i < params.length; i++)
	{
		var val = params[i];
		
		if ($.isArray(val) || $.isPlainObject(val))
		{
			val = JSON.stringify(val);
		}
		// handle dropped files
		if (typeof val == "object" && val != null) 
		{
			hasFiles = true;
			setNameValue(formData, "eventp__" + i, val.name);
			
			//FIXME: use a 'gather form data' callback or similar
			//This case ONLY applies to processUpload with the FileDrop control
			formData.push({
				name : 'uploadId',
				value : params[0]
			});
			formData.push({
				name : 'lazyUpload',
				value : 'true'
			});
			
			setNameValue(formData, "files[]", val);
			if (val.progressHandler)
			{
				progressHandler = val.progressHandler;
			}
			if (val.loadHandler) 
			{
				loadHandler = val.loadHandler;
			}
		}
		else
		{
			setNameValue(formData, "eventp__" + i, val);
		}
	}
	
	var ajaxOptions = {
		url : action,
		type : 'POST',
		dataType : "json",
		success : successCallback
	};
	
	if(typeof errorCallback != "undefined")
	{
		ajaxOptions.error = errorCallback;
		ajaxOptions.global = false; // Stops global error handler ajaxError 
	}
	
	if (!hasFiles || typeof FormData == "undefined") 
	{
		ajaxOptions.data = formData;
	} 
	else 
	{
	    fields = new FormData();
		for ( var i = 0; i < formData.length; i++)
		{
			fields.append(formData[i].name, formData[i].value);
		}
		ajaxOptions.xhr = function() 
		{
			var xhr = new XMLHttpRequest();
			if (typeof xhr.upload == 'undefined') 
			{
				return xhr;
			}
			if (typeof val == "object") 
			{
				xhr.fileIndex = xhr.upload.fileIndex = val.fileIndex;
				xhr.fileObj = xhr.upload.fileObj = val;
			}
			// Upload progress
			xhr.upload.addEventListener("progress",progressHandler, false);
			xhr.addEventListener("load", loadHandler, false);
			return xhr;
		}

		ajaxOptions.data = fields;
		ajaxOptions.contentType = false;
		ajaxOptions.processData = false;
	}

	$.ajax(ajaxOptions);

	return false;
}

function postAjaxJSON(form, name, params, callback, errorcallback)
{
	var f = $(form);
	_trigger("presubmit");
	var formData = $("input, select, textarea", f).serializeArray();
	return postAjaxEvent(f.attr("action"), formData, name, params, callback, errorcallback);
}

function setNameValue(formArray, name, value)
{
	var val = (value == null ? '$null$' : value);
	for ( var i = 0; i < formArray.length; i++)
	{
		if (formArray[i].name == name)
		{
			formArray[i].value = val;
			return;
		}
	}
	formArray.push({
		name : name,
		value : val
	});
}

var globalIncludes;
var globalCssIncludes;

function ensureIncludes()
{
	if (globalIncludes == null)
	{
		globalIncludes = {};
		$("script").each(function(index, elem)
		{
			var src = $(elem).attr("src");
			if (src != null)
			{
				globalIncludes[src] = true;
			}
		});
	}
	if (globalCssIncludes == null)
	{
		globalCssIncludes = {};
		$("link[rel=stylesheet]").each(function(index, elem)
		{
			var src = $(elem).attr("href");
			if (src != null)
			{
				globalCssIncludes[src] = true;
			}
		});
	}
}

function updateIncludes(newContents, onFinish)
{
	var wrappedFinish = function()
	{
		if (onFinish)
		{
			onFinish();
		}
		$(document).trigger('equella_ajaxcontent');
	}; 
	g_updates++;

	ensureIncludes();
	var jsIncludes = newContents.js;
	var cssIncludes = newContents.css;
	var newJs = new Array();
	for ( var i = 0; i < jsIncludes.length; i++)
	{
		var jsInc = jsIncludes[i];
		if (!globalIncludes[jsInc])
		{
			globalIncludes[jsInc] = true;
			newJs.push(jsInc);
		}
	}

	var newCss = [];
	var headDom = $("head")[0];
	for ( var i = 0; i < cssIncludes.length; i++)
	{
		var cssInc = cssIncludes[i];
		if (!globalCssIncludes[cssInc])
		{
			globalCssIncludes[cssInc] = true;

			var css;
			css = document.createElement('link');
			css.rel = 'stylesheet';
			css.type = 'text/css';
			css.media = "all";
			css.href = cssInc;

			document.getElementsByTagName("head")[0].appendChild(css);

			newCss.push(css);
			if ($.browser.msie && document.styleSheets.length > 30)
			{
				alert("Trying to load more than 31 stylesheets into IE, some styles will be broken");
				console.log("Trying to load more than 31 stylesheets into IE, some styles will be broken");
				break;
			}

		}
	}
	var execEnd = false;
	var jsFinished = false;
	var cssFinished = false;
	var intervalId;
	var MAX_CHECKS = 1000;
	var sanity = 0;
	var checkEnd = function()
	{
		if (!cssFinished && sanity < MAX_CHECKS)
		{
			var cssLoaded = true;
			for ( var i = 0; i < newCss.length; i++)
			{
				var cssStylesheet = newCss[i];
				cssLoaded = false;
				try
				{
					cssLoaded = (cssStylesheet.sheet && cssStylesheet.sheet.cssRules.length > 0)
							|| (cssStylesheet.styleSheet && cssStylesheet.styleSheet.cssText.length > 0)
							|| (cssStylesheet.innerHTML && cssStylesheet.innerHTML.length > 0);

				} catch (ex)
				{
					// access denied exceptions
				}
				if (!cssLoaded)
				{
					break;
				}
			}
			cssFinished = cssLoaded;
		}

		if (!execEnd && ((jsFinished && cssFinished) || sanity >= MAX_CHECKS))
		{
			execEnd = true;
			if (intervalId != undefined)
			{
				clearInterval(intervalId);
			}
			$.globalEval(newContents.script);
			wrappedFinish();
			g_updates--;
		}
		else if (!execEnd)
		{
			intervalId = setInterval(checkEnd, 200);
			sanity++;
		}

	}

	var loadNextScript = function()
	{
		if (!newJs.length)
		{
			jsFinished = true;
			checkEnd();
		}
		else
		{
			var sc = newJs.shift();
			$.getScript(sc, loadNextScript);
		}
	}

	updateFormAttributes(newContents);
	loadNextScript();
}

function registerAjaxSuccessCallback(cb)
{
	ajaxHelperSuccessCallbacks.push(cb);
}

function updateFormAttributes(newContents)
{
	var params = newContents.formParams;
	if (params)
	{
		var form = $("#" + params.id);
		if (params.action)
		{
			form.attr("action", params.action);
		}
		if (params.encoding)
		{
			form.attr("enctype", params.encoding);
		}
		var stateTag = $("._hiddenstate", form);
		var partial = params.partial;
		var newstate = [];
		for (var key in params.state)
		{
			if (partial)
			{
				$("input[name='" + key + "']", stateTag).remove();
			}
			var vals = params.state[key];
			for ( var i = 0; i < vals.length; i++)
			{
				var val = vals[i];
				newstate.push($('<input type="hidden" name="' + key + '" />').attr({
					value : val
				})[0]);
			}
		}
		if (!partial)
		{
			stateTag.empty();
		}
		stateTag.append(newstate);
	}
}

function _getDialogDiv(elemId)
{
	var dialogParent = $("#_ajax_dialog_parent");
	if (!dialogParent.length)
	{
		dialogParent = $("<div/>").attr({
			id : '_ajax_dialog_parent',
			style : "display:none;"
		});
		$("body").append(dialogParent);
	}
	var dialogDiv = $("#" + elemId, dialogParent);
	if (!dialogDiv.length)
	{
		dialogDiv = $("<div/>").attr({
			id : elemId,
			style : "height:100%"
		}).appendTo(dialogParent);
	}
	return dialogDiv;
}

function _dialogCallback(elemId, newContents, openCall, stopSpinner)
{
	updateIncludes(newContents, function()
	{
		var newBody = newContents.html['<BODY>'];
		var $oldContents = _getDialogDiv(elemId);

		// Hacky IE9 fix
		// http://msdn.microsoft.com/en-us/library/gg622929%28v=VS.85%29.aspx?ppud=4.
		var browser = $.browser;
		if (browser.msie && browser.version.slice(0, 1) == '9')
		{
			var $newHtml = $(newBody.html);
			var $iframe = $newHtml.find('iframe');
			if ($iframe.length > 0)
			{
				var src = $iframe.attr('src');
				$iframe.attr('src', 'javascript:void(0);');
				$oldContents.html($newHtml);
				$.globalEval(newBody.script);
				openCall();
				$iframe.attr('src', src);
				stopSpinner();
				return;
			}
		}

		$oldContents.html(newBody.html);
		$.globalEval(newBody.script);
		openCall();
		stopSpinner();
	});
}

function openAjaxDialog(elemId, form, name, params, openCall, startSpinner, stopSpinner)
{
	focusOnClose = $(':focus');
	startSpinner();
	postAjaxJSON(form, name, params, function(newContents, status)
	{
		_dialogCallback(elemId, newContents, openCall, stopSpinner);
	});
}

function openAjaxDialogUrl(elemId, url, name, params, openCall, startSpinner, stopSpinner)
{
	focusOnClose = $(':focus');
	startSpinner();
	postAjaxEvent(url, [], name, params, function(newContents, status)
	{
		_dialogCallback(elemId, newContents, openCall, stopSpinner);
	});
}

function closeAjaxDialog(elemId, closeCall)
{
	var $dialogParent = $("#_ajax_dialog_parent");
	$dialogParent.empty();
	closeCall();
	if (focusOnClose)
	{
		focusOnClose.focus();
	}
	
}

function submitBody(formElem, validate, blockFurtherSubmission)
{
	if (blockFurtherSubmission && g_bSubmitting)
	{
		return false;
	}

	g_bSubmitting = true;
	var $body = $('BODY');
	$body.addClass("waitcursor");
	if (validate)
	{
		if (!_trigger("validate"))
		{
			return false;
		}
	}
	var jsonParams = [];
	for ( var i = 3; i < arguments.length; i++)
	{
		jsonParams.push(arguments[i]);
	}
	postAjaxJSON(formElem, "$UP$<BODY>", jsonParams, function(newContents, status)
	{
		updateIncludes(newContents, function()
		{
			g_bSubmitting = false;
			$body.removeClass("waitcursor");
			var newBody = newContents.html['<BODY>'];
			var $newHtml = $(newBody.html);
			var $oldContents = $("#" + $newHtml.attr("id")).parent();
			// Yes this is a crazy workaround for #4567
			var $detachedIframes = $oldContents.find("iframe").detach();
			window.focus();
			$detachedIframes.remove();
			// End workaround
			$oldContents.html($newHtml);
			$.globalEval(newBody.script);
			setTimeout(function()
			{
				if ($('html.accessibility #fancybox-inner .focus').length != 0)
				{
					$('html.accessibility #fancybox-inner .focus')[0].focus();
				}
			}, 500);

		});
	});
}