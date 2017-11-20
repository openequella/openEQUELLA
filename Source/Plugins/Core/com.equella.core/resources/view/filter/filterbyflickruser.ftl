<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="flickr.css" hasRtl=true />

<@div class="filter" id="flickr-user-filter">
	<div class="input text">
		<label for="searchform-flickrid"><h3>${b.key("add.search.usertext")}</h3></label>
	</div>
	<div>
		<@textfield id="searchform-flickrid" class="flickrid" section=s.flickrIdField />
	</div>
</@div>
<hr />