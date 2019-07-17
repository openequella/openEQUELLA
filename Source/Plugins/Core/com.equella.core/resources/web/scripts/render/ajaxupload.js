var AjaxUploads = {
  guid: function() {
    function s4() {
      return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
    }
    return (
      s4() +
      s4() +
      "-" +
      s4() +
      "-" +
      s4() +
      "-" +
      s4() +
      "-" +
      s4() +
      s4() +
      s4()
    );
  },
  simpleUploadEntry: function(parent, canceller, uid, filename, xhr) {
    var newEntry = $(
      '<div class="file-upload"><div id="u' +
        uid +
        '" class="progress-bar"></div></div>'
    );
    newEntry.appendTo(parent);
  },
  addUploadEntry: function(parent, canceller, uid, filename, xhr) {
    var newEntry = $(
      '<div class="file-upload"><span class="file-name"></span><span class="file-upload-progress"><div id="u' +
        uid +
        '" class="progress-bar"></div><a class="unselect"></a></span></div>'
    );
    $(".file-name", newEntry).text(filename);
    $(".unselect", newEntry).bind("click", function(e) {
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
      startUpload(uploadId, f.name, xhr);
      xhr.setRequestHeader("X-UUID", uploadId);
      xhr.onerror = function() {
        done(uploadId, { code: 500, error: "Interal server error" });
      };
      xhr.onload = function() {
        var result;
        if (xhr.status == 200) {
          result = xhr.response;
        } else {
          result = { code: xhr.status, error: xhr.statusText };
        }
        done(uploadId, result);
      };
      xhr.upload.addEventListener("progress", function(e) {
        var percent = 100 * (e.loaded / e.total);
        $("#u" + uploadId).progression({
          Current: percent,
          AnimateTimeOut: 100
        });
      });
      return true;
    };
  }
};
