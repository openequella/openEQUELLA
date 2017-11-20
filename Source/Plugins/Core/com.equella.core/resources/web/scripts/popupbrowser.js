function setupGui() {
		if ($.browser.msie)
			$(".popupbrowser-searchQuery").keydown(checkForEnter);
		else
			$(".popupbrowser-searchQuery").keypress(checkForEnter);
}


function checkForEnter(event) {
	if( event.keyCode == 13 ) {
		$('.popupbrowser-searchButton').click();
		event.preventDefault();
		return false;
	}
}

$(function() {
	setupGui();
});