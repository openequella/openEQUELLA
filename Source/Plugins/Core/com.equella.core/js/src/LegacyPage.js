exports.setBodyHtml = function(ref)
{
  return function()
  {
    if (ref)
    {
      $(ref).html(renderData.html.body);
    }
  }
}
