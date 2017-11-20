<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/list.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<h3>${b.key('editor.freemarker.label.markup.title')}</h3>
<p>${b.key('editor.freemarker.label.markup.description')}</p>
<@ajax.div id="fm-editor">
<div class="btn-group load-action">
	<@render section=s.freeMarkerList />
</div>
<@render section=s.scriptEditor />
</@ajax.div>
<br>

<@ajax.div id="selectedCss">
	<p>${b.key('editor.freemarker.tab.freemarker.link.add.label')}</p>
	<@render section=s.externalCss class="url" />
	<@button section=s.addCssLink showAs="add" />
	<@render section=s.cssTable />
	<div hidden="true">
		<@render section=s.cssFileList />
	</div>
</@ajax.div>
