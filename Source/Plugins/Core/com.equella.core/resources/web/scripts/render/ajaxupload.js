var AjaxUploads = {
    guid : function()
    {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    },
    addUploadEntry: function(parent, canceller, uid, filename, xhr) {
        var newEntry = $('<div class="file-upload"><span class="file-name"></span><span class="file-upload-progress"><div id="u'+uid+
                            '" class="progress-bar"></div><a class="unselect"></a></span></div>');
        $(".file-name", newEntry).text(filename);
        $(".unselect", newEntry).bind('click', function(e) {
            xhr.abort();
            canceller(uid);
            return false;
        });
        newEntry.appendTo(parent);
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
            var uploadId = AjaxUploads.guid();
            xhr.onreadystatechange = function() {
                if (this.readyState == this.DONE) {
                    if (this.onreadystatechange) {
                        xhr.onreadystatechange = null;
                        if (xhr.status == 200)
                        {
                            done(uploadId);
                        }
                    }
                }
            }
	        startUpload(uploadId, f.name, xhr);
            xhr.setRequestHeader("X_UUID", uploadId);
            xhr.upload.addEventListener("progress", function(e) {
                var percent = 100 * (e.loaded / e.total);
                $('#u'+uploadId).progression({Current:percent, AnimateTimeOut : 100});
            });
	        return true;
	    }
	}
}
