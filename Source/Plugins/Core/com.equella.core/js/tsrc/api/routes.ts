
export interface Route {}

export interface Routes {
    CourseEdit: { 
        create: (uuid?: string) => Route 
    }
}

export var Routes : Routes = require("../../../output/Routes");
