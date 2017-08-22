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
    addAttachmentEntry: function(cid, canceller, uid, filename) {
        var attachTable = $("#"+cid+"_a")
        var newEntry = $('<tr class="rowShown"><td class="name"><span class="nametext"></span><div id="u'+uid+
                            '" class="progress-bar"></div></td><td class="actions"><a class="unselect"></a></td></tr>');
        $(".nametext", newEntry).text(filename);
        $(".unselect", newEntry).bind('click', function(e) {
            canceller(uid);
            return false;
        });
        var tbody = $("tbody", attachTable);
        if ($("td", tbody).length == 1)
        {
            $("tr", tbody).remove();
        }
        var trCount = $("tr", tbody).length;
        newEntry.addClass(trCount & 1 == 0 ? "odd" : "even")
        newEntry.appendTo(tbody);
    },
    addUploadEntry: function(cid, canceller, uid, filename) {
        var uploadTable = $("#uploads")
        var newEntry = $('<div class="file-upload"><span class="file-name"></span><span class="file-upload-progress"><div id="u'+uid+
                            '" class="progress-bar"></div><a class="unselect"></a></span></div>');
        $(".file-name", newEntry).text(filename);
        $(".unselect", newEntry).bind('click', function(e) {
            canceller(uid);
            return false;
        });
        var progressTable = $(".uploadsprogress", uploadTable);
        newEntry.appendTo(progressTable);
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
            var uploadId = guid();
            xhr.onreadystatechange = function() {
                if (this.readyState == this.DONE) {
                    if (this.onreadystatechange) {
                        xhr.onreadystatechange = null;
                        done(uploadId);
                    }
                }
            }
	        startUpload(uploadId, f.name);
            xhr.setRequestHeader("X_UUID", uploadId);
            xhr.upload.addEventListener("progress", function(e) {
                var percent = 100 * (e.loaded / e.total);
                $('#u'+uploadId).progression({Current:percent, AnimateTimeOut : 100});
            });
	        return true;
	    }
	},
    setupZipProgress: function(checkCall, done) {
        var intId;
        intId = window.setInterval(function() {
            checkCall(function(r){
                if (r.finished)
                {
                    window.clearInterval(intId);
                    done();
                }
                else {
                    var percent = 100 * (r.upto / r.total);
                    $('#zipProgress').progression({Current:percent, AnimateTimeOut : 100});
                }
            });
        }, 500);
    },
};
