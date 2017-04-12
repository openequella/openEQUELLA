function drmLicenseResponse(type, id, closeDialog)
{
	closeDialog(function()
	{
		var removeLicenseLinks = function()
		{
			var licenseLinks = $('.drmlink_license');
			licenseLinks.toggleClass("drmlink_preview");
			licenseLinks.each(function()
			{
				this.onclick = null;
			});
			licenseLinks.unbind("click").click(function(event)
			{
				// Remove "drmLink" from id and click that
				var viewerLinkId = this.id.substring(0, this.id.length - 7);
				$('#' + viewerLinkId).click();
				event.preventDefault();
			});
			$('.drmlink_viewer').toggleClass("drmlink_viewer");
		};
		if (type == 'preview')
		{
			var $prv = $('#' + id + 'previewLink');
			$prv.click();
			removeLicenseLinks();
		}
		else
			if (type == 'accept')
			{
				removeLicenseLinks();
				$('#' + id).click();
			}
	});
}

function hideReject(id)
{
	if (history.length <= 1)
	{
		$('#' + id).hide();
	}
}