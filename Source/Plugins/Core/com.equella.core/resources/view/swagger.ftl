<#include "/com.tle.web.freemarker@/macro/sections.ftl">


<@css base="apidocs/" path="swagger-ui.css" />

<div class="area">
	<h2>${b.key('docs.title')}</h2>

	<p>${b.key('guide.download', 'https://equella.github.io/')}</p>
	
    <div id="swagger-ui"></div>
    <script src="${p.url('apidocs/bundle.js')}"></script>
</div>
