
export interface TargetListEntry {
    granted:Boolean;
    override:Boolean;
    privilege:String;
    who:String;
}

export interface AclEditorProps {
    acls : Array<TargetListEntry>,
    onChange : (e: {
        canSave: Boolean, 
        getAcls: () => Array<TargetListEntry>
    }) => void;
    allowedPrivs : Array<String>
}