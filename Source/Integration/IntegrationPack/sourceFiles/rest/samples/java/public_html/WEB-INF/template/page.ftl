<#macro page><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
		<link rel="stylesheet" type="text/css" href="styles.css" media="all">
		<link rel="stylesheet" type="text/css" href="viewresource.css" media="all">
	</head>
	
	<body>
		<div class="nav">
			<div class="navlink"><a href="${urlContext}/search">Search</a></div>
			<div class="navlink"><a href="${urlContext}/edit">Contribute</a></div>
		</div>
		
		<div class="container">
			<#nested/>
		</div>	
	</body>
</html>
</#macro>