function register_drag_and_drop(options)
{
	var max_filename_length = 24;
	String.prototype.trunc = function(n)
	{
		return this.substr(0, n - 1) + (this.length > n ? '&hellip;' : '');
	};
	if (typeof options.uploadId == 'undefined')
	{
		options.uploadId == null;
	}
	if (typeof options.fallbackFormId == 'undefined')
	{
		options.fallbackFormId = null
	}
	if (typeof options.fallbackHiddenIds == 'undefined')
	{
		options.fallbackHiddenIds = null;
	}
	if (typeof options.banned == 'undefined')
	{
		options.banned = [];
	}
	if (typeof options.allowedMimes == 'undefined')
	{
		options.allowedMimes = [];
	}
	if (options.maxFilesize == 0)
	{
		options.maxFilesize = null;
	}
	
	$.fn.dropzone.notCapableBrowser = function()
	{
	};
	$.fn.dropzone.allUploadStarted = function(fileIndex, file, ruploadId)
	{
		//checkUpload
		if (typeof options.ajaxAfterUpload === 'function')
		{
			options.ajaxAfterUpload(ruploadId);
		}
	};
	$.fn.dropzone.uploadStarted = function(fileIndex, file, ruploadId)
	{
		if (!options.progressAreaId)
		{
			return;
		}
		var label = $("<div></div>");
		label.addClass('dropzone-label');
		label.attr("id", "dropzone-label-" + fileIndex);

		label.html(file.name.trunc(max_filename_length));

		var progress = $("<div></div>");
		progress.addClass('dropzone-progress');
		progress.attr("id", "dropzone-progress-" + fileIndex);

		var speedDiv = $("<span></span>");
		speedDiv.addClass('dropzone-speed');
		speedDiv.attr("id", "dropzone-speed-" + fileIndex);

		var fileDiv = $("<div></div>");
		fileDiv.addClass("dropzone-info");
		fileDiv.attr("id", "dropzone-info-" + fileIndex);
		label.append(speedDiv);
		fileDiv.append(label);
		fileDiv.append(progress);
		var container = $("#" + options.progressAreaId);
		container.prepend(fileDiv);

		$("#dropzone-progress-" + fileIndex).progression({
			Current : 0,
			AnimateTimeOut : 0
		});
	};
	$.fn.dropzone.uploadValidate = function(index, file, ruploadId)
	{
		var filename = file.name.toUpperCase();
		var fileMimetype = file.type;
		var fileSize = file.size;
		var valid = true;
		for ( var index in options.banned)
		{
			var ext = options.banned[index];
			valid = (filename.indexOf(ext, filename.length - ext.length) == -1);
			if (!valid)
			{
				alert("\"" + filename + "\" is a banned file type, it will be skipped");
				break;
			}
		}
		
		for ( var index in options.allowedMimes)
		{
			var mime = options.allowedMimes[index];
			valid = (fileMimetype == mime);
			if (valid)
			{
				break;
			}
		}
		if(!valid && options.allowedMimes.length > 0)
		{
			alert("\"" + filename + "\"" + options.mimeErrorMessage);
			return valid;
		}
		
		if(options.maxFilesize != null && (fileSize > (options.maxFilesize * 1048576)))
		{
			alert("\"" + filename + "\"" + options.maxFilesizeErrorMessage);
			valid = false;
		}
		return valid;
	}
	$.fn.dropzone.uploadBeforeStart = function(fileIndex, file, ruploadId)
	{
		if (typeof options.ajaxBeforeUpload === 'function')
		{
			return options.ajaxBeforeUpload(ruploadId);
		}
		return true;
	};

	$.fn.dropzone.uploadFinished = function(fileIndex, file, time, ruploadId)
	{
		var htmlid = "#dropzone-progress-" + fileIndex;
		$(htmlid).addClass('complete');
		$("#dropzone-label-" + fileIndex).html(file.name.trunc(max_filename_length));
		$("#dropzone-speed-" + fileIndex).remove();

		$(htmlid).progression({
			Current : 100,
			AnimateTimeOut : 0
		});
		if (typeof options.uploadFinished === 'function')
		{
			options.uploadFinished();
		}
	};

	$.fn.dropzone.fileUploadProgressUpdated = function(fileIndex, file, newProgress)
	{
		if (!options.progressAreaId)
		{
			return;
		}
		$("#dropzone-progress-" + fileIndex).progression({
			Current : newProgress,
			AnimateTimeOut : 0
		});
	};
	$.fn.dropzone.fileUploadSpeedUpdated = function(fileIndex, file, KBperSecond)
	{
		var dive = $("#dropzone-speed-" + fileIndex);
		dive.html(" " + getReadableSpeedString(KBperSecond) + " ");
	};
	$.fn.dropzone.newFilesDropped = function()
	{
		// $(".dropzone-info").remove();
	};
	$(".filedrop").dropzone({
		//url : options.ajaxUploadUrl,
		printLogs : true,
		uploadId : options.uploadId,
		ajaxMethod : options.ajaxMethod,
		maxFiles : options.maxFiles,
		fallbackFormId : options.fallbackFormId,
		fallbackHiddenIds : options.fallbackHiddenIds,
	});

	var base = 1024;

	function getReadableSpeedString(speedInKBytesPerSec)
	{
		var speed = speedInKBytesPerSec;
		speed = Math.round(speed * 10) / 10;
		if (speed < base)
		{
			return speed + "KB/s";
		}

		speed /= base;
		speed = Math.round(speed * 10) / 10;
		if (speed < base)
		{
			return speed + "MB/s";
		}

		return speed + "B/s";
	}

	function getReadableFileSizeString(fileSizeInBytes)
	{
		var fileSize = fileSizeInBytes;
		if (fileSize < base)
		{
			return fileSize + "B";
		}

		fileSize /= base;
		fileSize = Math.round(fileSize);
		if (fileSize < base)
		{
			return fileSize + "KB";
		}

		fileSize /= base;
		fileSize = Math.round(fileSize * 10) / 10;
		if (fileSize < base)
		{
			return fileSize + "MB";
		}

		return fileSizeInBytes + "B";
	}

	function getReadableDurationString(duration)
	{
		var elapsed = duration;

		var minutes, seconds;

		seconds = Math.floor(elapsed / 1000);
		minutes = Math.floor((seconds / 60));
		seconds = seconds - (minutes * 60);

		var str = "";
		if (minutes > 0)
			str += minutes + "m";

		str += seconds + "s";
		return str;
	}
}
