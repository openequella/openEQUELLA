<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>${m.title}</title>
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  		<#if m.useBaseHref>
  		<base href="${baseHref}">
  		</#if>
  		<#list m.styles as style>
  		<link type="text/css" rel="stylesheet" href="${style}" />
  		</#list>
  	</head>
	<body class="bodyclass htmlcontent"><div>${m.html}</div></body>
</html>
