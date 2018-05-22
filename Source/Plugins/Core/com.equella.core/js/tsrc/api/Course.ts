import { Entity } from './Entity'

export interface Course extends Entity {
    code: string;
    type?: string;
    departmentName?: string;
    citation?: string;
    students?: number;
    from?: string;
    until?: string;
    versionSelection?: string;
    archived?: boolean;
}
