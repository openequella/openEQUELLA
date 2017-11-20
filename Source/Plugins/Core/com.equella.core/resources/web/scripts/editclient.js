function useDefaultRedirectChanged($checkbox, $div, animate)
{
	var checked = $checkbox.attr('checked'); 
	if (checked == 'checked' || checked == true)
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
	else
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
}