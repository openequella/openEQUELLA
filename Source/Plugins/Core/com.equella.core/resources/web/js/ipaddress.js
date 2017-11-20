function setIp(pos, hidden, fieldId)
{
	var inputVal = $("#" + fieldId)[0].value;

	var $hidden = $(hidden);
	if ($hidden.data("ip") == undefined)
	{
		$hidden.data("ip", [ 0, 0, 0, 0, 32 ]);
	}
	var ip = $hidden.data("ip");

	if (inputVal.length > 0)
	{
		if (inputVal < 0)
			inputVal = 0;
		else if (pos !== 4 && inputVal > 255)
			inputVal = 255;
		else if (pos === 4 && inputVal > 32)
			inputVal = 32;
		ip[pos] = inputVal;
		$hidden.data("ip", ip);
		hidden.value = buildIp(ip);
	}
}

function buildIp(ip)
{
	var newVal = "";
	for (var i = 0; i < (ip.length - 1); i++)
	{
		newVal += ip[i] + ".";
	}
	return newVal.substring(0, (newVal.length - 1)) + "/" + ip[4];
}

function disableFields(elem, disable)
{
	var $fields = $(elem).siblings("input.ip-field");
	
	for (var i = 0; i < $fields.length; i++)
	{
		$fields[i].setAttribute("disabled", "disabled")
	}
}