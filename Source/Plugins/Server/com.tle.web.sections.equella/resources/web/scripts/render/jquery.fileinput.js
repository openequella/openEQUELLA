/**
 * Based on the customfileinput plugin by The Filament Group, but with a bunch
 * of customisations.
 */
$.fn.customFileInput = function(options){

	var opts = $.extend({
		browseText: 'Browse',
		changeText: 'Change',
		noneSelectedText: 'No file selected...',
		extraButtonClasses: '',
		ajaxUploadForm: null,
		ajaxUploadIframe: null,
		ajaxUploadUrl: null,
		
		/**
		 * A callback function to create the progress div
		 */
		ajaxBeforeUpload: null,
		/**
		 * A callback function after the submit is called.  This is not when the submission has completed!
		 */
		ajaxAfterUpload: null,
		uploadId: null,
		/**
		 * Callback to catch files that go over size restriction for file attachment control
		 */
		errorCallback: null
	}, options);
	
	//create custom control container
	var $upload = $('<div class="customfile"></div>');
	//create custom control button (TODO: create keyboard controls) 
	var $uploadButton = $('<button class="customfile-button ' + opts.extraButtonClasses + ' focus" aria-hidden="true">' + opts.browseText + '</button>').appendTo($upload);
	//create custom control feedback
	var $uploadFeedback = $('<span class="customfile-feedback" aria-hidden="true">' + opts.noneSelectedText + '</span>').appendTo($upload);
	
	$uploadButton.on("keyup", function(event){
		if(event.which == '13')
		{
			$('input[type="file"]').click();
		}
	});
	
	//remove wait cursor, probably from a previous AJAX submit
	$('BODY').removeClass('waitcursor');

	var ajaxUpload = function($this, $form, $iframe, action, beforeUpload, afterUpload, errorCallback)
	{		
		var $input = $(this);
		
		$input.data('alreadySubmitted', true);
		var oldTarget = $form.attr('target');
		var oldAction = $form.attr('action');
		//make an iframe (TODO: this will keep creating new ones...)
		var $iframeToUse = $iframe;
		if (!$iframeToUse)
		{
			var i = 0;
			var $t_iframe = $('#ajaxupload' + i); 
			while ($t_iframe.length > 0)
			{
				i++;
				$t_iframe = $('#ajaxupload' + i); 
			}
			$iframeToUse = $('<iframe id="ajaxupload' + i + '" name="ajaxupload' + i + '" src="" class="ajaxupload">');
			$('body').append($iframeToUse);
		}
		
		//add a download progress bar
		if (beforeUpload)
		{
			beforeUpload($this);
		}

		var $uploadForm = $('#ajaxuploadForm');
		if ($uploadForm.length == 0)
		{
			$uploadForm = $('<form id="ajaxuploadForm" />');
			$('body').append($uploadForm);
		}
		$uploadForm.empty();
		$uploadForm.attr('target', $iframeToUse.attr('name'));
		$uploadForm.attr('action', action);
		$uploadForm.attr('method', $form.attr('method'));
		$uploadForm.attr('enctype', 'multipart/form-data');
		$uploadForm.attr('encoding', 'multipart/form-data');
		
		var $fileElems = $('input[type="file"]', $form);
		if ($fileElems.length == 1)
		{
			//let the server know it can take advantage of it
			$uploadForm.append('<input id="lazyUpload" name="lazyUpload" type="hidden" value="true">');
			$uploadForm.append('<input id="uploadId" name="uploadId" type="hidden" value="' + opts.uploadId + '">');
		}
		$.each($fileElems, function(index, fileList) {
			if ( $(this).attr('name') )
			{
				//!! you cannot clone a file input
				//var $input = $(this).clone();
				var $input = $(this);
				var fileSize = fileList.files[0].size;
				if(options.maxFilesize > 0 && fileSize > (options.maxFilesize * 1048576))
				{
					errorCallback(null, opts.uploadId);
				}
				//file is acceptable size, actually put file data in request
				else
				{
					$uploadForm.append($input);
				}
			}
		});
			$uploadForm.submit();
		
		$input.data('val', '');
		$input.val('');
		
		var after = function()
		{
			if (afterUpload)
			{
				afterUpload($this);
			}
			// fix for #7638
			fileInput.trigger('disable');
			fileInput.remove();
		};
		// Ugh... everyone loves setTimeout.  Basically you have to wait for 
		// $uploadForm.submit() to actually get started, and as always, there is no callback for this.
		setTimeout(after, 1000);
	}
	
	var fileInput = null;
	
	var doCheckChange = function()
	{
		var inputVal = fileInput.val(); 
		debug('fileUpload: checkChange called, inputVal -> ' + inputVal);
		var data = fileInput.data('val');
		debug('fileUpload: checkChange called, data(val) -> ' + data);
		if( typeof(data) != 'undefined' && inputVal != data )
		{
			fileInput.data('val', inputVal);
			fileInput.trigger('change');
		}
//		else if (inputVal == '')
//		{
//			fileInput.data('val', inputVal);
//			fileInput.trigger('change');
//		}
	};
	
	var doReset = function()
	{
		if (!opts.ajaxUploadUrl)
		{
			$uploadButton.val(opts.browseText);
		}
		else
		{
			//debug('customFileInput: removing wait cursor');
			$('BODY').removeClass('waitcursor');
			$uploadButton.show();
		}
		
		$uploadFeedback
			.text(opts.noneSelectedText)
			.removeClass('customfile-feedback-populated');
		fileInput.data('val', null);
	};
	
	var doChange = function()
	{
		//get file name
		var fileName = fileInput.val().split(/\\/).pop();
		
		//debug('customFileInput: change triggered');
		
		//update the feedback
		if( fileName.length > 0 ) 
		{
			$uploadFeedback
				.text(fileName)
				.addClass('customfile-feedback-populated');
			if (!opts.ajaxUploadUrl)
			{
				$uploadButton.val(opts.changeText);
			}
			else
			{
				$('BODY').addClass('waitcursor');
				$uploadButton.hide();
				// Ajax upload
				ajaxUpload(fileInput, opts.ajaxUploadForm, opts.ajaxUploadIframe, opts.ajaxUploadUrl, opts.ajaxBeforeUpload, opts.ajaxAfterUpload, opts.errorCallback);
			}
		} 
		else 
		{
			doReset();
		}
	};
	
	//apply events and styles for file input element
	fileInput = $(this)
		.addClass('customfile-input') //add class for CSS
		.attr("tabIndex", "-1")
		.mouseover(function(){ $upload.addClass('customfile-hover'); })
		.mouseout(function(){ $upload.removeClass('customfile-hover'); })		
		.blur(function(){ 
			$(this).trigger('checkChange');
		 })
		 .bind('disable',function(){
		 	fileInput.attr('disabled',true);
			$upload.addClass('customfile-disabled');
		})
		.bind('enable',function(){
			fileInput.removeAttr('disabled');
			$upload.removeClass('customfile-disabled');
		})
		.bind('checkChange', doCheckChange)
		.bind('change', doChange)
		.click(function(){ //for IE and Opera, make sure change fires after choosing a file, using an async callback
			setTimeout(doCheckChange,100);
		})
		.data('alreadySubmitted', false)
		.data('maxFileSize', false);
	
	//match disabled state
	if(fileInput.is('[disabled]')){
		fileInput.trigger('disable');
	}
	
	//on mousemove, keep file input under the cursor to steal click
	$upload
		.mousemove(function(e){
			var extra = $('html[dir="rtl"]').length == 0 ? fileInput.outerWidth() : 40;
			fileInput.css({
				'left': e.pageX - $upload.offset().left - extra + 20, //position right side 20px right of cursor X)
				'top': e.pageY - $upload.offset().top - 3
			});	
		})
		.insertAfter(fileInput); //insert after the input
	
	fileInput.appendTo($upload);
	
	//Find all reset buttons on the form and listen to their click event
	$('input[type="reset"]').bind("click.fileInput", doReset);
		
	//return jQuery
	return $(this);
};
