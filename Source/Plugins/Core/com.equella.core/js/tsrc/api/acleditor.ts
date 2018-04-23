
export interface TargetListEntry {
    granted:Boolean;
    override:Boolean;
    privilege:String;
    who:String;
}

export interface AclEditorProps {
    acls : Array<TargetListEntry>,
    allowedPrivs : Array<String>
}