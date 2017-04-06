function periodEnabled($checkbox, $field1, $field2)
{
	var checked = $checkbox.attr('checked'); 
	if (checked == 'checked' || checked == true)
	{
		$field1.show();
		$field2.show();
	}
	else
	{
		$field1.hide();
		$field2.hide();	
	}
}