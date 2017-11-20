(function($)
{
	$.fn.shufflelist = function(multi)
	{
		var editlabel = multi.edit;
		var deletelabel = multi.del;

		return this.each(function()
		{
			var container = $(this);
			var select = $(multi.list);
			var addbutton = $(multi.addbutton);
			var control = $(multi.control);


			// Add all the list items on page load
			select.ready(function()
			{
				var nopts = multi.list.options.length;
				for(var i = 0; i < nopts; i++)
				{
					addListItem(multi.list.options[i].text);
				}
			});

			// Add from single control
			addbutton.click(function()
			{
				if (control.val().length > 0 && !container.hasClass('disabled'))
				{
					var value = control.val();
					if(addListItem(value))
					{
						control.val('');
						addOption(multi.list, value, encodeURIComponent(value));
					}
				}
				else
				{
					alert(multi.alertmsg);
					return false;
				}
			});


			function addListItem(text)
			{
				// if results list doesn't exist yet, create it
				if (!container.find('table.shuffle').length > 0)
				{
					container.append($('<table class="zebra selections shuffle" />'));
				}

				var table = container.find('table.shuffle');
				var rows = table.find('tr');

				// if new item already exists in the list, do nothing
				var exists = false;
				rows.find('.shuffle-text').each(function() {
					if ($(this).text() == text)
					{
						exists = true;
					}
				});
				if (exists)
					return false;

				var c = rows.length % 2 == 0 ? 'even' : 'odd';
				var textTD = $('<td class="shuffle-text"/>').text(text);
				var actionsTD = $('<td class="actions"><a class="shuffle-edit" tabIndex="0" href="javascript:void(0)">' + editlabel + '</a> | <a class="shuffle-remove" tabIndex="0" href="javascript:void(0)">' + deletelabel + '</a></td>');
				table.append($('<tr class="' + c + '"/>').append(textTD, actionsTD));
				return true;
			}

			container.delegate('.shuffle-remove', 'click', function()
			{
				if (!container.hasClass('disabled'))
				{
					var row = $(this).parent().parent();
					var table = row.parent().parent();

					// Remove
					removeListItem(table, row);
				}
			});

			container.delegate('.shuffle-edit', 'click', function()
			{
				var row = $(this).parent().parent();
				var table = row.parent().parent();

				// Load value into edit box then remove
				control.val($("td.shuffle-text", row).text())

				// Remove
				removeListItem(table, row);

			});

			function removeListItem(table, row)
			{
				// Remove
				removeOptionByIndex(multi.list, row.index());
				row.remove();

				var rows = table.find('tr');

				if (rows.length > 0)
				{
					// adjust classes
					rows.each(function(index) {
						var row = $(this);
						var c = index % 2 == 0 ? 'even' : 'odd';

						if (c == 'even')
						{
							row.removeClass('odd').addClass('even');
						}
						else
						{
							row.removeClass('even').addClass('odd');
						}
					});
				}
				else
				{
					table.remove();
				}
			}
		});
	};
})
(jQuery);
