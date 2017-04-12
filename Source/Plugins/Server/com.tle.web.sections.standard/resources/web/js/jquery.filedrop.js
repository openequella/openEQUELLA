/**
 *
 * Original code from http://code.google.com/p/dnd-file-upload/
 */
(function($)
{
	var opts = {};
	$.fn.dropzone = function(options)
	{
		options.capableBrowser = true;
		if (window.File && window.FileReader && window.FileList && window.Blob && window.FormData)
		{
			var blacklistedBrowsers = [ /opera.*Macintosh.*version\/12/i ];

			for ( var i = 0, _len = blacklistedBrowsers.length; i < _len; i++)
			{
				var regex = blacklistedBrowsers[i];
				if (regex.test(navigator.userAgent))
				{
					options.capableBrowser = false;
					continue;
				}
			}
		}
		else
		{
			options.capableBrowser = false;
		}
		// Extend our default options with those provided.
		opts = $.extend({}, $.fn.dropzone.defaults, options);
		var id = this.attr("id");
		$.fn.dropzone.dropzoneid = id;
		var dropzone = document.getElementById(id);
		if (options.capableBrowser)
		{
			var fileInput = $("<input>");
			fileInput.attr({
				id: id + "-fileinput",
				type : "file",
				multiple : "multiple"
			});
			fileInput.bind("change", change);
			fileInput.css({
				'opacity' : '0',
				'width' : '100%',
				'height' : '100%'
			});
			$(document).append(fileInput);
			
			fileInput.change(function()
			{
				var files;
				files = $(this).get(0).files;
			});
			dropzone.addEventListener("drop", drop, true);
			dropzone.addEventListener("dragenter", dragenter, true);
			dropzone.addEventListener("dragover", dragover, true);
			dropzone.addEventListener("dragstart", dragstart, true);

			$(dropzone).click(function()
			{
				fileInput.click();
			});
			var jQueryDropzone = $("#" + id);
			// jQueryDropzone.bind("dragenter", dragenter);
			// jQueryDropzone.bind("dragover", dragover);
		}
		else
		{
			$(dropzone).hide();
			if (!!opts.fallbackFormId)
			{
				$("#" + opts.fallbackFormId).show();

			}
			if (!!opts.fallbackHiddenIds)
			{
				var list = opts.fallbackHiddenIds;
				for ( var n = 0; n < list.length; n++)
				{
					$('#' + list[n]).hide();
				}
			}

			$.fn.dropzone.notCapableBrowser();
		}

		return this;
	};
	$.fn.dropzone.defaults = {
		method : "POST",
		maxFiles : -1,
		printLogs : false,
		fallbackFormId : '',
		fallbackHiddenIds : [],
		uploadId: '',
		// update upload speed every second
		uploadRateRefreshTime : 10
	};
	$.fn.dropzone.notCapableBrowser = function()
	{
	};
	// invoked when new files are dropped
	$.fn.dropzone.newFilesDropped = function()
	{
	};
	// invoked before upload started
	$.fn.dropzone.uploadBeforeStart = function(fileIndex, file, ruploadId)
	{
		return true;
	};
	// invoked when the upload for given file has been started
	$.fn.dropzone.uploadStarted = function(fileIndex, file, ruploadId)
	{
	};

	// invoked when the upload for given file has been finished
	$.fn.dropzone.uploadFinished = function(fileIndex, file, time, ruploadId)
	{
	};

	$.fn.dropzone.uploadValidate = function(fileIndex, file, ruploadId)
	{
		return true;
	}
	
	$.fn.dropzone.allUploadStarted = function(fileIndex, file, ruploadId)
	{
	};
	
	// all uploads finished
	$.fn.dropzone.allUploadFinished = function()
	{
	};

	// invoked when the progress for given file has changed
	$.fn.dropzone.fileUploadProgressUpdated = function(fileIndex, file, newProgress)
	{
	};

	// invoked when the upload speed of given file has changed
	$.fn.dropzone.fileUploadSpeedUpdated = function(fileIndex, file, KBperSecond)
	{
	};
	function dragstart(event)
	{
		event.preventDefault();
		return false;
	}
	function dragenter(event)
	{
		// event.stopPropagation();
		event.preventDefault();
		return false;
	}

	function dragover(event)
	{
		event.dataTransfer.dropEffect = "copy";
		// event.stopPropagation();
		event.preventDefault();
		return false;
	}

	function drop(event)
	{
		var dt = event.dataTransfer;
		var files = dt.files;
		event.preventDefault();
		uploadFiles(files);

		return false;
	}

	function log(logMsg)
	{
		if (opts.printLogs)
		{
			console && console.log(logMsg);
		}
	}

	function uploadFiles(files)
	{
		$.fn.dropzone.newFilesDropped();

		if (opts.maxFiles != -1 && files.length > opts.maxFiles)
		{
			alert("Accept maximum " + opts.maxFiles + "files.");
			return;
		}
		for ( var index = 0; index < files.length; index++)
		{
			var ruploadId = opts.uploadId + index;
			if (!$.fn.dropzone.uploadBeforeStart(index, file, ruploadId))
			{
				continue;
			}
			var file = files[index];

			if (!$.fn.dropzone.uploadValidate(index, file, ruploadId))
			{
				continue;
			}
			var reader = new FileReader();
			reader.onload = function()
			{
			};
			
			var callback = (function(i, f)
			{
				return function() {
					$.fn.dropzone.uploadFinished(i, f, 0, ruploadId);
				}
			})(index, file);
			$.fn.dropzone.uploadStarted(index, file, ruploadId);
			file.progressHandler = progress;
			file.loadHandler = load;
			file.fileIndex = index;
			try
			{
				opts.ajaxMethod(callback, ruploadId, file);
			} catch (ex)
			{
				alert(ex.message);
			}
		}
		for ( var index = 0; index < files.length; index++)
		{
			var ruploadId = opts.uploadId + index;
			$.fn.dropzone.allUploadStarted(index, file, ruploadId);
		}
		$.fn.dropzone.allUploadFinished();
	}

	function load(event)
	{
		var now = new Date().getTime();
		var timeDiff = now - this.downloadStartTime;
		//$.fn.dropzone.uploadFinished(this.fileIndex, this.fileObj, timeDiff);
		log("finished loading of file " + this.fileIndex);
	}

	function progress(event)
	{
		if (event.lengthComputable)
		{
			var percentage = Math.round((event.loaded * 100) / event.total);

			if (this.currentProgress != percentage)
			{

				log(this.fileIndex + " --> " + percentage + "%");

				this.currentProgress = percentage;
				$.fn.dropzone.fileUploadProgressUpdated(this.fileIndex, this.fileObj, this.currentProgress);

				var elapsed = new Date().getTime();
				if (!this.currentStart)
				{
					this.currentStart = elapsed;
				}
				if (!this.startData)
				{
					this.startData = 0;
				}
				var diffTime = elapsed - this.currentStart;
				if (diffTime >= opts.uploadRateRefreshTime)
				{
					var diffData = event.loaded - this.startData;
					var speed = diffData / diffTime; // in KB/sec

					$.fn.dropzone.fileUploadSpeedUpdated(this.fileIndex, this.fileObj, speed);

					this.startData = event.loaded;
					this.currentStart = elapsed;
				}
			}
		}
	}

	// invoked when the input field has changed and new files have been dropped
	// or selected
	function change(event)
	{
		event.preventDefault();
		// get all files ...
		var files = this.files;
		uploadFiles(files);
	}

})(jQuery);