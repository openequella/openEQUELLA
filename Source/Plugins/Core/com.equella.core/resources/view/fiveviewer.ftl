<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "video-js.css" />
<@css "htmlfiveplayer.css "/>

<#assign TEMP_body>
<script>
	videojs.options.flash.swf ="${p.url('scripts/video-js.swf')}";	 
</script>

<div id="player-container">
	<video id="html5player" class="video-js vjs-default-skin" controls
		width=${m.width} height=${m.height} preload="auto" 
		data-setup='{"techOrder": ["html5", "flash"]}'>
  		<source src=${m.videoUrl} type=${m.videoType}>
	</video>
	<!--[if gt IE 7]><p class="fallback ie">${m.fallbackIE}</p><![endif]-->
</div>

</#assign>