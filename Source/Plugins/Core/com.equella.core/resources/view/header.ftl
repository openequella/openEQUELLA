<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/>

<#assign TEMP_header>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" >
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<base href="${baseHref}">
	 
	<link rel="apple-touch-icon" href="${p.url("images/apple-touch-icon.png")}"> 
	<link rel="icon" type="image/png" href="${p.instUrl(p.url("images/favicon.png"))}"> 
	<link rel="icon" type="image/vnd.microsoft.icon" href="${p.instUrl(p.url("images/favicon.ico"))}"> 
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	<#list m.stylesheets as stylesheet><#t/>
		<#if stylesheet.browser.conditionStart??><#t/>
			${stylesheet.browser.conditionStart}<#lt/>
		</#if><#t/>
		<#if m.newLayout && stylesheet.hasNew>
		    <link rel="stylesheet" type="text/css" href="${stylesheet.newHref}" media="${stylesheet.media?string?lower_case}"><#lt/>
	  	<#elseif m.includeRtlStyles && stylesheet.hasRtl>
	  		<link rel="stylesheet" type="text/css" href="${stylesheet.rtlHref}" media="${stylesheet.media?string?lower_case}"><#lt/>
	  	<#else>
	  		<link rel="stylesheet" type="text/css" href="${stylesheet.href}" media="${stylesheet.media?string?lower_case}"><#lt/>
	  	</#if>
	  	<#if stylesheet.browser.conditionEnd??><#t/>
	  		${stylesheet.browser.conditionEnd}<#lt/>
	  	</#if><#t/>
	</#list><#t/>
		
	<#list m.externalScripts as script>
	  <script type="text/javascript" src="${script?html}"></script>
	</#list>
	<script type="text/javascript">${m.headerScript}</script>
	${m.head}
	<#if m.title??><title><@render m.title /></title></#if>
</#assign>

<#assign TEMP_postmarkup>
	<#list m.externalPostScripts as script>
	  <script type="text/javascript" src="${script?html}"></script>
	</#list>
	<script type="text/javascript">${m.postMarkupScript}</script>	
</#assign>