export interface Route {};

export interface Routes {
    CoursesPage: {
        value: Route
    },
    CourseEdit: (uuid?: string) => Route,
    SchemaEdit: (uuid?: string) => Route
}
