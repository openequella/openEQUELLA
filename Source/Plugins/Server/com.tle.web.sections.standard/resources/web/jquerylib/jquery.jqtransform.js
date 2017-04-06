/*
 *
 * jqTransform
 * by mathieu vilaplana mvilaplana@dfc-e.com
 * Designer ghyslain armand garmand@dfc-e.com
 * Massively modified by Aaron Holland 
 *
 * Version 1.0 25.09.08
 * Version 1.1 06.08.09
 * Add event click on Checkbox and Radio
 * Auto calculate the size of a select element
 * Can now, disabled the elements
 * Correct bug in ff if click on select (overflow=hidden)
 * No need any more preloading !!
 * 
 ******************************************** */
 
(function($){
	var defaultOptions = 
	{ 
			selectOpenerClass: 'jqTransformSelectOpen',
			wrapperClass: 'jqTransformSelectWrapper'
	};
	
	/* Hide all open selects */
	var jqTransformHideSelect = function(opts, oTarget)
	{
		var wrapperClass = opts.wrapperClass;
		
		var ulVisible = $('.'+wrapperClass+' ul:visible');
		ulVisible.each(function(){
			var oSelect = $(this).parents('.'+wrapperClass+':first').find('select').get(0);
			//do not hide if click on the label object associated to the select
			if( !(oTarget && oSelect.oLabel && oSelect.oLabel.get(0) == oTarget.get(0)) ){$(this).hide();}
		});
	};

	/* Apply document listener */
	var jqTransformAddDocumentListener = function (opts)
	{
		var wrapperClass = opts.wrapperClass;
		
		$(document).mousedown(
			function(event) 
			{
				if ($(event.target).parents('.' + wrapperClass).length === 0) 
				{ 
					jqTransformHideSelect(opts, $(event.target)); 
				}
			}
		);
	};	
			
	/* Add a new handler for the reset action */
	var jqTransformReset = function($select){
		var sel;
		$select.each(function(){sel = (this.selectedIndex<0) ? 0 : this.selectedIndex; $('ul', $(this).parent()).each(function(){$('a:eq('+ sel +')', this).click();});});
	};
	
	/***************************
	  Select 
	 ***************************/	
	$.fn._jqTransSelect = function(opts)
	{
		var openerClass = opts.selectOpenerClass;
		var wrapperClass = opts.wrapperClass;
		
		return this.each(function(index){
			var $select = $(this);

			if($select.hasClass('jqTransformHidden')) {return;}
			if($select.attr('multiple')) {return;}

			/* First thing we do is Wrap it */
			var $wrapper = $select
				.addClass('jqTransformHidden')
				.wrap('<div class="'+wrapperClass+'"></div>')
				.parent()
				.css({zIndex: 10-index})
			;
			
			/* Now add the html for the select */
			$wrapper.prepend('<div><span></span><a href="#" class="' + openerClass + '"></a></div><ul></ul>');
			var $ul = $('ul', $wrapper).css('width',$select.width()).hide();
			/* Now we add the options */
			$('option', this).each(function(i){
				var oLi = $('<li><a href="#" index="'+ i +'">'+ $(this).html() +'</a></li>');
				$ul.append(oLi);
			});
			
			/* Add click handler to the a */
			$ul.find('a').click(function(){
					$('a.selected', $wrapper).removeClass('selected');
					$(this).addClass('selected');	
					/* Fire the onchange event */
					if ($select[0].selectedIndex != $(this).attr('index') && $select[0].onchange) { $select[0].selectedIndex = $(this).attr('index'); $select[0].onchange(); }
					$select[0].selectedIndex = $(this).attr('index');
					$('span:eq(0)', $wrapper).html($(this).html());
					$ul.hide();
					return false;
			});
			
			/* Set the default */
			$('a:eq('+ this.selectedIndex +')', $ul).click();
			$('span:first', $wrapper).click(function(){$("a." + openerClass,$wrapper).trigger('click');});
			
			/* Apply the click handler to the Open */
			var oLinkOpen = $('a.' + openerClass, $wrapper)
				.click(function(){
					//Check if box is already open to still allow toggle, but close all other selects
					if( $ul.css('display') == 'none' ) {jqTransformHideSelect(opts);} 
					if($select.attr('disabled')){return false;}
					
					
					$ul.slideToggle('fast', function(){					
						//var offSet = ($('a.selected', $ul).offset().top - $ul.offset().top);
						var offSet = 0;
						$ul.animate({scrollTop: offSet});
					});
					
					return false;
				})
			;

			// Set the new width
			var iSelectWidth = /*$select.outerWidth() ||*/ parseInt($select.css('width'));
			/*
			debug('iSelectWidth ' + iSelectWidth);
			debug('$wrapper.width() ' + $wrapper.width());
			debug('$select.width() ' + $select.width());
			debug('$select.css(width) ' + );
			*/
			var oSpan = $('span:first',$wrapper);
			var newWidth = (iSelectWidth > oSpan.innerWidth())?iSelectWidth+oLinkOpen.outerWidth():$wrapper.width();
			$wrapper.css('width',newWidth);
			$ul.css('width',newWidth-2);
			/*oSpan.css({width:iSelectWidth});*/
		
			// Calculate the height if necessary, less elements that the default height
			//show the ul to calculate the block, if ul is not displayed li height value is 0
			$ul.css({display:'block',visibility:'hidden'});
			var iSelectHeight = ($('li',$ul).length)*($('li:first',$ul).height());//+1 else bug ff
			(iSelectHeight < $ul.height()) && $ul.css({height:iSelectHeight,'overflow':'hidden'});//hidden else bug with ff
			$ul.css({display:'none',visibility:'visible'});
			
		});
	};
	
	$.fn.jqTransform = function(options)
	{
		var opts = $.extend({},defaultOptions,options);
		
		return this.each(function(){
			
			var $self = $(this);
			if($self.hasClass('jqtransformdone')) 
			{
				return;
			}
			$self.addClass('jqtransformdone');
			
			$self._jqTransSelect(opts);
			jqTransformAddDocumentListener(opts);
			
			$self.bind('reset', function()
					{
						var action = function(){jqTransformReset(this);}; 
						window.setTimeout(action, 10);
					});
		}); 
				
	};/* End the Plugin */

})(jQuery);
				   