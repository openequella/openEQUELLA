var aaron_console = {
	cls_aaron : function(consoleId)
	{
		var $c = this._ensureConsole(consoleId ? consoleId : 'console');
		$c.find('ul').children('li').remove();
	},
	debug_aaron : function(text, consoleId, noescp)
	{
		var t2 = text || '' + text;

		var noesc = noescp ? noescp : false;
		var time = new Date();
		var timestamp = pl(time.getHours(), 2) + ':' + pl(time.getMinutes(), 2) + ':' + pl(time.getSeconds(), 2) + ':'
				+ pl(time.getMilliseconds(), 3) + ' ';
		var t = serialise(t2);
		var c = this._ensureConsole(consoleId);

		if (!noesc)
		{
			t = timestamp + ('' + t).replace(/</g, '&lt;');
			t = t.replace(/\n/g, '<br>');
		}
		var i = c.children('.inner');
		var trm = i.children('.terminal');
		trm.children('ul').append('<li>' + t + '</li>');

	},

	_ensureConsole : function(consoleId)
	{
		var sole = consoleId ? consoleId : 'console';
		var parent = window.parent;
		var $c;
		var doc;

		if (parent)
		{
			try
			{
				doc = parent.document;
				$c = $(parent.document).find('#' + sole);
			} catch (err)
			{
				doc = document;
				$c = $('#' + sole);
			}
		}
		else
		{
			doc = document;
			$c = $('#' + sole);
		}

		if ($c.length == 0)
		{
			$c = $('<div id="'
					+ sole
					+ '" class="console"><div class="inner"><div class="titlebar">'
					+ consoleId
					+ '</div>'
					+ '<div class="head"><textarea class="ev" /><button class="be">Eval</button><button class="bc" accesskey="c">Cls</button></div>'
					+ '<div class="terminal"><ul><li></li></ul></div></div></div>');

			var $i = $c.children('.inner');
			var $h = $i.children('.head');
			var $trm = $i.children('.terminal');

			var $body = $(doc).find('body');
			$c.hide();
			$c.draggable({
				handle : '.titlebar'
			});
			$c.resizable({
				resize : function(event, ui)
				{
					$i.height($c.height() - 20);
					$trm.height($i.height() - $h.height());
				}
			});

			$h.children('.be').click(function()
			{
				var $txtArea = $h.children('.ev');
				var v = $txtArea.val();
				if (typeof (v) != 'undefined')
				{
					$trm.children('ul').append('<li>' + eval(v) + '</li>');
				}
			});
			$h.children('.bc').click(function()
			{
				cls(sole);
			});
			$body.prepend($c);

			var $showButton = $('<button id="' + sole + 'show" class="' + sole + 'Show" value="' + sole + '">' + sole
					+ '</button>');
			$showButton.toggle(function()
			{
				$c.show();
			}, function()
			{
				$c.hide();
			});
			$body.prepend($showButton);
		}
		return $c;
	},
	std_log : function(text)
	{
		this.debug_aaron(text);
	},
	setup : function()
	{
		this.log = this.std_log;
		this.debug = this.std_log;
		this.error = this.std_log;
		this.info = this.std_log;
		this.warn = this.std_log;
	}
}

window.console = aaron_console;
debug = function(text, consoleId, noescp)
{
	aaron_console.debug_aaron(text, consoleId, noescp);
}
cls = function(){
	aaron_console.cls_aaron();
}
aaron_console.setup();

$(function()
{
	aaron_console._ensureConsole('console');
	aaron_console._ensureConsole('stack');
});

var old_alert = alert;
alert = function(msg)
{
	//debugger;
	old_alert(msg);
};