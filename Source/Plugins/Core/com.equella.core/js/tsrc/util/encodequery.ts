export function encodeQuery(params: {[key: string]: string|string[]|boolean|number|undefined}): string {
    let s = "";
    function addOne(key: string, element: any)
    {
        if (s.length > 0)
            s += "&";
        s += encodeURIComponent(key) + "=" + encodeURIComponent(element.toString());                    
    }
    for (const key in params) {
        if (params.hasOwnProperty(key)) {
            var paramValue = params[key];
            if (typeof paramValue != "undefined")
            {
                if (typeof paramValue == "object")
                {
                    paramValue.forEach(element => addOne(key, element));
                }
                else 
                {
                    addOne(key, paramValue)
                }
            }
        }
    }
    if (s.length > 0) {
        s = "?" + s;
    }
    return s;
}