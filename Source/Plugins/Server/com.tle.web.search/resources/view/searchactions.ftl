<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@renderList s.topSections/>
<#if s.topSections?size &gt; 0 && m.selectSections?size &gt; 0 ><hr></#if>
<@renderList m.selectSections/>