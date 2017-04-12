<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<@css path="bulkresults.css" hasRtl=true />
<h3>${b.key("opresults.preview")}</h3>
<#if m.errored>
	<pre class="preview-error">${m.previewErrorLabel}</pre>
</#if>
<@render section=s.previewTree class="topic-tree treeview-gray" />