var ItemSummary = {
		selectItem: function($button, selectItemCallback, itemId, extensionType)
		{
			if (typeof CourseList != 'undefined')
			{
				CourseList.scrollToSelected();
				CourseList.transfer($button);
			}
			selectItemCallback(itemId, extensionType);
		}
};