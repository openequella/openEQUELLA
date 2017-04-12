function showSlider(sliderId, selvalue)
{	
	var labelArr = new Array("Off", "x0.25", "x0.5", "No boost", "x1.5", "x2","x4","x8");
	
	$("#"+sliderId).slider({
		value:selvalue,
		min: 0,
		max: 7,
		step: 1,
		slide: function( event, ui ) 
		{
			switch (sliderId) 
			{
		    case "title-slider":
		    	$( "#_titleBoost" ).val( ui.value );
		        break;
		    case "description-slider":
		        $("#_descriptionBoost").val(ui.value);
		        break;
		    case "attachment-slider":
		    	$("#_attachmentBoost").val(ui.value);
		    	break;
		}}
	})
	.each(function() {
		  // Get the options for this slider (specified above)
		  var opt = $(this).data().uiSlider.options;

		  // Get the number of possible values
		  var vals = opt.max - opt.min;

		  // Position the labels
		  for (var i = 0; i <= vals; i++) 
		  {
		    // Create a new element and position it with percentages
		    var el = $('<label>'+(labelArr[i])+'</label>').css('left',(i/vals*100)+'%');
		    
		    // Add the element inside #slider
		    $("#"+sliderId).append(el);
		  }
		});
}

	