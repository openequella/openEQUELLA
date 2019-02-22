declare var bundle: any;

export interface Sizes {
  zero: string;
  one: string;
  more: string;
}
export function formatSize(size: number, strings: Sizes): string {
  var format;
  switch (size) {
    case 0:
      format = strings.zero;
      break;
    case 1:
      format = strings.one;
      break;
    default:
      format = strings.more;
      break;
  }
  return sprintf(format, size);
}

export function prepLangStrings<A>(prefix: string, strings: A): A {
  if (typeof bundle == "undefined") return strings;
  const overrideVal = (prefix: string, val: any) => {
    if (typeof val == "object") {
      var newOut = {};
      for (var key in val) {
        if (val.hasOwnProperty(key)) {
          newOut[key] = overrideVal(prefix + "." + key, val[key]);
        }
      }
      return newOut;
    } else {
      var overriden = bundle[prefix];
      if (overriden != undefined) {
        return overriden;
      }
      return val;
    }
  };
  return overrideVal(prefix, strings);
}
