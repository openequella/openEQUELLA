<#ftl strip_whitespace=true strip_text=true />

<#-- note: path is relative to a 'scripts/' or 'css/' folder -->
<#macro script path base="scripts/"><#t/>
<#local nested><#nested></#local><#t/>
<#if path??><#t/>
${head.addJs(p.url(base + path))}<#t/>
<#else><#t/>
<#assign PART_READY>
<#if PART_READY??>
	${PART_READY}
</#if>
${nested}
</#assign><#t/>
</#if><#t/>
</#macro>

<#-- See CssInclude.java for a list of supported browser and media names -->
<#macro css path base="css/" plugin="" browser="ANY" media="ALL" hasRtl=false priority="NORMAL">
<#if plugin == "">
	${head.addCss(p.url(base + path), browser, media, hasRtl, priority)}
<#else>
	${head.addCss(p.plugUrl(plugin, base + path), browser, media, hasRtl, priority)}
</#if>
</#macro>

<#macro globalscript path>
${head.addJs('scripts/' + path)}
</#macro>

<#-- See CssInclude.java for a list of supported browser and media names -->
<#macro globalcss path browser="ANY" media="ALL" hasRtl=false priority="NORMAL">
${head.addCss('css/' + path, browser, media, hasRtl, priority)}
</#macro>

<#macro namedscript name>
${head.addJs('#' + name)}
</#macro>

<#macro documentation path linkText><#t/>
	<a href="${p.url('documentation/' + path)}">${linkText}</a>
</#macro>
