function modifyUsernameChanged($checkbox, $div, animate)
{
	var checked = $checkbox.attr('checked'); 
	if (checked == 'checked' || checked == true)
	{
		if (animate)
		{
			$div.slideDown(300);
		}
		else
		{
			$div.show();
		}
	}
	else
	{
		if (animate)
		{
			$div.slideUp(300);
		}
		else
		{
			$div.hide();
		}
	}
}