exports.postFile_ = function(options) {
  return function(errback, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", options.url, true);
    xhr.onerror = function() {
      errback(new Error("AJAX request failed: " + options.url));
    };
    xhr.onload = function() {
      callback({
        status: xhr.status,
        headers: [],
        response: xhr.response
      });
    };
    xhr.upload.addEventListener("progress", options.progress);
    xhr.responseType = "text";
    xhr.send(options.file);

    return function(cancelError, cancelErrback, cancelCallback) {
      try {
        xhr.abort();
      } catch (e) {
        return cancelErrback(e);
      }
      return cancelCallback();
    };
  };
};
