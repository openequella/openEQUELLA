var AutoSubmit = 
{
	setupAutoSubmit: function(field, button)
	{
		$(field).keypress(function(evt)
		{
			if (AutoSubmit.isEnter(evt))
			{
				return false;
			}	
		});
		$(field).keydown(function(evt)
		{
			if (AutoSubmit.isEnter(evt))
			{
				$(button).click();
				return false;
			}
			return true;
		});
		$(field).bind("focus", function()
		{
			$(button).addClass("autoselect");
		});
		$(field).bind("blur", function()
		{
			$(button).removeClass("autoselect");
		});
	},
	
	isEnter: function(evt)
	{
		var key = 0;
		if (evt && evt.keyCode)
		{
			key = evt.keyCode;
		}
		else if (evt && evt.which)
		{
			key = evt.which;
		}
			
		if (key == 13)
		{
			return true;
		}
		return false;
	}
};