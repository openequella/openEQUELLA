
export function encodeQuery(params: {[key: string]: string|undefined}): string {
    let s = "";
    for (const key in params) {
        if (params.hasOwnProperty(key)) {
            const paramValue = params[key];
            if (paramValue)
            {
                if (s.length > 0)
                    s += "&";
                s += encodeURIComponent(key) + "=" + encodeURIComponent(paramValue);
            }
        }
    }
    if (s.length > 0)
        s = "?" + s;
    return s;
}