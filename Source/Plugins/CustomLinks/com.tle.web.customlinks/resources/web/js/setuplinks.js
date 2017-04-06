var ajaxFuncs = {};

function setupLinks(ajaxCallbacks, listId)
{
	var list = $(listId);
	var links = list.children("li");

	list.disableSelection();
	list.sortable(
	{
		placeholder : 'ui-state-highlight',
		stop : stopCallback,
		containment : '#col',

		// If the page is scrolled down in FF, the dragged object is
		// offset from the pointer by that much. Hax to fix it
		start : function(event, ui)
		{
			if ($.browser.mozilla && ui.helper !== undefined)
			{
				ui.helper.css('position', 'absolute').css('margin-top',
						$(window).scrollTop());
			}
		},
		beforeStop : function(event, ui)
		{
			if ($.browser.mozilla && ui.offset !== undefined)
			{
				ui.helper.css('margin-top', 0);
			}
		}
	});

	links.mousedown(function( )
	{
		links.removeClass("highlight");
		$(this).addClass("highlight");
	});
	ajaxFuncs.movedCallback = ajaxCallbacks.movedCallback;
}


function stopCallback(event, ui)
{
	var url = ui.item;
	var uuid = url.children("input").val();
	var position = url.prevAll().length;

	if (ajaxFuncs.movedCallback)
	{
		ajaxFuncs.movedCallback(null, uuid, position);
	}
}
