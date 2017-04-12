var FileUploadHandler = {
	dndFinishedCallback: function()
	{
		FileUploadHandler.dndScroll();
	},
	
	dndCheckUpload: function(func, uploadId) 
	{
		func(uploadId);
		FileUploadHandler.dndScroll();
//		setTimeout(
//			function()
//			{
//				func(uploadId);
//				FileUploadHandler.dndScroll();
//			},
//			200
//		);
	},
	
	dndScroll: function()
	{
		var container = $(".modal-content");
		var scrollTo = $("#filedndarea");
		container.animate({
			scrollTop: scrollTo.offset().top - container.offset().top + container.scrollTop() - 20
		});
	}
};