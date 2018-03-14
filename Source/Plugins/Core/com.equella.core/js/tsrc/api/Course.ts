import { Entity } from './Entity'
import { SearchResults } from './General'

export interface Course extends Entity {
    code: string;
}

export interface CourseList extends SearchResults<Course> {
    
}