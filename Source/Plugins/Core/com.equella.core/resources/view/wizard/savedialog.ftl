<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>

<@css "savedialog.css"/>

<div id="savePrompt">
	<p tabIndex="0" class="focus">${m.prompt}</p>
	<#if m.showMessage>
		<div id="moderateMessage">
			<label for="${s.message}">
				<p>${b.key('message.title')}</p>
			</label>
			<@textarea section=s.message class="focus"/>
		</div>
	</#if>
</div>