function setupMultiEdit(elem, defaultSelection, localeSetter) {
	var uniinput = $(".universalinput", elem); // Universal translation field
	var unitrans = $(".universaltranslation", elem);
	var alltrans = $(".alltranslations", elem); // All translations
	var select = $(".localeselector", elem); // Locale selector

	var setter = localeSetter;
	setter(defaultSelection);

	function updateText() {
		uniinput.val(getReal().val());
	}

	function getReal() {
		return $("." + select.val(), elem);
	}

	updateText();

	uniinput.keyup(function(event) {
		var text = uniinput.val();
		getReal().val(text);
	});

	select.change(function() {
		var select = $(this);
		var selvalue = select.val()

		if (selvalue == "all") {
			unitrans.slideUp(500, function() {
				alltrans.slideDown();
			});
		} else {
			updateText();
		}
	});

	$(".collapse", elem).click(function(event) {
		alltrans.slideUp(500, function() {
			unitrans.slideDown();
			setter(defaultSelection);
			updateText();
		});
		event.preventDefault();
	});
}

function disableMultiEdit(elem, disable) {
	// Do not disable the locale changer drop-down. Even when disabled, they
	// should still be able to read it.
	var comps = $(
			'.universalinput, .alltranslations input, .singletranslation input',
			elem);
	if (disable)
		comps.attr('disabled', 'disabled');
	else
		comps.removeAttr('disabled');
}
