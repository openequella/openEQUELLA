import { Entity } from './Entity'
import { SearchResults } from './General'

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

export interface CourseList extends SearchResults<Course> {
    
}