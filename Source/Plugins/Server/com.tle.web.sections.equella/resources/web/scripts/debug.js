var debugEnabled = true;
var debugvars = [];
var debug;
var cls;

var serialise = function(obj, full)
{
	var t;
	if (obj instanceof jQuery)
	{
		t = '$(';
		if (obj.length == 1)
		{
			var tag = obj[0].tagName;
			var attributes = '';
			/*
			 * if (obj.attr('id')) { attributes += ' id="' + obj.attr('id') +
			 * '"'; } if (obj.attr('name')) { attributes += ' name="' +
			 * obj.attr('name') + '"'; }
			 */
			var elem = obj[0];
			for ( var i = 0; i < elem.attributes.length; i++)
			{
				var attrib = elem.attributes[i];
				if (attrib.specified == true)
				{
					attributes += ' ' + attrib.name + '= "' + attrib.value + '"';
				}
			}

			t += '<' + tag + attributes + '>';
			if (tag === 'select')
			{
				for ( var opt in obj.find('option'))
				{
					t += '\n<option>' + $(opt).val() + '</option>';
				}
				t += '\n</select>';
			}

			t += obj.text();
			t += '</' + tag + '>';
		}
		else
		{
			t += 'array[' + obj.length + ']';
		}
		t += ')';
	}
	else if (obj instanceof Object)
	{
		t = 'Object:' + (obj.nodeName ? '(DOM Node "' + obj.nodeName + '")' : typeof (obj)) + '\n';
		for ( var prop in obj)
		{
			if (full || obj.hasOwnProperty(prop))
			{
				t += prop + ': ' + obj[prop] + '\n';
			}
		}
		t += '\n';
	}
	else
	{
		t = obj;
	}
	return t;
};

var printStackTrace = function()
{
	var callstack = [];
	var isCallstackPopulated = false;
	try
	{
		i.dont.exist += 0; // doesn't exist- that's the point
	} catch (e)
	{
		if (e.stack)
		{
			// Firefox
			var lines = e.stack.split('\n');
			for ( var i = 0, len = lines.length; i < len; i++)
			{
				// if (lines[i].match(/^\s*[A-Za-z0-9\-_\$]+\(/)) {
				callstack.push(lines[i]);
				// }
			}
			// Remove call to printStackTrace()
			callstack.shift();
			isCallstackPopulated = true;
		}

		else if (window.opera && e.message)
		{
			// Opera
			var lines = e.message.split('\n');
			for ( var i = 0, len = lines.length; i < len; i++)
			{
				if (lines[i].match(/^\s*[A-Za-z0-9\-_\$]+\(/))
				{
					var entry = lines[i];
					// Append next line also since it has the file info
					if (lines[i + 1])
					{
						entry += ' at ' + lines[i + 1];
						i++;
					}
					callstack.push(entry);
				}
			}
			// Remove call to printStackTrace()
			callstack.shift();
			isCallstackPopulated = true;
		}
	}

	if (!isCallstackPopulated)
	{
		// IE and Safari
		var currentFunction = arguments.callee.caller;
		while (currentFunction)
		{
			var fn = currentFunction.toString();
			var fname = fn.substring(fn.indexOf('function') + 8, fn.indexOf('')) || 'anonymous';
			callstack.push(fname);
			currentFunction = currentFunction.caller;
		}
	}

	// cls('stack');
	for ( var i = 0; i < callstack.length; i++)
	{
		debug(callstack[i], 'stack');
	}
	debug('---------------------------------------------', 'stack');
};

function pl(n, totalDigits)
{
	var ns = n.toString();
	var pd = '';
	if (totalDigits > ns.length)
	{
		for (i = 0; i < (totalDigits - ns.length); i++)
		{
			pd += '0';
		}
	}
	return pd + ns;
}


if (window.console)
{
	debug = function(text, consoleId, noesc)
	{
		window.console.log(text);
	};
	cls = function()
	{
	};
}