(function($)
{
	var busy = false;
	var methods = {
		init : function(options)
		{
			var id = this.attr('id');
			$(options.addButton).click(function()
			{
				options.addAjax(function(results, status)
				{
					updateIncludes(results, function()
					{
						var parent = $("#" + id + "_groups");
						var newhtml = $(results.added.html).hide();
						WizardCtrl.setMessage(id, results.message);
						parent.append(newhtml);
						$.globalEval(results.added.script);
						options.disabler(results.disabled);
						newhtml.slideDown();
					});
				});
			});
			$("." + id + "repeater-remove").live('click', function()
			{
				if (busy)
				{
					return false;
				}
				if (!$(this).hasClass('disabled'))
				{
					busy = true;
					var index = $(this).data('repeater.index');
					options.removeAjax(function(results, status)
					{
						updateIncludes(results, function()
						{
							var parent = $("#" + id + "_groups");
							var $removing = $($(parent).children()[index]);
							$removing.slideUp(function()
							{
								$removing.remove();
								WizardCtrl.setMessage(id, results.message);
								var newhtml = $(results.added.html);
								parent.html(newhtml);
								$.globalEval(results.added.script);
								busy = false;
							});
							options.disabler(results.disabled);
						});
					}, index);
				}
			});
			$("." + id + "move-down").live('click', function()
			{
				if (busy)
				{
					return false;
				}
				else
				// if not disabled? is such a thing even possible
				{
					busy = true;
					var index = $(this).data('repeater.index');
					options.swapIndexAjax(function(results, status)
					{
						var parent = $("#" + id + "_groups");
						var moving = $(parent.children()[index]);
						var target = $(parent.children()[index + 1]);
						
						if(target.length < 1)
						{
							busy = false;
							return false;
						}
						updateIncludes(results, function()
						{
							moving.slideUp("slow", function()
							{
								var newhtml = $(results.added.html);
								parent.html(newhtml);
								$.globalEval(results.added.script);
								busy = false;
								options.disabler(results.disabled);
							});
							target.slideUp();
						});
						moving.slideDown("slow");
						target.slideDown("slow");
					}, index, index + 1);
				}
			});
			$("." + id + "move-up").live('click', function()
			{
				if (busy)
				{
					return false;
				}
				else
				{
					busy = true;
					var index = $(this).data('repeater.index');
					options.swapIndexAjax(function(results, status)
					{
						var parent = $("#" + id + "_groups");
						var moving = $(parent.children()[index]);
						var target = $(parent.children()[index - 1]);
						
						if(target.length < 1)
						{
							busy = false;
							return false;
						}
						updateIncludes(results, function()
						{
							moving.slideUp("slow", function()
							{
								var newhtml = $(results.added.html);
								parent.html(newhtml);
								$.globalEval(results.added.script);
								busy = false;
								options.disabler(results.disabled);
							});
							target.slideUp();
						});
						moving.slideDown("slow");
						target.slideDown("slow");
					}, index, index - 1);

				}
			});
		}
	};
	$.fn.repeater = function(method)
	{
		if (methods[method])
		{
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method)
		{
			return methods.init.apply(this, arguments);
		}
	}
})(jQuery);
