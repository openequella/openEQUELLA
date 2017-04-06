<#assign TEMP_body>
	<object type="application/x-shockwave-flash" data="${p.url('player_flv_maxi.swf')}" width="${m.width}" height="${m.height}">
	    <param name="movie" value="${p.url('player_flv_maxi.swf')}" />
	    <param name="FlashVars" value="flv=${m.flvUrl?url('utf-8')}&config=${p.url('config.xml')?url('utf-8')}" />
	</object>
</#assign>
