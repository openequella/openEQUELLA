progress(ajaxId) ::= <<
$page(
  script={
    var interval = window.setInterval("work()", 1000);
    function work() {
      \$.getJSON('/ajax/$ajaxId$', function(data) {
        var div = \$('#status');
        \$.each(data, function(i, msg) {
          if (msg.type == "finish") {
            clearInterval(interval);
            \$('#finish').attr('action', msg.redirect).find('input').val(msg.message).end().show();
          \} else {
            div.append('<div class="' + msg.type + '">' + msg.message + '</div>');
          \}
          div.scrollTop(div[0].scrollHeight);
        \});
      \});
    \}
 },
 content={
  <div id="status"></div>
  <form id="finish" action="" method="get" style="display: none">
    <input type="submit" value="" />
  </form>
})$
>>
