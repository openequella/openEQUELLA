<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />

<@css path="flickr.css" hasRtl=true />

<@detailArea>
	<div>
		<#if m.specificDetail["embed"]?? >
			<div id="flickr-preview-container">${m.specificDetail["embed"].second}</div>
		</#if>
	</div>
	<@editArea />
</@detailArea>

<@detailList />

<br clear="both">
