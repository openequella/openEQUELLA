import { IDictionary, properties } from "./dictionary";

export function encodeQuery(params: IDictionary<string>): string {
    let s = "";
    for (const key in properties(params)) {
        const paramValue = params[key];
        if (paramValue)
        {
            if (s.length > 0) {
                s += "&";
            }
            s += encodeURIComponent(key) + "=" + encodeURIComponent(paramValue);
        }
    }
    if (s.length > 0) {
        s = "?" + s;
    }
    return s;
}