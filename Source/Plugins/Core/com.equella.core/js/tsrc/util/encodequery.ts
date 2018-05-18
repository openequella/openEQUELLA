export function encodeQuery(params: {[key: string]: string|string[]|undefined}): string {
    let s = "";
    for (const key in params) {
        if (params.hasOwnProperty(key)) {
            var paramValue = params[key];
            if (paramValue)
            {
                if (typeof paramValue == "string")
                {
                    paramValue = [paramValue]
                }
                paramValue.forEach(element => {
                    if (s.length > 0)
                        s += "&";
                    s += encodeURIComponent(key) + "=" + encodeURIComponent(element);                    
                });
            }
        }
    }
    if (s.length > 0) {
        s = "?" + s;
    }
    return s;
}