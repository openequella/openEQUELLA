function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}


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
	},

	validateFile: function(maxSize, mimeTypes, errf, startUpload, done) {
	    return function(f, xhr) {
	        if (maxSize > 0 && f.size > maxSize) {
	            errf(f.name, "size");
	            return false;
	        }
	        if (mimeTypes.length > 0 && mimeTypes.indexOf(f.type) == -1) {
	            errf(f.name, "mime");
	            return false;
	        }
            xhr.onreadystatechange = function() {
                if (this.readyState == this.DONE) {
                    if (this.onreadystatechange) {
                        xhr.onreadystatechange = null;
                        done();
                    }
                }
            }
            var uploadId = guid();
            xhr.setRequestHeader("X_UUID", uploadId);
            xhr.upload.addEventListener("progress", function(e) {
                var $udiv = $('#u'+uploadId);
                var percent = 100 * (e.loaded / e.total);
                $udiv.progression({Current:percent, AnimateTimeOut : 600});
            });
	        startUpload(uploadId, f.name);
	        return true;
	    }
	},

};
