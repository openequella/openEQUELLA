import { IDictionary } from '../util/dictionary';
import { TargetListEntry } from './acleditor';
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
    validationErrors?: IDictionary<string>;
    readonly?: {
        granted: string[]
    }
}

export interface EntitySecurity {
    entries: TargetListEntry[];
}

export interface EntityExport {

}