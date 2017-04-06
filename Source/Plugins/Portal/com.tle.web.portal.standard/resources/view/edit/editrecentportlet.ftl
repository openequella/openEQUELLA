<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>

<@css path="recent.css" hasRtl=true />

<@setting mandatory=true error=m.errors["collection"] label=b.key('editor.recent.label.collection') rowStyle="collections" labelFor=s.allCollections>
	<@render section=s.allCollections />
	<@checklist list=true section=s.collection class="input checkbox" />
</@setting>
<@setting  section=s.itemStatus label=b.key('editor.recent.label.status') />
<@setting  section=s.query label=b.key('editor.recent.label.query') />
<@setting mandatory=true error=m.errors["age"] label=b.key('editor.recent.label.age') labelFor=s.age>
	<@textfield  section=s.age maxlength=6 />
</@setting>
<@setting section=s.displayTypeList label=b.key('editor.rss.label.display') />