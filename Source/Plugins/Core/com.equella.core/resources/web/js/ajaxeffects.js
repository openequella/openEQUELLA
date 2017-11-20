var AjaxFX = {
	getParameter : function(params, paramName)
	{
		var param = 0;
		if (params && params[paramName])
		{
			param = params[paramName];
		}
		return param;
	},
	
	wrapSuccess : function(onSuccess)
	{
		return function()
		{
			if (onSuccess)
			{
				onSuccess.call();
			}
			$(document).trigger('equella_ajaxeffectfinished');
		};
	},
	
	//!! This should never be called externally because it doesn't invoke updateIncludes
	_updateWithHideShow : function(newContents, onSuccess, removeSpinner)
	{
		var success = this.wrapSuccess(onSuccess);
		
		$.each(newContents.html, function(key, html)
		{
			var upDiv = $(document.getElementById(key));
			upDiv.hide(AjaxFX.getParameter(html.params, 'hideEffect'), AjaxFX.getParameter(html.params, 'hideEffectParams'), function()
			{
				if (removeSpinner)
				{
					removeSpinner(upDiv);
				}
				upDiv.html(html.html);
				$.globalEval(html.script);
				// IE fix
				var effect = AjaxFX.getParameter(html.params, 'showEffect');
				if (effect == 0)
				{
					upDiv.show();
					success();
				}
				else
				{
					var effectParams = AjaxFX.getParameter(html.params, 'showEffectParams');
					upDiv.show(effect, effectParams, success);
				}
				onSuccess = null;
			});
		});
	},

	updateDomWithLoading : function(imageUrl, oldDiv, newContents, onSuccess)
	{
		if (newContents)
		{
			updateIncludes(newContents, function()
			{
				AjaxFX._updateWithHideShow(newContents, onSuccess, function(upDiv)
				{
					if (upDiv.attr("id") == oldDiv.attr("id"))
					{
						oldDiv.css('text-align', '');
						oldDiv.css('width', '');
						oldDiv.css('height', '');
					}
				});
			});
		}
		else
		{
			var h = oldDiv.css('height');
			if (h === 'auto' || h === '0px')
			{
				h = oldDiv.height();
			}
			else
			{
				h = parseInt(h);
			}
			oldDiv.css('height', h);
			
			var w = oldDiv.css('width');
			if (w === 'auto' || w === '0px')
			{	
				w = oldDiv.width();
			}
			else
			{
				w = parseInt(w);
			}
			oldDiv.css('width', w);
			
			var css = {
					'text-align': 'center',
					'width': w,
					'height': h
					};
			oldDiv.css(css).html('<img class="ajax-spinner" style="float: right, position:relative; top: '+ h/2 +'px" src="' + imageUrl + '">');
		}
	},
	updateDomWithActivity : function(imageUrl, oldDiv, newContents, onSuccess)
	{
		if (newContents)
		{
			updateIncludes(newContents, function()
			{
				AjaxFX._updateWithHideShow(newContents, onSuccess);
			});
		}
		else
		{
			var img = $("<img class='ajax-spinner' src='" + imageUrl + "'>");
			oldDiv.append(img);
		}
	},
	updateDomSilent : function(imageUrl, oldDiv, newContents, onSuccess)
	{
		if (newContents)
		{
			updateIncludes(newContents, function()
			{
				AjaxFX._updateWithHideShow(newContents, onSuccess);
			});
		}
	},
	fadeDomResults : function(oldDiv, newContents, onSuccess)
	{
		var success = this.wrapSuccess(onSuccess);
		if (newContents)
		{
			updateIncludes(newContents, function()
			{
				$.each(newContents.html, function(key, html)
				{
					var oldDiv = $("#" + key);
					oldDiv.fadeTo("normal", 0, function()
					{
						oldDiv.html(html.html).fadeTo("normal", 1, success)
						$.globalEval(html.script);
						success = null;
					});
				});
			});
		}
	},
	updateDomFadeIn : function(imageUrl, oldDiv, newContents, onSuccess)
	{
		var success = this.wrapSuccess(onSuccess);
		if (newContents)
		{
			updateIncludes(newContents, function()
			{
				$.each(newContents.html, function(key, html)
				{
					var upDiv = $("#" + key);
					upDiv.fadeTo("normal", 0, function()
					{
						oldDiv.css("text-align", "");
						upDiv.html(html.html).fadeTo("normal", 1, success)
						$.globalEval(html.script);
						success = null;
					});
				});
			});
		}
		else
		{
			oldDiv.css("text-align", "center").html("<img class=\"ajax-spinner\" src=\"" + imageUrl + "\">");
		}
	},
	fadeDom : function(imageUrl, oldDiv, newContents, onSuccess)
	{
		var success = this.wrapSuccess(onSuccess);
		var fading = oldDiv.data("fading");
		if (!newContents)
		{
			if (!fading)
			{
				oldDiv.data("fading", true).data("cssMinHeight", oldDiv.css("min-height")).data("cssMinWidth",
						oldDiv.css("min-width"));

				oldDiv.fadeOut("normal", function()
				{
					oldDiv.html("&nbsp;").show().css({
						"background-image" : "url('" + imageUrl + "')",
						"background-repeat" : "no-repeat",
						"background-position" : "center center",
						"min-height" : "24px",
						"min-width" : "24px"
					}).removeData("fading");

					if (oldDiv.data("newcontents"))
					{
						AjaxFX.fadeDom(imageUrl, oldDiv, oldDiv.data("newcontents"), success);
					}
				});
			}
		}
		else
		{
			if (!fading)
			{
				oldDiv.removeData("newcontents").hide().css({
					"background-image" : "",
					"min-height" : oldDiv.data("cssMinHeight"),
					"min-width" : oldDiv.data("cssMinWidth")
				}).removeData("cssMinHeight").removeData("cssMinWidth");

				updateIncludes(newContents, function()
				{
					$.each(newContents.html, function(key, html)
					{
						var upDiv = $("#" + key);
						oldDiv.css("text-align", "");
						upDiv.html(html.html).fadeTo("normal", 1, success);
						$.globalEval(html.script);
					});
				});
			}
			else
			{
				oldDiv.data("newcontents", newContents);
			}
		}
	},
	diffChildList : function(childMap, parentTag, diff)
	{
		var toRemove = !diff ? [] : diff.removed;
		var toAdd = !diff ? [] : diff.added;
		var toUpdate = !diff ? [] : diff.updated;
		var insertPoint = null;

		parentTag.children().each(function()
		{
			if (!childMap[this.id])
			{
				toRemove.push(this);
			}
		});

		$.each(childMap, function(key, content)
		{
			var origPage = $("#" + key);
			if (!origPage.length)
			{
				var newNode = $(content.html);
				newNode.hide();
				if (insertPoint == null)
				{
					parentTag.prepend(newNode);
				}
				else
				{
					insertPoint.after(newNode);
				}
				$.globalEval(content.script);
				insertPoint = newNode;
				toAdd.push(newNode[0]);
			}
			else
			{
				insertPoint = origPage;
				if (content.html)
				{
					toUpdate.push({oldNode:origPage, content:content});
				}
			}
		});
		return {
			removed: toRemove,
			added: toAdd,
			updated : toUpdate
		};
	}
}
