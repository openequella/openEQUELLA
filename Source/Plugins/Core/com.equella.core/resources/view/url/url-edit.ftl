<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include m.commonIncludePath />

<@css "url/urledithandler.css" />

<@detailArea>
	<@editArea>
		<@setting mandatory=true label=b.key('handlers.url.edit.url') section=s.url error=m.errors["url"]/>
	</@editArea>
</@detailArea >

<@detailList />

<br clear="both">
