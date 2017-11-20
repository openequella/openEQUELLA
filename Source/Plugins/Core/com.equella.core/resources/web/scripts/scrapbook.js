var Scrapbook = {
	validateFile: function(done) {
        return function(f, xhr) {

            var newProgress = $('<div class="file-upload"><span class="file-name"></span>' +
            '<span class="file-upload-progress">' +
            '<div class="progressbar"></div>' +
            '</span></div>');
            newProgress.appendTo("#dndfiles");
            $(".file-name", newProgress).text(f.name);
            var progDiv = $(".progressbar", newProgress);

            xhr.onreadystatechange = function() {
                if (this.readyState == this.DONE) {
                    if (this.onreadystatechange) {
                        xhr.onreadystatechange = null;
                    }
                    if (this.status == 200)
                    {
                        var resp = JSON.parse(this.responseText)
                        done(function() {
                            progDiv.html('<div class="complete"/>');
                        }, resp.value, f.name);
                    }
                }
            }

            xhr.upload.addEventListener("progress", function(e) {
                var percent = 100 * (e.loaded / e.total);
                console.log("HIH");
                progDiv.progression({Current:percent, AnimateTimeOut : 600});
            });
            return true;
        }
	}
};