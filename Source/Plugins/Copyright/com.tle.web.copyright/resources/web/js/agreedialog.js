function acceptAgreement(acceptCall, closeDialog, id, attachUuid)
{
	acceptCall(function(result, status)
	{
		closeDialog();
		$('.copylink' + attachUuid).toggleClass("copylink_hidden");
		var opener = $('#' + id + '.copylink' + attachUuid);
		setTimeout(function(){
			
			opener.click();
		}, 1000);
	})
}