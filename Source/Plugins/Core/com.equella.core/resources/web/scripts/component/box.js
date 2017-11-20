function boxCallback(newContents, onSuccess, id)
{
	var wrappedSuccess = function()
	{
		if (onSuccess)
		{
			onSuccess(id, newContents.boxHtml, newContents.boxScript);
		}
		$(document).trigger('equella_ajaxeffectfinished');
	};
	updateIncludes(newContents, wrappedSuccess);
}