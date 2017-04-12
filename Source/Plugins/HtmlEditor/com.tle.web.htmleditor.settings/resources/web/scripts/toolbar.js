function setupToolbar($available, $currents, $container,initialDirty, ajaxCallbacks)
{
	var dirty = initialDirty;
	var getRow = function($toolbar)
	{
		if ($toolbar.hasClass('currenttoolbar0'))
		{
			return 0;
		}
		else if ($toolbar.hasClass('currenttoolbar1'))
		{
			return 1;
		}
		else if ($toolbar.hasClass('currenttoolbar2'))
		{
			return 2;
		}
		return -1;
	};
	var startCallback = function(event, ui)
	{
		var $button = ui.item;
		var $toolbar = $button.parent();
		var fromRow = getRow($toolbar);
		var fromIndex = $toolbar.children('.toolbarbutton').index($button);
		$button.data('fromRow', fromRow);
		$button.data('fromIndex', fromIndex);
	};
	var startAvailableCallback = function(event, ui)
	{
		startCallback(event, ui);
		var $button = ui.item;
		if ($button.hasClass('cloneable'))
		{
			var $helper = ui.helper;
			var $clone = $helper.clone(false);
			$clone.css('position', 'static');
			$button.after($clone);
			$button.data('clone', $clone);
		}
	};
	var stopCallback = function(event, ui)
	{
		var $button = ui.item;
		var $toolbar = $button.parent();
		
		var fromRow = $button.data('fromRow');
		var toRow = getRow($toolbar);
		var fromIndex = $button.data('fromIndex');
		var toIndex  = $toolbar.children('.toolbarbutton').index($button);
		
		//make sure the dynamicheighthack is always last
		$(this).find('.dynamicheighthack').appendTo(this);
		
		if (fromRow != toRow || fromIndex != toIndex)
		{
			if (ajaxCallbacks.movedCallback)
			{
				var buttonId = $button.attr('id');
				
				ajaxCallbacks.movedCallback(null, toRow, toIndex, fromRow, fromIndex, buttonId);
			}
			$button.data('fromRow', null);
			$button.data('fromIndex', null);
			
			if ($button.hasClass('cloneable') && fromRow > -1 && toRow == -1)
			{
				$button.remove();
			}
			
			var oldDirty = dirty;
			dirty = true;
			if (oldDirty != dirty && ajaxCallbacks.dirtyCallback)
			{
				ajaxCallbacks.dirtyCallback();
			}
		}
	};
	var stopAvailableCallback = function(event, ui)
	{
		var $button = ui.item;
		var $clone = $button.data('clone');
		if ($clone)
		{
			if ($button.parent().is($clone.parent()))
			{
				$clone.remove();
			}
			$button.data('clone',null);
		}
		
		stopCallback(event, ui);
	};
	var beforeStopCallback = function(event, ui)
	{
		if ($.browser.mozilla && ui.helper !== undefined)
		{
			ui.helper.css('margin-top', '');
		}
	};
	var sortCallback = function(event, ui)
	{
		if ($.browser.mozilla && ui.helper !== undefined)
		{
			ui.helper.css('position','absolute').css('margin-top', $(window).scrollTop());
		}
	};
	
	var currentIds = '#' + $($currents[0]).attr('id')
		+ ', #' + $($currents[1]).attr('id')
		+ ', #' + $($currents[2]).attr('id');
	var ids = '#' + $available.attr('id') + ', ' + currentIds;
	
	$available.sortable(
			{
			connectWith: currentIds,
			containment: '#' + $container.attr('id'),
			distance: 1,
			opacity: 0.7,
			placeholder: 'button_placeholder',
			items: '.toolbarbutton',
			tolerance: 'intersect',
			revert: 300,
			start: startAvailableCallback,
			beforeStop: beforeStopCallback,
			stop: stopAvailableCallback,
			sort: sortCallback,
			forceHelperSize: true,
			forcePlaceholderSize: true,
			scroll: false,
			helper: 'clone'
			}
		);
	$available.disableSelection();
	
	$currents.sortable(
			{
			connectWith: ids,
			containment: '#' + $container.attr('id'),
			distance: 1,
			opacity: 0.7,
			placeholder: 'button_placeholder',
			items: '.toolbarbutton',
			tolerance: 'intersect',
			revert: 300,
			start: startCallback,
			beforeStop: beforeStopCallback,
			stop: stopCallback,
			sort: sortCallback,
			forceHelperSize: true,
			forcePlaceholderSize: true,
			scroll: false,
			helper: 'original'
			}
		);
	$currents.disableSelection();
}