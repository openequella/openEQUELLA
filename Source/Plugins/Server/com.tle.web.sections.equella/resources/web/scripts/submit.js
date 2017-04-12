var g_bSubmitting = false;
function _dosub(validate)
{
	if (g_bSubmitting)
	{
		alert(i18n_com_tle_web_sections_equella_alreadysubmitting);
		return false;
	}
	g_bSubmitting = true;
	_trigger("presubmit");
	if (validate)
	{
		if (!_trigger("validate"))
		{
			return false;
		}
		$(window).unbind("beforeunload");
		window.onbeforeunload = function()
		{
		};
	}
	window.onpagehide = function()
	{
		g_bSubmitting = false;
	}

	var oldbefore = window.onbeforeunload;
	if (oldbefore)
	{
		window.onbeforeunload = function(e)
		{
			var result = oldbefore.call(window, e);
			if (result)
			{
				g_bSubmitting = false;
				return result;
			}
			return undefined;
		}
	}

	$(_f()).submit();
	return false;
}

function _sub()
{
	_dosub(true);
}

function _subnv()
{
	_dosub(false);
}

function _subev()
{
	_setupEvent(arguments);
	_dosub(true);
}

function _subevnv()
{
	_setupEvent(arguments);
	_dosub(false);
}

function _setupEvent(args)
{
	_setHidden("event__", args[0]);
	for ( var i = 1; i < args.length; i++)
	{
		var val = args[i];
		if ($.isArray(val) || $.isPlainObject(val))
		{
			val = JSON.stringify(val);
		}
		_setHidden("eventp__" + (i - 1), val);
	}
}

function _setHidden(name, value)
{
	var val = (value == null ? '$null$' : value);
	var hid = $("#" + name);
	if (hid.length)
	{
		hid.val(val);
	}
	else
	{
		$("<input/>").attr({
			type : 'hidden',
			name : name,
			id : name,
			value : val
		}).prependTo(_f());
	}
}
