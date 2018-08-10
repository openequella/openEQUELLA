export interface Route {};

export interface Routes {
    PrivilegesPage: Route,
    CoursesPage: Route,
    SchemasPage: Route,
    NewCourse: Route,
    CourseEdit: (uuid?: string) => Route,
    PrivilegeEdit: (targetNode: string) => Route,
    SchemaEdit: (uuid?: string) => Route
}
