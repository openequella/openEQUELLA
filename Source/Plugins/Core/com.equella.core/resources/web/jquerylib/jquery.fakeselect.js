// This is EQUELLA code guys, so don't bother looking for it on the JQuery
// Plug-ins site.  It hides and replicates a fake select list to allow for
// option text to wrap (see issue #2047).  You should make sure that both
// width and height styles are set on the select so that it knows how to
// wrap correctly.
//
// Usage: $("select").fakeselect();
//
jQuery.fn.fakeselect = function() {
	return this.each(function() {
		var select = jQuery(this);
		
		var list = jQuery("<ul></ul>");					
		jQuery("option", select).each(function() {
			jQuery('<li></li>').text(jQuery(this).text()).appendTo(list).click(function (){
				jQuery(this).siblings('li.selected').removeClass('selected').end().addClass('selected');
			
				var options = select.get(0).options;
				jQuery.each(options, function() {
					this.selected = false;
				});
				options[jQuery('li', list).index(this)].selected = true;
			});
		});
		
		var wrapper = jQuery('<div class="fakeselect"></div>').append(list).css({
			width: select.css('width'),
			height: select.css('height')
		});
		select.hide().after(wrapper);
	});
};