var WorkflowBulkOpDnd = {
	internalId : 0,
	uploads: 0,
	getDeleteButton : function(linkid)
	{
		return "<a id='" + linkid + "' href='javascript:void(0)' class='unselect'> </a>";
	},
	removeStagingFile: function(removeCb, filename, itemhtmlid) {
		removeCb(function() {
			$('#' + itemhtmlid).remove();
			WorkflowBulkOpDnd.uploads--;
		}, filename);
	},
	dndUploadFinishedCallback : function(file, ajaxResponse, removeCb)
	{
		var fileUrl = ajaxResponse.stagingFileUrl;
		WorkflowBulkOpDnd.internalId++;
		WorkflowBulkOpDnd.uploads++;
		var suffix = file.fileIndex + '-' + WorkflowBulkOpDnd.internalId + "-" + Math.random().toString(36).slice(-5);
		var itemhtmlid = 'wfmFile-li-' + suffix;
		var linkid = 'wfmFile-action-delete-' + suffix;
		$("#uploaded").append(
				" <span id='" + itemhtmlid + "'><a target='_blank' href='" + fileUrl + "'>" + file.name + "</a> "
						+ WorkflowBulkOpDnd.getDeleteButton(linkid) + "</span>");
		$("#" + linkid).click(function(e)
		{
			removeCb(function()
			{
				$('#' + itemhtmlid).remove();
				WorkflowBulkOpDnd.uploads--;
			}, file.name);
			return false;
		});
	},
	validateMessage: function($messageField, alertText) {
		if (WorkflowBulkOpDnd.uploads > 0)
		{
			if ($messageField.length > 0)
			{
				var text = $.trim($messageField.val());
				if ( text === '' )
				{
					alert(alertText);
					return false;
				}
			}
		}
		return true;
	}
};