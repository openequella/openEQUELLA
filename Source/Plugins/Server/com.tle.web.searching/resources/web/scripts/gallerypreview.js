$(window).resize(setupPreviews);

var previewSelector = "div.gallery-preview";

var showTimer;
var hideTimer;

function setupPreviews()
{
	$("div.itemresult-wrapper").unbind("hover");
	$("div.itemresult-wrapper").hover(function()
	{
		preview($(this));
	}, function()
	{
		// compensate for the thumb-bar disappearing (joys of relative
		// positioning)
		$(this).find(previewSelector).css("bottom", "326px");
		$(this).find(previewSelector + ".resize").css("bottom", "296px");
		hidePreview($(this));
	});
}

function hidePreview($wrapper)
{
	clearTimeout(hideTimer);
	hideTimer = setTimeout(function()
	{
		$($wrapper).find(previewSelector).hide();
	}, 505);
}

function preview($wrapper)
{
	clearTimeout(showTimer);
	showTimer = setTimeout(function()
	{
		showPreview($wrapper);
	}, 300);
}

function showPreview($wrapper)
{
	$(previewSelector).hide();
	var $preview = $wrapper.find(previewSelector);

	var $onHoverDiv = $preview.find("div.onhover");
	var $image;
	if ($onHoverDiv.length > 0)
	{
		$image = $("<img/>")
		$image.error(function()
		{
			$(this).parent(previewSelector).remove();
		});
		$image.attr("src", $onHoverDiv.attr("data-img-src"));
		$onHoverDiv.replaceWith($image);
	}else
	{
		$image = $preview.find("img");
		if($image.length == 0)
		{
			$(this).parent(previewSelector).remove();
			return;
		}
	}
	var $arrow = $wrapper.find("div.preview-arrow");
	offset = $wrapper.offset();

	if (offset.left < $preview.width())
	{
		$arrow.removeClass("arrow-right");
		$arrow.addClass("arrow-left");
		$preview.css("right", "auto");
		$preview.css("left", "155px");
	}
	else
	{
		$arrow.removeClass("arrow-left");
		$arrow.addClass("arrow-right");
		$preview.css("left", "auto");
		if ($(window).width() < 1090)
		{
			$preview.css("right", "470px");
		}
		else
		{
			$preview.css("right", "520px");
		}
	}

	if ($(window).width() < 1090)
	{
		$image.addClass("resize");
		$preview.addClass("resize");
	}
	else
	{
		$(".resize").removeClass("resize");
	}

	$preview.css("bottom", "350px");
	$wrapper.find(previewSelector + ".resize").css("bottom", "320px");
	$preview.fadeIn();
	$preview.hover(function()
	{
		$(this).hide();
	});
}
