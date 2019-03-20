export interface Route {}

export interface Routes {
  CoursesPage: Route;
  SchemasPage: Route;
  NewCloudProvider: Route;
  CloudProviderListPage: Route;
  NewCourse: Route;
  CourseEdit: (uuid?: string) => Route;
  SchemaEdit: (uuid?: string) => Route;
}
