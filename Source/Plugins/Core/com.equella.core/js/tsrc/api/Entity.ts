import { User } from './User';

export interface Entity {
    uuid?: string;
    name: string;
    description?: string;

    modifiedDate?: string;
    createdDate?: string;
    
	owner?: User;

	security?: EntitySecurity;
	exportDetails?: EntityExport;
}

export interface EntitySecurity {

}

export interface EntityExport {

}