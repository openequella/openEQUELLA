function searchFunction(callbacks)
{
	if ( callbacks )
	{
		for(var i = 0; i < callbacks.length; i++) 
		{
			callbacks[i].call(null);
		}
	}
}


function changedWhere(updaters, current, previous)
{
	//Dropdown not actually changed
	if (current == previous)
	{
		return;
	}
	
	// Advanced Search selected
	if(current.charAt(0) == 'P' )
	{
		fadeResultsOut();
		updaters.powerSelected();
	}
	else if(current.charAt(0) == 'R')
	{
		updaters.remoteSelected();
	}
	else
	{
		updaters.collectionSelected();
	}
}

function editQuery(updater)
{
	fadeResultsOut();
	updater();
}

// If we're in a skinny session, hide the sort and filter actions when in edit mode
function fadeResultsOut()
{
	if( $(".skinnysearch").size() > 0 )
	{
		$("#sortandfilter").hide();
	}
	$("#searchresults-outer-div").fadeOut();
}

function fadeResultsContainerIn()
{
	if(!$("#searchresults-outer-div").is(":visible"))
	{
		$("#searchresults-outer-div").fadeIn();
	}
}

function fadeResultsIn()
{
	$("#searchresults").fadeIn();
}

//If we're in a skinny session, restore the sort and filter actions when loading results from edit search
function restoreSkinnySearchActions()
{
	if( $(".skinnysearch").size() > 0 )
	{
		$("#sortandfilter").show();
	}
}
