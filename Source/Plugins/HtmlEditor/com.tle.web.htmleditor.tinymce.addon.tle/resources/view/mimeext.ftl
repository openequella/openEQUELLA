<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl" />

<@css "mimeext.css" />
<h3>${b.key("mimetemplate.title")}</h3>
<p><@bundlekey "mimetemplate.entertemplate"/></p>
<@textarea section=s.template rows=8 class="mimeexttemplate" />
<@bundlekey "mimetemplate.entertemplate.help" />