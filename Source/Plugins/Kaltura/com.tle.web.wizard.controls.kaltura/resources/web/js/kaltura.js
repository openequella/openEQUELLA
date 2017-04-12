var entryIds;
var closeCallback;

function setupKCW(id, kcwVars )
{
	var height = kcwVars.height;
	var width = kcwVars.width;
	var endPoint = kcwVars.ep;
	var uiConfId = kcwVars.uiConfId;
	var flashVersion = kcwVars.flashVersion;

	entryIds = new Array()
	closeCallback = kcwVars.onClose;

	var params = getDefaultParams();

	var flashVars = {
		uid: "",
		partnerId: kcwVars.pid,
		ks: kcwVars.ks,
		afterAddEntry: "afterUpload",
		close: "onClose",
		permissions: 2,
		terms_of_use: "http://corp.kaltura.com/terms-of-use",
		showClose: false
	};

	swfobject.embedSWF(endPoint + "/kcw/ui_conf_id/" + uiConfId, id, width, height, flashVersion, "", flashVars, params);
}

function setupKDP(id, kdpVars)
{
	var height = kdpVars.height;
	var width = kdpVars.width;
	var flashVersion = kdpVars.flashVersion;
	var embedUrl = kdpVars.embedUrl;
	var params = getDefaultParams();
	var flashVars = "";

	swfobject.embedSWF(embedUrl, id, width, height, flashVersion, "", flashVars, params);
}

function getDefaultParams()
{
	return {
	        allowScriptAccess: "always",
	        allowNetworking: "all",
	        allowFullScreen: true,
	        wmode: "opaque"
		};
}

function afterUpload(entries)
{
	for(var i = 0; i < entries.length; i++ )
	{
		entryIds.push(entries[i]);
	}
}

function onClose()
{
	closeCallback(entryIds);
}

