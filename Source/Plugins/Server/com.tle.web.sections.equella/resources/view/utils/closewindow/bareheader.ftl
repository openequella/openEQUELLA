<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/>

<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"> 
	<#list m.externalScripts as script>
	  <script type="text/javascript" src="${script}"></script>
	</#list>
	<script type="text/javascript">${m.headerScript}</script>
	${m.head}
	<#if m.title??><title><@render m.title /></title></#if>
</head>