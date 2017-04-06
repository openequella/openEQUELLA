/**
 * settings must have a .tip
 */

jQuery.fn.tooltip = function(settings)
{
	settings = jQuery.extend( 
			{ 
				xOffset : 15, 
				yOffset : -5, 
				delay: 600, 
				tipelement: null, 
				tiphtml: null,
				mousePosition: true
		}, settings);	

	var $t = $(this);
	var $tipelement = null;
	if ( settings.tipelement )
	{
		$tipelement = settings.tipelement;
	}
	else
	{
		$tipelement = $('<div id="' + $t.attr('id') + '_tip" class="tooltip">' 
				+ settings.tiphtml + '</div>');
		$('BODY').prepend($tipelement);
	}
	
	var rtl = $('html[dir="rtl"]').size() > 0;
	var xname = rtl ? "right" : "left";
	
	var pauseTimer = null;
	var x = 0;
	var y = 0;
	var mousePos = settings.mousePosition;
		
	$t.hover(
		function(e) 
		{
			var $this = $(this);
			if (mousePos)
			{
				x = e.pageX;
				y = e.pageY;
			}
			else
			{
				var pos = $this.position(); 
				x = pos.left;
				y = pos.top;
			}
			pauseTimer = setTimeout(function(){
				applyCss({top: x, left: y}).fadeIn("fast"); 
				}, settings.delay);
	    }, 
	    function() 
	    {
	    	if (pauseTimer)
	    	{
	    		clearTimeout(pauseTimer);
	    	}
	    	$tipelement.fadeOut("fast");
	    }
	).mousemove(
		function(e) 
		{
			if (mousePos)
			{
				x = e.pageX;
				y = e.pageY;
			}
		}
	);
    
	function applyCss() 
	{
		var t = $tipelement.css("top", (y + settings.yOffset) + "px");
		if( !rtl ) 
		{
			t.css("left", (x + settings.xOffset) + "px");
		} else
			{
				t.css("left", (x - $tipelement.width() - settings.xOffset) + "px");
			}
		return t;	
	}
}