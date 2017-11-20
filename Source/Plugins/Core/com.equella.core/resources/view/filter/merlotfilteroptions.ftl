<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/list.ftl" />

<div class="filter">
	<div class="input checkbox">
		<@render s.free />
	</div>
</div>

<div class="filter">
	<div class="input checkbox">
		<@render s.creativeCommons />
	</div>
</div>
<hr>
<div class="filter">
	<h3>${b.key("filter.language.title")}</h3>
	<div class="input select">
		<@render s.languages />
	</div>
</div>
<hr>
<div class="filter">
	<h3>${b.key("filter.technicalformat.title")}</h3>
	<div class="input select">
		<@render s.technicalFormats />
	</div>
</div>
<hr>
<div class="filter">
	<h3>${b.key("filter.audience.title")}</h3>
	<div class="input select">
		<@render s.materialAudiences />
	</div>
</div>
<hr>
<div class="filter">
	<h3>${b.key("filter.mobile.os.title")}</h3>
	<@boollist section=s.mobileOS; opt, state>
		<div class="input checkbox">
			<@render state />
		</div>
	</@boollist>
</div>
<hr>
<div class="filter">
	<h3>${b.key("filter.mobile.type.title")}</h3>
	<@boollist section=s.mobileType; opt, state>
		<div class="input checkbox">
			<@render state />
		</div>
	</@boollist>
</div>
<hr>
