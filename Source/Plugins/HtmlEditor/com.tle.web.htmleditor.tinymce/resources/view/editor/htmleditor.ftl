<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl">

<@css path="htmleditor.css" hasRtl=true />

<#-- for resource selection -->
<input type="hidden" name="searchResults" value="">

<div class="editorbox">
	<div class="link-fullscreen"><@render s.fullscreenLink /></div>

	<@textarea section=s.html style="width:${m.width}; height:${m.height};" rows=m.rows cols=1 class="htmleditor" />
</div>