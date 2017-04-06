function setupDetails($usageTable) 
{
	$(".detailsRow").addClass("inactive");
	
	// Hide details on click anywhere
    $(document).bind('click.details',
    	function(e)
    	{
    		var $targ = $(e.target);
    		if (
	    			$targ.parents('#' + $usageTable.attr('id')).length == 0
	    			&& !$targ.hasClass('.itemDetails')
	    		)
	    	{
    			$(".detailsRow").removeClass('active');
    			$(".detailsRow").addClass('inactive');
				$.each($('.itemDetails'), 
						function(i, ele)
						{
							$(ele).hide()
						});
	    	}
    	}
    );
    
    var stopProp = function(e)
    {
    	e.stopImmediatePropagation();
    };
	
    $(".detailsRow").delegate('a', 'click.details', stopProp)
    .delegate('', 'click.details', 
    		function(e)
    		{
    				if (!$(this).hasClass('active'))
    				{
						$(".detailsRow").removeClass('active').addClass('inactive');
						$(this).addClass('active');
						$(this).removeClass('inactive');
			
						$.each($('.itemDetails'), 
						function(i, ele)
						{
							$(ele).hide()
						}); 
						$(this).find('.itemDetails').slideDown(300);
    				}
    		});
}
