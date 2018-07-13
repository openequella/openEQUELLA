<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="courseFilter" class="filter">
	<h3>${b.key("filter.bycourse.title")}</h3>
    <div class="input select">
	    <@render s.selectCourse />
	</div>
</@a.div>
<hr>