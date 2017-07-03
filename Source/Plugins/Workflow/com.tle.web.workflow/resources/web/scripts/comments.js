var WorkflowComments = {
	internalId : 0,
	getDeleteButton : function(linkid)
	{
		return "<a id='" + linkid + "' href='javascript:void(0)' class='unselect'> </a>";
	},
	removeStagingFile: function(removeCb, filename, itemhtmlid) {
		removeCb(function() {
			$('#' + itemhtmlid).remove();
		}, filename);
	},
	dndUploadFinishedCallback : function(file, ajaxResponse, removeCb)
	{
		var fileUrl = ajaxResponse.stagingFileUrl;
		WorkflowComments.internalId++;
		var suffix = file.fileIndex + '-' + WorkflowComments.internalId + "-" + Math.random().toString(36).slice(-5);
		var itemhtmlid = 'wfmFile-li-' + suffix;
		var linkid = 'wfmFile-action-delete-' + suffix;
		$("#uploaded").append(
				"<li id='" + itemhtmlid + "'><a target='_blank' href='" + fileUrl + "'>" + file.name + "</a> "
						+ WorkflowComments.getDeleteButton(linkid) + "</li>");
		$("#" + linkid).click(function(e)
		{
			removeCb(function()
			{
				$('#' + itemhtmlid).remove()
			}, file.name);
			return false;
		});
	}
};