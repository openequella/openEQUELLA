<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl">

<@css path="htmleditor.css" hasRtl=true />
<@css path="htmleditorctl.css" />

<#-- for resource selection -->
<input type="hidden" name="searchResults" value="">

<div class="editorbox">
	<div class="link-fullscreen"><@render s.fullscreenLink /></div>
	
	<@textarea section=s.html rows=m.rows cols=1 class="htmleditor" />
</div>
