// the regex logic needs to be in a validator ...
function isWildIPRegex($theText)
{
//	return false;
	var txt = $theText.val();
	var numericOrWildcardIP = /^((25[0-5]|2[0-4]\d|[01]?\d\d?|\*)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?|\*)$/; // new Regex("")
	return numericOrWildcardIP.test(txt);
}
