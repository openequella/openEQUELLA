var Cloud = {
  onSearch: function(cb, $span, searchingText) {
    setTimeout(function() {
      //TODO: spinner
      $span.text(searchingText);

      var updateCount = function(data) {
        if ($span[0] && $.contains(document, $span[0])) {
          $span.text(data.text);
        }
      };
      cb.call(null, updateCount);
    }, 0);
  }
};
