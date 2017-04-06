<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<h3>${b.key('editor.freemarker.label.script.title')}</h3>
<p>${b.key('editor.freemarker.label.script.description')}</p>
<@ajax.div id="server-editor">
<div class="btn-group load-action">
	<@render section=s.javaScriptList />
</div>
<@render section=s.scriptEditor />
</@ajax.div>
