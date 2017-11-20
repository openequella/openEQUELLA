var _ajaxBusy = false;
var g_bSubmitting;
var g_updatingControls;
var g_filteringTable;
var g_updates;

window.confirm = function(msg) {
    return true;
}

$(function()
{
	$("HTML").ajaxStart(function()
	{
		_ajaxBusy = true;
	}).ajaxStop(function()
	{
		_ajaxBusy = false;
	});

	window.onerror = function(msg, url, line)
	{
		$("body").attr("jserror-msg", msg);
		$("body").attr("jserror-url", url);
		$("body").attr("jserror-line", line);
		alertDiv("msg: " + msg + " \nurl:" + url + " \nline:" + line);
	}

});

$.fx.off = true;

function _checkBusy()
{
	return _ajaxBusy ? 1 : 0 | g_bSubmitting ? 2 : 0 | g_updatingControls ? 4 : 0 | g_filteringTable ? 8 : 0 | g_updates ? 16 : 0;
}

function alertDiv(msg)
{
	$("body").append(
			"<div style='position: fixed; left: 50%; top: 50%; background: gray; "
					+ "width: 50%; height: 50%; margin-top: -15%; margin-left: -25%; opacity: 0.8; text-color: white; font-size: 32px;'>" + msg + "</div>");
}
