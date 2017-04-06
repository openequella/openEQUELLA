var ajaxFuncs = {};

function setupPortal(params, ajaxCallbacks)
{
	$('.dashboard #col1,.dashboard #col2,.dashboard #topwide').sortable(
			{
			connectWith: '.dashboard #col1,.dashboard #col2,.dashboard #topwide',
			//containment: '#content-inner',
			distance: 1,
			opacity: 0.7,
			placeholder: 'portlet_placeholder',
			handle: '.box_head h3',
			items: '.box',
			tolerance: 'pointer',
			revert: 300,
			start: startCallback,
			beforeStop: beforeStopCallback,
			stop: stopCallback,
			sort: sortCallback,
			forceHelperSize: true,
			forcePlaceholderSize: true,
			appendTo: '.dashboard',
			scrollSensitivity: 80,
			scrollSpeed: 20
			}
		);

	$('.dashboard .box_head').live('mouseenter', function() {
		$('img.action', this).stop(true, true).fadeIn();
	}).live('mouseleave', function() {
		$('img.action', this).stop(true, true).fadeOut();
	});

	ajaxFuncs.movedCallback = ajaxCallbacks.movedCallback;
}

function startCallback(event, ui)
{
	$('.dashboard #col1,.dashboard #col2,.dashboard #topwide').addClass("droptarget");
	$('.dashboard #topwide').css('padding-bottom', '80px');
}

function sortCallback(event, ui)
{
	if ($.browser.mozilla && ui.helper !== undefined)
	{
		ui.helper.css('position','absolute').css('margin-top', $(window).scrollTop());
	}
	$('.portlet_placeholder').height(ui.item.height());
}

function beforeStopCallback(event, ui)
{
	if ($.browser.mozilla && ui.helper !== undefined)
	{
		ui.helper.css('margin-top', '');
	}
}

//this relies heavily on the markup classes and ids...
function stopCallback(event, ui)
{
	$('.dashboard #col1,.dashboard #col2,.dashboard #topwide').removeClass("droptarget");
	$('.dashboard #topwide').css('padding-bottom','');
	
	var box = ui.item;
	var boxid = box.attr('id');
	var prefix = "pportlet_";
	//this could be looked up in a JS map initialised on load?
	var portlet = boxid.substring(prefix.length);

	var prevbox = box.prev('.box');
	var prevboxid = null;
	var prevPortlet = null;
	if (prevbox && prevbox.length > 0)
	{
		prevboxid = prevbox.attr('id');
		//this could be looked up in a JS map initialised on load?
		prevPortlet = prevboxid.substring(prefix.length);
	}
	
	var position;
	if(box.parents('#topwide') && box.parents('#topwide').length > 0)
	{
		position = 1;
		box.addClass('wide');
	}
	if(box.parents('#col1') && box.parents('#col1').length > 0)
	{
		position = 2;
		box.removeClass('wide');
	}
	if(box.parents('#col2') && box.parents('#col2').length > 0)
	{
		position = 3;
		box.removeClass('wide');	
	}
	
	if (ajaxFuncs.movedCallback)
	{
		ajaxFuncs.movedCallback(null, position, prevPortlet, portlet);
	}
}

function editPortlet(cb)
{
	return cb();
}
