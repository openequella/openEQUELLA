function autocompletesearch(callback, elem, options) {

	var queryField = $(".real", elem);
	var promptField = $(".prompt", elem);
	var clear = false;
	var isHover;

	queryField.autocomplete({

		source : function(request, response) {
			callback(response);
		},
		search : function(event, ui) {
			if(clear)
			{
				clearPromptData(promptField);
				clear = false;
			}
		},
		open : function(event, ui) {
			var data = $(this).data('ui-autocomplete');

			if(data != null && data != 0)
			{
				// Do call to update prompter if appropriate
				var fo = data.menu.element.find('li:first-child');
				var first = fo.data("ui-autocomplete-item");
				updatePromptField(queryField, promptField, first);

				// Bold matching with regular expression
				highlightMatching(data, first.term);
			}

			return true;
		},
		close : function(event, ui) {
			clearPromptData(promptField);
		},
		select: function(event, ui) {
			var isClick = false;
			if (event.originalEvent.originalEvent.type == "click")
			{
				isClick = true;
			}
			
			if (isClick == false && (event.keyCode == 9 || (isHover == true && event.keyCode != null && event.keycode != 0)))
			{
                //just tabbed or hovered and hit enter
                event.preventDefault();
            }
			else
			{
				updateQueryField(queryField, promptField, ui.item);
				if(options != undefined && options.autoselect)
				{
					options.autoselect();
				}
            }

			return false;
		},
		focus: function(event, ui) {
			if(event.keyCode != null && event.keyCode != 0)
			{
				isHover = false;
				event.preventDefault();
				updateQueryField(queryField, promptField, ui.item);
			}
			else
			{
				isHover = true;
			}
			return true;
		},
		minLength : 2,
		autoFocus: false,
		delay: 350
	}).data("ui-autocomplete")._renderMenu = function(ul, items) {
		 var that = this;
		 $.each( items, function( index, item ) {
			 that._renderItemData( ul, item );
		 });
		 $( ul ).find( "li:odd" ).addClass( "ac_odd" );
		 $( ul ).find( "li:even" ).addClass( "ac_even" ); 
	};

	queryField.on('keydown', function(event) {
		var KEY_CODE = $.ui.keyCode;
		var elem = queryField.get(0);
		var kc = event.keyCode;

		// Auto complete from prompt when key is TAB or Right-Arrow if caret is at the end and prompt available
		if((kc == KEY_CODE.RIGHT || kc == KEY_CODE.TAB) && elem.value.length == getCaret(elem) && promptField.val() != "" )
		{
			event.preventDefault();
			queryField.val(promptField.data("real"));
			queryField.focusEnd();
			queryField.autocomplete("close");
		}
		else
		{
			var start = queryField.val().length;
			if(promptField.val().length > start)
			{
				var expectedLetter = promptField.val().charAt(start).toLowerCase();
				var nextLetter = String.fromCharCode(kc).toLowerCase();
				if(nextLetter != expectedLetter)
				{
					clearPromptData(promptField);
					clear = true;
				}
			}
		}
	});
}

function clearPromptData(promptField)
{
	promptField.val("");
	promptField.removeData("real");
}

function highlightMatching(data, term)
{
	try
	{
		var template = '<strong>%s</strong>';

		data.menu.element.find('a').each(function() {
			var me = $(this);
			var text = data.term;
			text = text.replace(/[\"\']/gi, "");
			text = text.replace(/\+/gi, "\\+");
			if(term)
			{
				text = text.split(/\s/).pop();
			}
			var html = me.html().replace(new RegExp(text, "gi"), function(matched) {
				return template.replace('%s', matched);
			});
			me.html(html);
		});
	}
	catch(err)
	{
		// do nothing
	}
}

function updateQueryField(queryField, promptField, selected)
{
	clearPromptData(promptField);
	field = queryField.get(0);
	if(selected.term)
	{
		var value = selected.value;
		var terms = field.value.split(/\s/);
		terms.pop();
		terms.push( value );
		var result = terms.join(" ");
		field.value = result
	}
	else
	{
		field.value = selected.value;
	}
}

function updatePromptField(queryField, promptField, first)
{
	var term = queryField.val();

	// Is it a term? (single word prompt)
	if(first.term)
	{
		var terms = term.split(/\s/);
		var prefix = terms.pop();
		var ending = first.value.replace(new RegExp("("+prefix+")?", "i"), "");
		var full = term + ending;

		if(calculateOffset(full.split(/\s/)) <= queryField.width() && promptField.val() != full)
		{
			promptField.val(full);
			promptField.data("real", full);
		}

		// Position menu
		$('.ui-autocomplete').css('width','auto');
		var calculated = calculateOffset(terms);
		if(calculated < queryField.width())
		{
			setPosition("left+" + calculated + " top", "left bottom", queryField, "none");
		}
		else
		{
			setPosition("right top", "right bottom", queryField, "none");
		}
	}
	else
	{
		try
		{
			var prompt = new RegExp(term + ".*", "i").exec(first.label);
			if(prompt != null)
			{
				prompt = prompt[0];
				prompt = prompt.replace(new RegExp(term, "i"), term);

				if(calculateOffset(prompt.split(/\s/)) <= queryField.innerWidth())
				{
					promptField.val(prompt);
					promptField.data("real", first.value);
				}
			}
		}
		catch(err)
		{
			// do nothing
		}
	}
}

function calculateOffset(terms)
{
	$(".autocomplete-widthcalc").remove();
	var div = $("<div class='autocomplete-widthcalc' style='display:none; font-size: 12px' >").text(terms.join(" "));
	$("body").append(div);

	var padding = 0;
	if(terms.length > 0 )
	{
		// Add 8px padding
		padding = 8;
	}

	return  $(".autocomplete-widthcalc").outerWidth(true) + padding;
}

function setPosition(my, at, of, collision)
{
	$('.ui-autocomplete').position({
		my : my,
		at : at,
		of : of,
		collision: collision
	});
}

function getCaret(elem)
{
	// FF, Chrome etc
	if (elem.selectionStart)
	{
		return elem.selectionStart;
	}
	// IE...
	else if (document.selection)
	{
		elem.focus();
		var r = document.selection.createRange();
		if (r == null)
		{
			return 0;
		}
		var re = elem.createTextRange(), rc = re.duplicate();
		re.moveToBookmark(r.getBookmark());
		rc.setEndPoint('EndToStart', re);
		return rc.text.length;
	}
	return 0;
}

$.fn.focusEnd = function() {
	this.setCursorPosition(this.val().length);
}

$.fn.setCursorPosition = function(pos) {
	this.each(function(index, elem) {
		if (elem.setSelectionRange)
		{
			elem.setSelectionRange(pos, pos);
		}
		else if (elem.createTextRange)
		{
			var range = elem.createTextRange();
			range.collapse(true);
			range.moveEnd('character', pos);
			range.moveStart('character', pos);
			range.select();
		}
	});
	return this;
};

