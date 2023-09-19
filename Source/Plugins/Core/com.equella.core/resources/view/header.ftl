<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/>

<#assign faviconSizes = ["32x32", "48x48", "64x64", "96x96", "128x128", "196x196", "320x320", "400x400", "640x640"]>
<#assign faviconAppleSizes = ["120x120", "152x152", "167x167", "180x180"]>
<#assign TEMP_header>
  <meta http-equiv="X-UA-Compatible" content="IE=edge" >
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <base href="${baseHref}">

  <!-- Favicon -->
  <link rel="icon" type="image/vnd.microsoft.icon" href="${p.instUrl(p.url("images/favicon.ico"))}">
  <#list faviconAppleSizes as size>
    <link rel="apple-touch-icon" sizes="${size}" href="${p.instUrl(p.url('images/favicon.' + size + 'px.png'))}">
  </#list>
  <#list faviconSizes as size>
    <link rel="icon" type="image/png" sizes="${size}" href="${p.instUrl(p.url('images/favicon.' + size + 'px.png'))}">
  </#list>
  <!-- SVG version for high-res displays -->
  <link rel="icon" type="image/svg+xml" href=${p.instUrl(p.url('images/favicon.svg'))}>

  <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <#list m.stylesheets as stylesheet><#t/>
    <#if stylesheet.browser.conditionStart??><#t/>
      ${stylesheet.browser.conditionStart}<#lt/>
    </#if><#t/>
      <#if m.includeRtlStyles && stylesheet.hasRtl>
        <link rel="stylesheet" type="text/css" href="${stylesheet.getRtlHref(_info)}" media="${stylesheet.media?string?lower_case}"><#lt/>
      <#else>
        <link rel="stylesheet" type="text/css" href="${stylesheet.getHref(_info)}" media="${stylesheet.media?string?lower_case}"><#lt/>
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
