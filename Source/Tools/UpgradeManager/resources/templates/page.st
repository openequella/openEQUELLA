page(script, content, managerVersion) ::= <<
<html>
	<head>
		<title>openEQUELLA Manager</title>

		<link rel="stylesheet" type="text/css" href="/application.css" />
		<link rel="stylesheet" type="text/css" href="/tabs/ui.tabs.css" />
		<!--[if lte IE 7]>
		<link rel="stylesheet" type="text/css" href="/application-ie.css" />
		<![endif]-->

		<script type="text/javascript" src="/jquery.js"></script>
		<script type="text/javascript" src="/ui.core.js"></script>
		<script type="text/javascript" src="/tabs/ui.tabs.js"></script>

		<script type="text/javascript">
		$script$
		</script>
	</head>
	<body>
		<div id="title">
			<h1>openEQUELLA Manager</h1>
		</div>
		<div id="content">
			$content$
        </div>
        <div id="footer">
        	openEQUELLA Manager $managerVersion$
        </div>
	</body>
</html>
>>
