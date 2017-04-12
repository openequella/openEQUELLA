
function setupKDP(id, kdpVars)
{
	var width = kdpVars.width;
	var height = kdpVars.height;
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