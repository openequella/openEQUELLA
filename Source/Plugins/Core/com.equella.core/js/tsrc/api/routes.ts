import * as React from 'react'

export interface Route {
    href: string;
    onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
}

export interface Routes {
    CourseEdit: { 
        create: (uuid?: string) => Route 
    },
    SchemaEdit: { 
        create: (uuid?: string) => Route 
    }
}

export var Routes : () => Routes = () => require('Routes');
