/**
 * Encode an object as a query string
 *
 * @param params object to encode
 *
 * TODO: replace with https://github.com/ljharb/qs
 */
export function encodeQuery(params: {
  [key: string]: string | string[] | boolean | number | undefined;
}): string {
  let s = "";
  function addOne(key: string, element: string | number | boolean) {
    if (s.length > 0) s += "&";
    s += encodeURIComponent(key) + "=" + encodeURIComponent(element.toString());
  }
  for (const key in params) {
    if (params.hasOwnProperty(key)) {
      var paramValue = params[key];
      if (typeof paramValue != "undefined") {
        if (typeof paramValue == "object") {
          paramValue.forEach(element => addOne(key, element));
        } else {
          addOne(key, paramValue);
        }
      }
    }
  }
  if (s.length > 0) {
    s = "?" + s;
  }
  return s;
}
