var WorkflowComments = {
	internalId : 0,
	getDeleteButton : function(linkid)
	{
		return "<a id='" + linkid + "' href='javascript:void(0)' class='unselect'> </a>";
	},
	validateFile: function (done) {
	    return function (f, xhr) {
            var $parediv = $("#current-uploads");
            var $progress = $('<div id="upload" class="progressbar"><div class="progress-bar-inner"/></div>');
            var $fullUpload = $('<div class="file-upload" />');
            var $fname = $('<span class="file-name" />').text(f.name);
            var $progspan = $('<span class="file-progress"/>');
            $progress.appendTo($progspan);
            $fullUpload.append($fname);
            $fullUpload.append($progspan);
            $parediv.append($fullUpload);
	        xhr.onreadystatechange = function() {
                if (this.readyState == this.DONE) {
                    if (this.onreadystatechange) {
                        xhr.onreadystatechange = null;
                        $fullUpload.remove();
                        done();
                    }
                }
            }
            xhr.upload.addEventListener("progress", function(e) {
                var percent = 100 * (e.loaded / e.total);
                $progress.progression({Current:percent, AnimateTimeOut : 600});
            });
	        return true;
	    }
	}
};