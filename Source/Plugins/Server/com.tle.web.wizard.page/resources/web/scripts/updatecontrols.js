var g_updatingControls = false;

function _getEffect(effect)
{
	if (effect == undefined)
	{
		effect = 0;
	}
	return effect;
}

function doUpdates(result)
{
	$.each(result.updates, function(key, html)
	{
		var $oldDiv = $("#" + key);
		$oldDiv.hide(_getEffect(html.params.hideEffect), function()
		{
			$oldDiv.html(html.html);
			$.globalEval(html.script);
			$oldDiv.show(_getEffect(html.params.showEffect));

		});
	});
	$("#affix-div").affix({ offset: {top: 140}});
}

function updatePageList(result)
{
	var listTag = $("#wizard-pagelist ul");
	if (!listTag.length)
	{
		return;
	}
	var newPages = result.listUpdates['wizard-pagelist-page'];
	var newPageMap = {};
	$.each(newPages, function()
	{
		this.html = $(this.html)[0];
		newPageMap[this.html.id] = this;
	});
	var result = AjaxFX.diffChildList(newPageMap, listTag);
	var removed = $(result.removed);
	var added = $(result.added);

	var showIt = function()
	{
		removed.remove();
		added.show("blind").effect("highlight");
	};
	if (removed.length)
	{
		removed.effect("highlight").hide("blind", showIt);
	}
	else
	{
		showIt();
	}
}

function updateControls(ajax, controlId, eventArgs, ajaxIds)
{
	if (g_updatingControls)
	{
		return false;
	}
	
	// IE before unload hack ( see #4096 )
	var unloader = window.onbeforeunload;
	window.onbeforeunload = null;

	g_updatingControls = true;
	var visibleControls = [];
	var controlParents = $(".wizard-parentcontrol");
	controlParents.each(function()
	{
		$(this).children().each(function()
		{
			visibleControls.push(this.id);
		});
	});
	var reloadData = {
		controlId : controlId,
		visibleIds : visibleControls,
		ajaxIds : ajaxIds,
		event : eventArgs
	};

	ajax(function(result)
	{
		updateIncludes(result, function()
		{
			doUpdates(result);
			updatePageList(result);

			var diff = null;
			controlParents.each(function()
			{
				var ids = result.controlIds[this.id];
				if (ids)
				{
					var ctrlMap = {};
					$.each(ids, function()
					{
						var val = result.contents[this];
						if (!val)
						{
							val = {};
						}
						ctrlMap[this] = val;
					});
					diff = AjaxFX.diffChildList(ctrlMap, $(this), diff);
				}
			});

			if (diff)
			{
				$.each(diff.updated, function()
				{
					this.oldNode.replaceWith(this.content.html);
					$.globalEval(this.content.script);
				});
				var toAdd = $(diff.added);
				var toRemove = $(diff.removed);

				var finishedSlide = function()
				{
					g_updatingControls = false;
				};
				var doSlideDown = function()
				{
					if (toAdd.length)
					{
						toAdd.slideDown(finishedSlide);
					}
					else
					{
						finishedSlide();
					}
				}
				if (toRemove.length)
				{
					toRemove.slideUp(function()
					{
						toRemove.remove();
						doSlideDown();
					});
				}
				else
				{
					doSlideDown();
				}
			}
			if (unloader)
			{
				window.onbeforeunload = unloader;
			}
		});
	}, JSON.stringify(reloadData));
}