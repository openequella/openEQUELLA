<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<script type="text/javascript">
  function switchText(id) {
    $("#bluebar_help > div").hide();
    $("#" + id).show();
  }
</script>

${b.key('themesettingshelp', [''])}
${b.key('commonmoredivs', [''])}