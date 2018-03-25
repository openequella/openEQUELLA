export interface Course {
    uuid?: string;
    code: string;
    name: string;
    description?: string;
    departmentName?: string;
    citation?: string;
    students?: number;
    from?: string;
    until?: string;
    versionSelection?: string;
    archived?: boolean;
}