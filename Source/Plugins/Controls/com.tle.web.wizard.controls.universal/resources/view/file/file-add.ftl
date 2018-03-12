<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>

<@css path="file/file.css" hasRtl=true />

<div class="fileadd">
	<h3><@bundlekey "handlers.file.title"/></h3>
	
	<p><@bundlekey "handlers.file.prompt"/></p>
	<@render m.reactTag/>
</div>