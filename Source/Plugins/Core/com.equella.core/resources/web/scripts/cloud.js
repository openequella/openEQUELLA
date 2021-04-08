const Cloud = {
  onSearch: function (cb, $span, searchingText) {
    if (document.getElementById("eqpageForm")) {
      //TODO: spinner
      $span.text(searchingText);

      const updateCount = function (data) {
        if ($span[0] && $.contains(document, $span[0])) {
          $span.text(data.text);
        }
      };
      cb.call(null, updateCount);
    } else {
      // In case the form is not yet ready - such as in New UI / React
      window.requestAnimationFrame(() =>
        Cloud.onSearch(cb, $span, searchingText)
      );
    }
  },
};
