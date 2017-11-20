<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<h3>${b.key("rss.dialog.title")}</h3>
<div>
	<@render section=s.rssLink class="feedlink focus" >${b.key("rss.dialog.rss")}</@render> <br>
	<@render section=s.atomLink class="feedlink" >${b.key("rss.dialog.atom")}</@render>
</div>
