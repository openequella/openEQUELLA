(function($) 
{
	$.fn.criteria = function(options) 
	{
		return this.each(function() 
		{
			var settings = {
				"datewarning" : ""
			};
			
			if(options) 
			{ 
				$.extend( settings, options );
			}

			var container = $(this);
			var whereclauses = container.find('.whereclauses');
			var wherestart = container.find('.criteria-1 .wherestart');
			var whereoperator = container.find('.criteria-2 .whereoperator');
			var wherepath = container.find('.criteria-1 .wherepath');
			var wherevalue = container.find('.criteria-2 .wherevalue');
			var add = container.find('.whereadd');
			
			var whereoption = container.find('.criteria-1 .wherestart option[value="WHERE"]');
			var andoption = container.find('.criteria-1 .wherestart option[value="AND"]');
			var oroption = container.find('.criteria-1 .wherestart option[value="OR"]');
			
			whereclauses.ready(function()
			{
				var select = whereclauses.get(0);
				for(var i = 0; i < select.options.length; i++) 
				{
					var criteria = select.options[i].value;
					addWhereClause(criteria);
				}
			});
			
			// Removes AND/OR options from select on load. 
			wherestart.ready(function()
			{
				buildWhereStart(true);
			});
			
			
			add.click(function() 
			{
				var start = wherestart.val(), path = wherepath.val(), operator = whereoperator.val(), value = wherevalue.val();

				if (!start.length > 0 || !path.length > 0 || !operator.length > 0 || !value.length > 0) 
				{
					return false;
				} 
				else 
				{
					// if the operator is >, >=, <, <= then value must be a date
					var regexp = new RegExp(/^\d{4}-\d\d-\d\d((T)?(\s)?(\d\d:\d\d(:\d\d)?(Z)?)?)?$/gm);
					var o = operator;
					if(o == ">" || o == ">=" || o == "<" || o == "<=")
					{
						if(!regexp.test(value))
						{
							alert(settings.datewarning);
							return;
						}
					}
					
					var criteria = start + ' ' + path + ' ' + operator + ' \'' + value + '\'';
					if(addWhereClause(criteria))
					{	
						addOption(whereclauses.get(0), criteria, criteria);
						buildWhereStart(false);
					}
				}
			});
			
			function buildWhereStart(load)
			{
				var list = container.find('ul.criteria-list');
				var items = list.find('li');
				
				if(items.length == 0)
				{
					wherestart.children().remove();
					whereoption.appendTo(wherestart);
					if(!load)
					{
						wherestart.resetSS();
					}
				}
				else
				{
					wherestart.children().remove();
					andoption.appendTo(wherestart);
					oroption.appendTo(wherestart);
					andoption.attr("selected", "selected")
					if(!load)
					{
						wherestart.resetSS();
					}
				}
			}
			
			function addWhereClause(criteria)
			{				
				// if results list doesn't exist yet, create it
				if (!container.find('ul.criteria-list').length > 0) 
				{
					container.prepend($('<ul class="criteria-list" />'));
				}
				
				var list = container.find('ul.criteria-list');
				var items = list.find('li');
				
				// if new item already exists in the list, do nothing
				var exists = false;
				items.each(function() 
				{
					var text = $(this).text();
					var endtext = text.slice(text.indexOf(' '), text.length);
					var endcriteria = criteria.slice(criteria.indexOf(' '), criteria.length);
					
					if (endtext == endcriteria) 
					{
						exists = true;
					}
				});
				if (exists) 
					return false;

				var c = items.length % 2 != 0 ? 'even' : 'odd';
				list.append('<li class="' + c + '">' + criteria + '<span class="criteria-remove"></span></li>');
				return true;
			}

			container.find('.criteria-remove').live('click', function() 
			{
				var li = $(this).parent();
				var ul = li.parent();
				
				// Remove
				var value = li.text();
				removeOption(whereclauses.get(0), value);				
				li.remove();

				var items = ul.find('li');

				if (items.length > 0) {
					
					// adjust classes
					items.each(function(index) 
					{
						var item = $(this);
						var c = index % 2 != 0 ? 'even' : 'odd';
						
						// Ghetto hax to change the value
						if(index == 0)
						{
							var text = item.text();
							var remove = item.children(); // Remove button
							var start = text.slice(0, text.indexOf(' '));
							var end = text.slice(text.indexOf(' '), text.length);
							var newvalue = "";
							
							if (start.match("AND"))
							{
								start = "WHERE";
								
							}
							else if (start.match("OR"))
							{
								start = "WHERE";
							}
							
							newvalue = start + end;
							item.text(newvalue);
							editOption(whereclauses.get(0), text, newvalue);
							remove.appendTo(item);
						}

						if (c == 'even') 
						{
							item.removeClass('odd').addClass('even');
						} 
						else 
						{
							item.removeClass('even').addClass('odd');
						}
					});
				} 
				else 
				{
					ul.remove();
					
					buildWhereStart(false);
					
				}
			});


		});
	};
})
(jQuery);