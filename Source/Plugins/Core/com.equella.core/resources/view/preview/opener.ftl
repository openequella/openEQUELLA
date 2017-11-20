<html>
    <head>
    <script type="text/javascript">
        var w = Math.floor(screen.availWidth * ${m.width});
        var h = Math.floor(screen.availHeight * ${m.height});
        var x = Math.floor(screen.availWidth/2 - w/2);
        var y = Math.floor(screen.availHeight/2 - h/2);
        window.resizeTo(Math.floor(w), Math.floor(h));
        window.moveTo(Math.floor(screen.availWidth/2-w/2), Math.floor(screen.availHeight/2-h/2));

        newwin = window.open(
        	"${m.url?js_string}",
        	'_blank',
        	'resizable=1,scrollbars=1,toolbar=${m.toolbar},menubar=${m.menubar},width='+w+',height='+h+',left='+x+',top='+y
        );

        firstwindow = window.self;
        firstwindow.opener = window.self;
        firstwindow.close();
        newwin.focus();
    </script>
	</head>
</html>