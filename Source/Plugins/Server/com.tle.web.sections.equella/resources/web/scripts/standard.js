function _e(id)
{
	return document.getElementById(id);
}

function _f()
{
	return _e("eqpageForm");
}

function _trigger(event)
{
	var result = $(window).triggerHandler(event);
	if (result == undefined)
	{
		return true;
	}
	return result;
}

function _bind(event, forId, handler)
{
	if (event == 'beforeunload')
	{
		window.onbeforeunload = handler;
		return;

	}
	_bindW3C(event, forId, function(event)
	{
		var result = handler();
		if (result == false)
		{
			event.stopImmediatePropagation();
		}
		return result;
	});
}

var _globalEvents = new Object();

function _bindW3C(event, forId, handler)
{
	if (forId == null)
	{
		$(window).bind(event, handler);
	}
	else
	{
		var target = $(_e(forId));
		if (!target.length)
		{
			return;
		}
		target.bind(event, handler);
		var handlerKey = "_delegateHandler" + event;
		if (!target.data(handlerKey))
		{
			if (!_globalEvents[event])
			{
				$(window).bind(event + ".delegate", _delegateHandler);
				_globalEvents[event] = new Array();
			}
			target.data(handlerKey, true);
			_globalEvents[event].push(target);
		}
	}
}

function _delegateHandler(event)
{
	var result;
	var type = event.type;
	var delegates = _globalEvents[type];
	for ( var i = 0; i < delegates.length; i++)
	{
		result = delegates[i].triggerHandler(event);
		if (event.isImmediatePropagationStopped())
		{
			break;
		}
	}
	// Clean up no longer attached handlers
	var handlerKey = "_delegateHandler" + type;
	for ( var i = 0; i < delegates.length; i++)
	{
		if (!delegates[i].data(handlerKey))
		{
			delegates.splice(i, 1);
			i--;
		}
	}
	if (!delegates.length)
	{
		$(window).unbind(type + ".delegate");
		_globalEvents[type] = undefined;
	}
	return result;
}

if (typeof (debugEnabled) == 'undefined')
	var debugEnabled = false;
if (typeof (printStackTrace) == 'undefined')
	var printStackTrace = function()
	{
	};
if (typeof (debug) == 'undefined')
	var debug = function()
	{
	};
if (typeof (cls) == 'undefined')
	var cls = function()
	{
	};
if (typeof (serialise) == 'undefined')
	var serialise = function()
	{
		return '';
	};
if (typeof (window.console) == 'undefined')
{
	window.console = {
		log : function()
		{
		},
		debug : function()
		{
		},
		info : function()
		{
		},
		warn : function()
		{
		},
		error : function()
		{
		}
	}
}
