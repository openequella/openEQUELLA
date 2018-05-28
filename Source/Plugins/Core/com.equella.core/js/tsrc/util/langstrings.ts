

declare var bundle : any;

export interface Sizes {
    zero: string;
    one: string;
    more: string;
}
export function sizedString(size: number, strings: Sizes): string {
    switch (size)
    {
        case 0: return strings.zero;
        case 1: return strings.one;
    }
    return strings.more;
}

export function prepLangStrings<A>(prefix:string, strings: A) : A {
    if (typeof bundle == "undefined")
      return strings;
    const overrideVal = (prefix: string, val: any) => {
        if (typeof val == "object")
        {
            var newOut = {};
            for (var key in val) {
                if (val.hasOwnProperty(key)) {
                    newOut[key] = overrideVal(prefix+"."+key, val[key])
                }
            }
            return newOut;
        }
        else {
            var overriden = bundle[prefix];
            if (overriden != undefined)
            {
                return overriden;
            }
            return val;
        }
    }
    return overrideVal(prefix, strings);
    
}
