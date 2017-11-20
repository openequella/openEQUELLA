<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css base="swagger-ui-dist/" path="swagger-ui.css" />
<@script base="swagger-ui-dist/" path='swagger-ui-bundle.js'/>
<@script base="swagger-ui-dist/" path='swagger-ui-standalone-preset.js'/>
<script>
$(function()
{
	var url = $("base").attr('href') + 'api/swagger.yaml';
	const ui = SwaggerUIBundle({
        url: url,
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      })

      window.ui = ui
});
</script>

<div class="area">
	<h2>${b.key('docs.title')}</h2>

	<p>${b.key('guide.download', 'https://equella.github.io/')}</p>
	
    <div id="swagger-ui"></div>
</div>
