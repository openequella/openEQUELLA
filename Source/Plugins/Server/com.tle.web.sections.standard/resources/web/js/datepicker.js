/**
 * Note: the javascript should always be setting *local midnight* into the
 * hidden values
 */

$.datepicker.setDefaults({
	showOn : 'both',
	yearRange : "c-50:c+10",
	changeYear : true,
	changeMonth : true,
	constrainInput : true,
	buttonImageOnly : true
});

function disablePicker($jelem, disabled)
{
	if ($jelem.data('pickerDisabled') === undefined)
	{
		$jelem.data('pickerDisabled', disabled);
	}
	else
	{
		if (disabled)
		{
			$jelem.datepicker('disable');
		}
		else
		{
			$jelem.datepicker('enable');
		}
	}
}

/**
 * 
 * @param $jelem
 * @param $hiddenElem
 * @param timezoneOffset
 *            Munge the hidden value by this
 * @param changeFunc
 * @param $otherCalendar
 * @param primary
 * @param setTo
 * @param imageUrl
 */
function setupPicker($jelem, $hiddenElem, timezoneOffset, changeFunc, $otherCalendar, primary, pickerType, setTo,
		imageUrl, buttonText)
{
	var tzOff = timezoneOffset;
	var settings = {
		onSelect : function(date, inst)
		{
			selectDate($jelem, tzOff, $otherCalendar, primary, changeFunc);
		},
		buttonImage : imageUrl,
		showButtonPanel : false,
		buttonText : buttonText
	};
	
	$jelem.datepicker(settings);
	$jelem.data('$hiddenElem', $hiddenElem);

	// These ugly date formats are used because if there isn't a full date
	// format (d, m & y) then datepicker('setDate', d) doesn't work
	if (pickerType == "MY")
	{
		$jelem.datepicker("option", "dateFormat", "dd __MM yy__");
		$jelem.datepicker("option", "showButtonPanel", true);

		$jelem.focusin(function()
		{
			$(".ui-datepicker-calendar").css("display", "none");
		});
		$jelem.datepicker("option", "onClose", function(date, inst)
		{
			var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
			var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
			var d = new Date(year, month, 1);
			$(this).datepicker('setDate', d);

			selectDate($jelem, tzOff, $otherCalendar, primary, changeFunc);
		});
		$jelem.keydown(function()
		{
			$(".ui-datepicker-calendar").css("display", "none");
		});
	}
	else if (pickerType == "Y")
	{
		$jelem.datepicker("option", "dateFormat", "dd mm __yy__");
		$jelem.datepicker("option", "changeMonth", false);
		$jelem.datepicker("option", "showButtonPanel", true);
		$jelem.datepicker("option", "stepMonths", 12);

		$jelem.focusin(function()
		{
			$(".ui-datepicker-calendar").css("display", "none");
			$(".ui-datepicker-month").css("display", "none");
		});
		$jelem.datepicker("option", "onClose", function(date, inst)
		{
			var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
			var d = new Date(year, 0, 1);
			$(this).datepicker('setDate', d);

			selectDate($jelem, tzOff, $otherCalendar, primary, changeFunc);
		});
		$jelem.keydown(function()
		{
			$(".ui-datepicker-calendar").css("display", "none");
			$(".ui-datepicker-month").css("display", "none");
		});
	}

	if (setTo != null)
	{
		setPickerUiValue($jelem, setTo, tzOff);
	}

	if ($jelem.data('pickerDisabled'))
	{
		$jelem.datepicker('disable');
	}
	else
	{
		$jelem.data('pickerDisabled', false);
	}
	stripExtras($jelem);
}

function selectDate($jelem, tzOff, $otherCalendar, primary, changeFunc)
{
	var thisDate = getPickerUiValue($jelem, tzOff);

	if ($otherCalendar && primary)
	{
		// ensure second date is *after* this one
		var preDate = getPickerUiValue($otherCalendar, tzOff);
		// Adjust the UI minDate date so it's midnight UTC
		$otherCalendar.datepicker('option', 'minDate', new Date(thisDate + tzOff));
		var postDate = getPickerUiValue($otherCalendar, tzOff);

		// if the date of the other calendar changed when we set the
		// minDate:
		if (preDate != postDate)
		{
			if (debugEnabled)
			{
				debug('date changed as consequence of minDate change');
			}
			// but... does this actually set the UI value of
			// $otherCalendar??
			// setPickerHiddenValue($otherCalendar, thisDateLong);
			setPickerUiValue($otherCalendar, thisDate, tzOff);
		}
		stripExtras($otherCalendar);
	}
	setPickerHiddenValue($jelem, thisDate);
	stripExtras($jelem);
	if (changeFunc)
	{
		changeFunc();
	}
}

function stripExtras($dp)
{
	text = $dp.val();
	if (text && text.indexOf("__") != -1)
	{
		var start = text.indexOf("__") + 2;
		var end = text.indexOf("__", start);
		$dp.val(text.substring(start, end));
	}
}

/**
 * 
 * @param $elem
 * @param timezoneOffset
 * @returns A UTC date which is Midnight *our* time
 */
function getPickerUiValue($elem, timezoneOffset)
{
	var dt = $elem.datepicker('getDate');
	if (dt)
	{
		var dtUtc = Date.UTC(dt.getFullYear(), dt.getMonth(), dt.getDate());
		// Convert to midnight our time
		// var toff2 = new Date(dtUtc).getTimezoneOffset() * 60000;
		return dtUtc; /* - toff2 */
		;
	}
	return null;
}

/**
 * 
 * @param $elem
 * @param utcDate
 *            A UTC date which is Midnight *our* time
 * @param timezoneOffset
 * @returns
 */
function setPickerUiValue($elem, utcDate, timezoneOffset)
{
	var tz = (timezoneOffset ? timezoneOffset : -1);
	// Convert to midnight UTC for the picker
	var d2;
	// Datepicker tries to display the date in local time. We don't want this so
	// we
	// essentially need to undo the user's *machine's* timezone.
	if (tz == -1)
	{
		var toff2 = new Date(utcDate).getTimezoneOffset() * 60000;
		d2 = new Date(0 + utcDate + toff2);
	}
	else
	{
		d2 = new Date(0 + utcDate + tz);
	}
	$elem.datepicker('setDate', d2);

	setPickerHiddenValue($elem, utcDate);
}

/**
 * @param utcDate
 *            A UTC date which is Midnight *our* time
 */
function setPickerHiddenValue($elem, utcDate)
{
	if (utcDate != null)
	{
		$elem.data('$hiddenElem').val(0 + utcDate);
	}
	else
	{
		$elem.data('$hiddenElem').val('');
	}
}