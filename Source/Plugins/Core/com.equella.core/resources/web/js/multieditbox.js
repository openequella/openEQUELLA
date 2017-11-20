function toggleLocale(box, localeIndex) {
	if (box.selectedIndex == localeIndex) {
		localeIndex = -1;
	}
	for ( var i = 0; i < box.rows.length; i++) {
		var row = box.rows[i];
		if (i != localeIndex && localeIndex != -1) {
			row.hide();
		} else {
			row.show();
		}
	}
	box.selectedIndex = localeIndex;
	if (localeIndex == -1) {
		box.jquery.addClass("editBoxGroupMulti");
	} else {
		box.jquery.removeClass("editBoxGroupMulti");
	}
}

function setupBox(boxelem, rows) {
	var box = {
		jquery : $(boxelem),
		selectedIndex : -1,
		rows : []
	};
	for ( var i = 0; i < rows.length; i++) {
		var row = $(rows[i]);
		box.rows[i] = row;
		$("input:button", row).bind("click", i, function(event) {
			toggleLocale(box, event.data);
		});
	}
	toggleLocale(box, 0);
}