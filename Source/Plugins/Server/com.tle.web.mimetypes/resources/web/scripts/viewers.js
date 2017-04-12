function openConfig(data, updateFunc, defaultData, showFunc, closeFunc, okButton, popFunc)
{
	okButton.unbind('click');
	okButton.bind('click', function(evt) 
		{
			updateFunc(); 
			closeFunc();
		}
	);
	if (!data)
	{
		data = defaultData;
	}
	popFunc(data);
	showFunc();
}