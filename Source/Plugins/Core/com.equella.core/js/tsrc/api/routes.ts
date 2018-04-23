export interface Route {};

export interface Routes {
    CoursesPage: {
        value: Route
    },
    CourseEdit: { 
        create: (uuid?: string) => Route 
    },
    SchemaEdit: { 
        create: (uuid?: string) => Route 
    }
}
