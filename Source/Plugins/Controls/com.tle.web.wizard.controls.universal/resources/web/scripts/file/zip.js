var zipTree;

function zipSelectAll(select)
{
	for (var prop in zipTree)
	{
	    if ( zipTree.hasOwnProperty(prop) )
	    {
	    	var nodeInfo = zipTree[prop];
	    	var children = nodeInfo.children;
	    	$('#' + prop).attr('checked', select);
	    	for (var i=0; i < children.length; i++)
	    	{
		    	// Unnecessary to recurse into folders from this level, zipTree is effectively a flat structure
	    		_doSelect(children[i], select, false);
	    	}
	    }
	}
}

// existing functionality is that to select a folder is to only select that folder's
// first-level leaf nodes. To recurse into folders (so selection of a non-root folder selects
// that folder's sub-tree) alter the code so the 3rd parameter of _doSelect is set to true. 

function zipSelect(elementId)
{
	var nodeInfo = zipTree[elementId];
	if (nodeInfo)
	{
		var children = nodeInfo.children;
		var select = $('#' + elementId).attr('checked');
		for (var i=0; i < children.length; i++)
		{
			// to recurse into folders (so a non-root folder selects that folder's sub-tree)
			// set the 3rd parameter here to true, in which case it would make sense to
			// 'keep intermediate folders' in ZipDetails.java
			_doSelect(children[i], select, false);
		}
	}
}

function _doSelect(elementId, select, recurseIntoFolders)
{
	var isFolder = false;
	var nodeInfo = zipTree[elementId];
	if (nodeInfo)
	{
		isFolder = nodeInfo.folder;
	}

	if (!isFolder)
	{
		$('#' + elementId).attr('checked', select);
	}
	else if (recurseIntoFolders)
	{
		// if we're recursing into folders, select the child folder itself, and step into it
		$('#' + elementId).attr('checked', select);
		zipSelect(elementId);
	}
}