function isSomeChecked(groupName)
{
	return !isNoneChecked(groupName);
}

function isNoneChecked(groupName) {
	return foreachCheckedWithName(groupName, function(opt) {
		return false;
	}, true);
}

function getCheckedValue(groupName) {
	return foreachCheckedWithName(groupName, function(opt) {
		return opt.value;
	}, null);
}

function getCheckedValues(boxes) {
	var res = [];
	foreachCheckedWithName(boxes, function(opt) {
		res[res.length] = opt.value;
	});
	return res;
}

function getAllFormValues(boxes) {
	var res = [];
	foreachWithName(boxes, function(opt) {
		res[res.length] = opt.value;
	});
	return res;
}

function setAllDisabledState(groupName, disabled) {
	foreachWithName(groupName, function(opt) {
		opt.disabled = disabled == undefined ? !opt.disabled : disabled;
	});
}

/*
 * If "checked" parameter is undefined, the option check will be inverted
 */
function setAllCheckedState(groupName, checked) {
	foreachWithName(groupName, function(opt) {
		opt.checked = checked == undefined ? !opt.checked : checked;
	});
}

function foreachCheckedWithName(groupName, fn, defaultReturn) {
	return foreachWithName(groupName, function(opt)	{
		if (opt.checked) {
			return fn(opt);
		}
	}, defaultReturn);
}

function foreachWithName(groupName, fn, defaultReturn) {
	var options = document.getElementsByName(groupName);
	if (options) {
		for (var i = 0; i < options.length; i++) {
			var rv = fn(options[i])
			if( rv != undefined ) {
				return rv;
			}
		}
	}
	return defaultReturn;
}
