
export interface SearchResults<T> {
    start: number;
	length: number;
	available: number;
	results: T[];
}