function makeClickable($ul, callback)
{
	$ul.delegate('.lineItem', 'click', 
			function(event)
			{
					var $checkbox = $(this).find('.selection');
					$checkbox.attr('checked', !$checkbox.attr('checked'));
					if (callback)
					{
						callback();
					}
			}); 
	

	$ul.delegate('.selection', 'click', 
			function(event)
			{
				if (callback)
				{
					callback();
				}
				event.stopPropagation();
			}); 
	
}

function resourceSelected($resources, $summaryId, $packageId)
{
	if ( $summaryId.attr('checked') || $packageId.attr('checked') )
	{
		return true;
	}

	var selected = false; 
	$resources.each( function(){ if ($(this).attr('checked')){ selected = true; } });
	return selected;
}

function locationSelected($locations)
{
	var selected = false; 
	$locations.each( function(){ if ($(this).attr('checked')){ selected = true; } });
	return selected;
}

function swallowEnter(event)
{
	if (event.which == 13)
	{		
		event.preventDefault();
		return false;
	}
}

function filterCourses($tree, $boxId)
{
	var timer = $tree.data("timer");
	clearTimeout(timer);
	g_filteringTable = true;
	timer=setTimeout(function(){doFilter($tree, $boxId)}, 400);
	$tree.data("timer", timer);
}

function _show($elem, noAnimate)
{
	if (noAnimate) $elem.show(); else $elem.show('fast');
}

function _hide($elem, noAnimate)
{
	if (noAnimate) $elem.hide(); else $elem.hide('fast');
}

function doFilter($tree, $boxId, noAnimate)
{
	var filterText = $boxId.hasClass('blur') ? '' : $boxId.val().toLowerCase();
	
	$tree.children("li").each(function(index) 
		{
			var $this = $(this);
			var text = $this.find(".course").text(); 
			if (filterText == '' || ~text.toLowerCase().indexOf(filterText))
			{
				$this.addClass("filtered-in").removeClass("filtered-out");
			}
			else
			{
				$this.addClass("filtered-out").removeClass("filtered-in");	
			}
		});
		
	_hide($(".filtered-out"), noAnimate);
	_show($(".filtered-in"), noAnimate);
	
	if ($tree.children("li.filtered-in").length==0)
	{
		_show($("#no-results"), noAnimate);
	}
	else
	{
		_hide($("#no-results"), noAnimate);
		
		$tree.children("li.expandable, li.collapsable").each(function(index) 
				{
			$(this).removeClass("lastExpandable").removeClass("lastCollapsable");
			$(this).children(".hitarea").removeClass("lastExpandable-hitarea").removeClass("lastCollapsable-hitarea");
		});
		
		var last = $tree.children("li.filtered-in:last");
		if (last.hasClass("expandable"))
		{
			last.addClass("lastExpandable");
			last.children(".hitarea").addClass("lastExpandable-hitarea");
		}
		else
		{
			last.addClass("lastCollapsable");
			last.children(".hitarea").addClass("lastCollapsable-hitarea");
		}
	}	
	g_filteringTable = false;
}

function selectAllAttachments($cb)
{
	var checked = $cb.attr('checked')
	$('.attcontainer input[type=checkbox]').attr('checked', (checked ? true : false));
}