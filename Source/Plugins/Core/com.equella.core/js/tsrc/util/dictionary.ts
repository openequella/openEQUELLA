export interface IDictionary<T> {
    [key: string]: T | undefined;
}

export function properties<T>(obj: IDictionary<T>): string[] {
    const props: string[] = [];
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            props.push(key);
        }
    }
    return props;
}
