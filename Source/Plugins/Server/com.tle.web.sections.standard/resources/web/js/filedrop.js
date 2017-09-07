
function setupFileDrop(options, filedrag)
{
	if (filedrag){
    	var $file = $('#'+filedrag.id+"_file");
		$(filedrag).bind('click', function(e) {
			$file.click();
		});

		var FileDragHover = function(e) {
			e.stopPropagation();
			e.preventDefault();
		}
		var FileSelectHandler = function(e) {
			FileDragHover(e);
			var files = e.target.files || e.dataTransfer.files;
			for (var i = 0, f; f = files[i]; i++) {
				var xhr = null;
				xhr = new XMLHttpRequest();
				xhr.open("POST", options.ajaxUploadUrl, true);
				if (!options.validateFile || options.validateFile(f, xhr))
				{
					if (xhr)
					{
						xhr.setRequestHeader("X_FILENAME", window.btoa(unescape(encodeURIComponent(f.name))));
						xhr.send(f);
					}
				}
			}
		}
		filedrag.addEventListener("dragover", FileDragHover, false);
		filedrag.addEventListener("dragleave", FileDragHover, false);
		filedrag.addEventListener("drop", FileSelectHandler, false);
		$file.bind('change', FileSelectHandler);
    }
}