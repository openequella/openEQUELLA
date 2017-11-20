function setupShuffleBox(left, right, buttons, changeHandler) 
{
	$(buttons[0]).click(function(event) {
		moveAll(left, right);
		changeHandler();
		event.preventDefault();
	});
	
	$(buttons[1]).click(function(event) {
		moveSelected(left, right);
		changeHandler();
		event.preventDefault();
	});
	
	$(buttons[2]).click(function(event) {
		moveSelected(right, left);
		changeHandler();
		event.preventDefault();
	});
	
	$(buttons[3]).click(function(event) {
		moveAll(right, left);
		changeHandler();
		event.preventDefault();
	});
	
	$(left).change(function(event){
		updateButtons(left, right, buttons);
	});
	
	$(right).change(function(event){
		updateButtons(left, right, buttons);
	});
	
	updateButtons(left, right, buttons);
}

function updateButtons(left, right, buttons)
{
	if( !$(left).is(':disabled') && !$(right).is(':disabled') )
	{
		var lopts = left.options.length
		var lall = $(buttons[0]);
		var lone = $(buttons[1]);
		var lselopts = getSelectedTexts(left).length;
		
		var ropts = right.options.length;
		var rone = $(buttons[2]);
		var rall = $(buttons[3]);
		var rselopts = getSelectedTexts(right).length;
	
	

		if(lopts > 0)
		{
			enableDisableButton(lall, true);
			if(lselopts > 0)
			{
				enableDisableButton(lone, true);
			}
			else
			{
				enableDisableButton(lone, false);
			}
		}
		else
		{
			enableDisableButton(lall, false);
			enableDisableButton(lone, false)
		}
		
		if(ropts > 0)
		{
			enableDisableButton(rall, true);
			if(rselopts > 0)
			{
				enableDisableButton(rone, true);
			}
			else
			{
				enableDisableButton(rone, false);
			}
		}
		else
		{
			enableDisableButton(rall, false);
			enableDisableButton(rone, false)
		}
	}
}

function enableDisableButton(button, enable)
{
	if(enable)
	{
		button.removeAttr("disabled");
	}
	else
	{
		button.attr("disabled", "disabled");
	}
}

function disableShuffleBox(left, right, buttons, disable) 
{
	if (disable)
	{
		$(left).attr('disabled', 'disabled');
		$(right).attr('disabled', 'disabled');
		$(buttons).attr('disabled', 'disabled');
	}
	else
	{
		$(left).removeAttr('disabled');
		$(right).removeAttr('disabled');
		updateButtons(left, right, buttons);
	}
}