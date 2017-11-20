/* updateBaseUrlMatches.js */

var timer; // must have scope outside of set/clear calling function
function timerUpdateBaseUrlMatches(enteredText, selectablePairs, selectedUuid, matchUrlWarningIdentifier)
{
	window.clearTimeout(timer);
	var millisecBeforeRedirect = 1500;
	timer = window.setTimeout(function()
		{
		updateBaseUrlMatches(enteredText, selectablePairs, selectedUuid, matchUrlWarningIdentifier)	  
		},millisecBeforeRedirect); 
}

/*
 * takes a double-level array of [n][2] (effectively a list of Pair<String, String>) and
 *  sets a warning where the non-empty textField content does not match the URL corresponding
 *  to the currently selected URL/uuid pair in the list
 */
function updateBaseUrlMatches(enteredText, selectablePairs, selectedUuid, matchUrlWarningIdentifier)
{
	matchUrlWarningIdentifier.hide();
	if( selectablePairs == null || enteredText == null || selectedUuid == null ||
			selectablePairs.length < 1 || enteredText.length < 1 || selectedUuid.length < 1 )
	{
		return;
	}
	for (var i = 0; i < selectablePairs.length; i++)
	{
		if( selectedUuid === selectablePairs[i][1] )
		{
			if( !(enteredText === selectablePairs[i][0]) )
			{
				matchUrlWarningIdentifier.show();
			}
			break;
		}
	}
}
