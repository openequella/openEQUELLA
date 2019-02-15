
export interface PagingResults<T> {
    start: number;
	length: number;
	available: number;
	resumptionToken?: string;
	results: T[];
}

export interface ClickableLink {
	href: string;
	onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
}