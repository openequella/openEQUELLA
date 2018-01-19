(function($)
{
	var busy = false;
	var methods = {
		init : function(options)
		{
			var id = this.attr('id');
			var $t = $('#' + id);

			options.disablers = options.disablers || [];

			var checkInsertButton = function(){
				$('.repeater-addtop').toggle($t.find('.repeater-groups').children().length !== 0);
			};
			checkInsertButton();

			var add = function(top){
            		return function(results, status)
            			{
            				updateIncludes(results, function()
            				{
            					var parent = $("#" + id + "_groups");
            					var $newhtml = $(results.added.html).hide();
            					WizardCtrl.setMessage(id, results.message);
            					if (top)
            					{
            						parent.html($newhtml);
            					}
            					else
            					{
            						parent.append($newhtml);
            					}
            					$.globalEval(results.added.script);
            					options.disablers.forEach(function(dis){dis(results.disabled);});
            					if (top)
            					{
            						var $first = $newhtml.first();
            						$newhtml = $newhtml.not($first);
            						$newhtml.show();
            						$first.slideDown(checkInsertButton);
            					}
            					else
            					{
            						$newhtml.slideDown(checkInsertButton);
            					}
            				});
            			};
            	};
			$(options.addButton).click(function()
			{
				options.addAjax(add(false), false);
			});
			$(options.addTopButton).click(function()
			{
				options.addAjax(add(true), true);
			});

			$(document).on('click.remove.repeater', "." + id + "repeater-remove", function()
			{
				if (busy)
				{
					return false;
				}
				if (!$(this).hasClass('disabled'))
				{
					if (confirm(options.confirmRemoveMessage || 'Remove this item?'))
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
									checkInsertButton();
								});
								options.disablers.forEach(function(dis){dis(results.disabled);});
							});
						}, index);
					}
				}
			});
			$(document).on('click.movedown.repeater', "." + id + "move-down", function()
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
								options.disablers.forEach(function(dis){dis(results.disabled);});
							});
							target.slideUp();
						});
						moving.slideDown("slow");
						target.slideDown("slow");
					}, index, index + 1);
				}
			});
			$(document).on('click.moveup.repeater', "." + id + "move-up", function()
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
								options.disablers.forEach(function(dis){dis(results.disabled);});
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
