function setupToggle() 
{
	$('.maingroup > a.itemheading').toggle(
		function() 
		{
			$(this).next().slideUp().end().children("img")
				.attr({alt: 'Expand', src: 'images/treeplusicon.gif'});
		}, 
		function() 
		{
			$(this).next().slideDown().end().children("img")
				.attr({alt: 'Collapse', src: 'images/treeminusicon.gif'});
		}
	).click();
}

function toggleAllNow()
{
	$('.maingroup > a.itemheading').click();
}