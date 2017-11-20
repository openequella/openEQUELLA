var ItemListAttachments = {
	toggle: function($toggler, $attachments, updateFunc, uuid, version)
	{
		if ($attachments.hasClass('opened'))
		{
			$attachments.removeClass('opened');
			updateFunc(uuid, version, false);
		}
		else
		{
			$attachments.addClass('opened');
			updateFunc(uuid, version, true);
		}
	},
	
	endToggle: function()
	{
		$(document).trigger('equella_showattachments');
	}
};