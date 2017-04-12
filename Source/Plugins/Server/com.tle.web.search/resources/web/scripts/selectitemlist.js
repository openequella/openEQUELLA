function selectItemList($button, selectCallback, itemId, extensionType)
{
	if (selectCallback)
	{
		if (typeof CourseList != 'undefined')
		{
			CourseList.scrollToSelected();
			CourseList.transfer($button);
		}
		selectCallback(itemId, extensionType);
	}
}