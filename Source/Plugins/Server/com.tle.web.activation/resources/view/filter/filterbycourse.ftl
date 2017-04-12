<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="course" class="filter">
	<h3>${b.key("filter.bycourse.title")}</h3>
	<#if m.courseUuid??><#assign buttonText="filter.bycourse.changebutton" />
	<#else><#assign buttonText="filter.bycourse.selbutton" /></#if>

	<p>
		<#if m.courseUuid??>
			${m.courseName}
		</#if>
	</p>
	<@button section=s.selectCourse.opener style="margin-left: 0">${b.key(buttonText)}</@button>
	<#if m.courseUuid??>
	 	<@button section=s.remove showAs="delete">${b.key("filter.bycourse.remove")}</@button>
	</#if>
</@a.div>
<hr>