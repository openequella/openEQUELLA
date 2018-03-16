exports.setInnerHtml = function (html) {
  return function(ref)
  {
    return function()
    {
      if (ref)
      {
        $(ref).html(html);
      }
    }
  }
}