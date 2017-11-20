var VideoResults = {
	setupHover: function($elem, ajaxCallbacks)
	{
		var $parent = $elem.parents("div.itemresult-wrapper");
		var $previewDiv = $parent.find("div.video-preview");
		var $placeholder = $parent.find("div.video-preview-placeholder");
			
		$(document).bind("click", function()
		{
			$previewDiv.hide();
		});
	
		$previewDiv.bind("click", function(e)
		{
			e.stopPropagation();
		});
		
		var clearTimer = function(timerName, $preview)
		{
			var $div = $preview || $previewDiv;
			var timer = $div.data(timerName);
			if (timer != null)
			{
				clearTimeout(timer);
				$div.data(timerName, null);
				return true;
			}
			return false;
		};
		
		var fixOffset = function($preview)
		{
			var $window = $(window); 
			if ($window.width() < 1090)
			{
				var scrollLeft = $window.scrollLeft();
				var offset = $preview.parent().offset();
				
				var leftToBrowser = offset.left - scrollLeft;
				
				if (leftToBrowser < ($preview.width() / 2))
				{
					$preview.css("left", "135px");
				}
	
				if ((offset.left + $preview.width()) > $window.width())
				{
					$preview.css("right", "135px");
				}
			}
			else
			{
				$preview.css({'left' : '','right' : ''});
			}
		};
		
		var doOpen = function()
		{
			if (!clearTimer('openWaitTimer'))
			{
				debug('no longer waiting to open');
				return;
			}
			
			$("div.video-preview").each(function(index)
				{
					var $lookat = $(this);
					if (!$previewDiv.is($lookat) )
					{
						//prevent any queued up opens for things we aren't hovering
						clearTimer('openWaitTimer', $lookat);
						$lookat.hide();
					}
				});
			
			if (!$previewDiv.data('showing'))
			{
				$previewDiv.data('showing', true);
				
				if (!$previewDiv.is(':visible'))
				{
					debug("establish new preview");
					
					//clone contents of placeholder into preview div
					var $cloned = $placeholder.clone();
					$previewDiv.append($cloned);
					fixOffset($previewDiv);	
					$previewDiv.show();
					$cloned.show();
					
					ajaxCallbacks();
				}
				else
				{
					debug("apparently still visible..");
				}
			}
		};
		
		$parent.hover(
				function()
				{
					if (clearTimer('closeWaitTimer') && $previewDiv.data('showing'))
					{
						debug("prevented rebuild");
						return;
					}
					
					var openWaitTimer = setTimeout(doOpen, 500);
					$previewDiv.data('openWaitTimer', openWaitTimer);
				}, 
				function(e)
				{					
					debug("initiate fade out");
					if (clearTimer('openWaitTimer'))
					{
						debug('cancelled pending open');
					}
					
					var closeWaitTimer = setTimeout(function()
					{
						$previewDiv.data('closeWaitTimer', null);
						
						if (clearTimer('openWaitTimer'))
						{
							debug('cancelled pending open (in close)');
						}
						
						debug("fade out and removal");
						$previewDiv.data('showing', false);
						$previewDiv.fadeOut();
						$previewDiv.empty();
					}, 500);
					
					$previewDiv.data('closeWaitTimer', closeWaitTimer);
				}
		);
	},
	
	videoAjaxSuccess: function($elem)
	{
		var $parent = $elem.parents("div.itemresult-wrapper");
		var $previewDiv = $parent.find("div.video-preview");
		if (!$previewDiv.data('showing'))
		{
			debug('ajax success after video already hidden');
			$previewDiv.hide();
		}
	}
};