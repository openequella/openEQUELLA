var Scrapbook = {
	dndUploadFinishedCallback: function() 
	{
		Scrapbook.dndScroll();
	},
	
	dndCheckUpload: function(func, uploadId) 
	{
		setTimeout(
			function()
			{
				func(uploadId);
				Scrapbook.dndScroll();
			},
			2000
		);
	},
	
	dndScroll: function()
	{
		var container = $("body");
		var scrollTo = $("#filedndarea");
		container.animate({
			scrollTop: scrollTo.offset().top - container.offset().top + container.scrollTop() - 20
		});	
	}
};