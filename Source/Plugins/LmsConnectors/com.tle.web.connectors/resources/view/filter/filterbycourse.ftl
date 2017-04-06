<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="filter">
	<h3>${b.key("manage.filter.course.title")}</h3>
	<@a.div id="filterbycourse" class="input select">
		<@render section=s.courseList />
	</@a.div>
</div>
<hr>