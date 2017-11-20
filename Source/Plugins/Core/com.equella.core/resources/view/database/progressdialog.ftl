<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<p class="progress-curr-migration">&nbsp;</p>
<p class="progress-curr-step">&nbsp;</p>

<div class="progress-links">
	<a class="link-messages selected" href="javascript:switchOutput('messages')">${b.key('databases.progress.messages')}</a>
	<span class="warnings-link-wrapper">
		|
		<a class="link-warnings" href="javascript:switchOutput('warnings')">${b.key('databases.progress.warnings')}</a>
	</span>
</div>
<div class="progress-messages textpane"></div>
<div class="progress-warnings textpane"></div>
