<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/list.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<h3>${b.key('editor.freemarker.label.client.title')}</h3>
<p>${b.key('editor.freemarker.label.client.description')}</p>
<@ajax.div id="client-editor">
<div class="btn-group load-action">
	<@render section=s.javaScriptList />
</div>
<@render section=s.scriptEditor />
</@ajax.div>
<br>
<@ajax.div id="selectedJS">
	<p>${b.key('editor.freemarker.tab.client.link.add.label')}</p>
	<@render section=s.externalJs class="url"/>
	<@button section=s.addJavascriptLink showAs="add" />
	<@render s.javascriptTable />
	<div hidden="true">
		<@render section=s.javascriptFileList />
	</div>
</@ajax.div>
