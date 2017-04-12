(function($)
{
	$.fn.shufflegroup = function(multi)
	{
		return this.each(function()
		{
			var container = $(this);
			var select = $(multi.list);
			var addbutton = $(multi.addbutton);
			var edit = multi.edit;
			var controls = multi.controls;
			var opendialog = multi.opendialog;
			var editlabel = multi.editlabel;
			var dellabel = multi.dellabel;
			var editIndex = -1;

			container.bind('addMulti', addMulti);

			// Add all the list items on page load
			select.ready(function()
			{
				var nopts = multi.list.options.length;
				for(var i = 0; i < nopts; i++)
				{
					addListItem(multi.list.options[i].text);
				}
			});

			// Add from multiple controls
			function addMulti(event, values, texts)
			{
				var table = container.find('table.shuffle');
				if(table != null && editIndex != -1)
				{
					var rows = table.find('tr');
					//removeListItem(table, $(rows[editIndex]));

					if (values.length > 0 && texts.length > 0 && !container.hasClass('disabled'))
					{
						if(editListItem(texts, editIndex))
						{
							editOptionByIndex(multi.list, editIndex, texts, values);
						}
						editIndex = -1;
					}
					else
					{
						return false;
					}
				}
				else
				{
					if (values.length > 0 && texts.length > 0 && !container.hasClass('disabled'))
					{
						if(addListItem(texts))
						{
							addOption(multi.list, texts, values);
						}
					}
					else
					{
						return false;
					}
				}

			}



			function addListItem(text)
			{
				// if results list doesn't exist yet, create it
				if (!container.find('table.shuffle').length > 0)
				{
					container.prepend($('<table class="zebra selections shuffle" />'));
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
				var actionsTD = $('<td class="actions"><a class="shuffle-edit" href="javascript:void(0)">' + editlabel + '</a> | <a class="shuffle-remove" href="javascript:void(0)">' + dellabel + '</a></td>');
				table.append($('<tr class="' + c + '"/>').append(textTD, actionsTD));

				return true;
			}

			function editListItem(text, index)
			{
				var table = container.find('table.shuffle');
				var rows = table.find('tr');
				var row = $(rows[index]);

				var exists = false;
				rows.find('.shuffle-text').each(function() {
					if ($(this).text() == text)
					{
						exists = true;
					}
				});
				if (exists)
					return false;

				row.find('.shuffle-text').text(text);
				return true;
			}

			container.delegate('.shuffle-edit', 'click', function()
			{
				if (!container.hasClass('disabled'))
				{
					var row = $(this).parent().parent();
					var table = row.parent().parent();
					editIndex = row.index();

					// Set controls to be filled
					edit(controls, multi.list.options[row.index()].value);
					
					// Open dialog with controls filled					
					opendialog(true);
				}
			});

			container.delegate('.shuffle-remove', 'click', function()
			{
				if (!container.hasClass('disabled'))
				{
					var row = $(this).parent().parent();
					var table = row.parent().parent();

					removeListItem(table, row)
				}
			});

			function removeListItem(table, row)
			{
				// Remove
				var text = row.find('.shuffle-text').text();
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