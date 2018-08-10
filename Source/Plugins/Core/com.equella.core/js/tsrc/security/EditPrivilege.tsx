import { Button, Paper, Theme } from '@material-ui/core';
import { StyleRules, WithStyles, withStyles } from '@material-ui/core/styles';
import * as React from 'react';
import { connect, Dispatch } from 'react-redux';
import { sprintf } from 'sprintf-js';
import { AclEditorChangeEvent, TargetList, TargetListEntry } from '../api/acleditor';
import { Error, Loader } from '../components';
import MessageInfo from '../components/MessageInfo';
//import PrivilegeService from '.';
import aclService from '../service/acl';
import { EditObjectDispatchProps, EditObjectProps, EditObjectStateProps } from '../service/generic';
import { StoreState } from '../store';
import { commonString } from '../util/commonstrings';
import { properties } from '../util/dictionary';
import { prepLangStrings } from '../util/langstrings';


const styles = (theme: Theme) => {
    //TODO: get drawerWidth passed in somehow
    const footerHeight = 48;
    return {
        form: {
            display: 'flex',
            flexFlow: 'row wrap',
        },
        formControl: {
            margin: theme.spacing.unit,
            flex: '1 1 40%',
            marginBottom: 2 * theme.spacing.unit
        },
        formControl2: {
            margin: theme.spacing.unit,
            flex: '2 1 100%',
            marginBottom: 2 * theme.spacing.unit
        },
        body: {
            padding: `${theme.spacing.unit * 2}px`,
            paddingBottom: footerHeight,
            height: "100%"
        },
        footer: {
            minHeight: footerHeight
        },
        footerActions: {
            padding: '4px',
            paddingRight: '20px', 
            display: "flex", 
            justifyContent: "flex-end"
        }, 
        hiddenTab: {
            display: "none"
        }
    } as StyleRules;
};

interface EditPrivilegeStateProps extends EditObjectStateProps<TargetList> {
    availablePrivileges?: string[];
}

interface EditPrivilegeDispatchProps extends EditObjectDispatchProps<TargetList> {
    listPrivileges: (node: string) => Promise<{node: string, result: string[]}>;
}

interface EditPrivilegeProps extends EditObjectProps, EditPrivilegeStateProps, EditPrivilegeDispatchProps {
	targetNode: string;
}

type Props = EditPrivilegeProps & 
    WithStyles<'hiddenTab' | 'body' | 'formControl' | 'formControl2' | 'form' | 'footerActions' | 'footer'>;

interface EditPrivilegeState {
    activeTab?: number;
    canSave: boolean;
    changed: boolean;
    justSaved: boolean;
    editing: boolean;
    errored: boolean;
    editSecurity?: () => TargetListEntry[];
}
export const strings = prepLangStrings("privilegeedit",
    {
        title: "Editing Privilege - %s",
        newtitle: "Creating new Privilege",
        tab: "Privilege Details",
        name: {
            label: "Name",
            help: "Privilege name, e.g. Advanced EQUELLA studies"
        }, 
        description:
        {
            label: "Description",
            help: "A brief description"
        }, 
        code: {
            label: "Code",
            help: "Privilege code, e.g. EQ101"
        }, 
        type: {
            label: "Privilege Type",
            i: "Internal" ,
            e: "External",
            s: "Staff"
        }, 
        department: {
            label: "Department Name"
        }, 
        citation: {
            label: "Citation"
        }, 
        startdate: {
            label: "Start Date" 
        }, 
        enddate: {
            label: "End Date"
        }, 
        version: {
            label: "Version Selection",
            default: "Institution default",
            forcecurrent: "Force selection to be the resource version the user is viewing",
            forcelatest: "Force selection to always be the latest live resource version",
            defaultcurrent: "User can choose, but default to be the resource version the user is viewing",
            defaultlatest: "User can choose, but default to be the latest live resource version",
            help: "When accessing EQUELLA via this Privilege in an external system, all resources added to the external system will use this version selection strategy"
        },
        students: {
            label: "Unique Individuals"
        }, 
        archived: {
            label: "Archived"
        },
        saved: "Successfully saved", 
        errored: "Save failed due to server error"
    }
);


class EditPrivilege extends React.Component<Props, EditPrivilegeState> {

    constructor(props: Props){
        super(props);

        this.state = {
            activeTab: 0,
            canSave: true,
            changed: false, 
            justSaved: false,
            errored: false,
            editing: this.props.targetNode ? true : false
        };
        const { targetNode } = this.props;
        if (targetNode)
        {
            this.props.loadObject(targetNode);
        }
        /*
        else 
        {
            this.props.modifyObject({
				entries: []
            });
        }*/
        this.props.listPrivileges(targetNode);
    }

    modifyObject = (c: Partial<TargetList>) => {
        if (this.props.object)
        {
            this.props.modifyObject({...this.props.object, ...c});
            this.setState({changed:true});
        }
    }

    handleSave() {
        if (this.props.object)
        {
            const targetList = this.props.object;
            //FIXME: should always be true?
            targetList.entries = this.state.editSecurity ? this.state.editSecurity() : this.props.object.entries
            /*
            let Privilege = {
                ...this.props.object,
                security: this.state.editSecurity ? {entries: this.state.editSecurity()} : this.props.object.entries
            };*/
            
            const { saveObject } = this.props;
            const thiss = this;
            this.props.validateObject(targetList).then(function(valErrors){
                if (properties(valErrors).length === 0){
                    saveObject(targetList)
                    .then(_ => thiss.setState({changed:false, justSaved: true}))
                    .catch(r => thiss.setState({errored: true}))
                }
            });
        }
    }
    /*
    handleChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.modifyObject({ [stateFieldName]: event.target.value });            
        };
    }

    handleIntChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            let val = event.target.value;
            let intVal: number | undefined = parseInt(val);
            if (!Number.isInteger(intVal)){
                intVal = undefined;
            }
            this.modifyObject({ [stateFieldName]: intVal });   
        };
    }

    handleCheckboxChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.modifyObject({ [stateFieldName]: event.target.checked });
        };
    }

    handleDateChange(stateFieldName: string): (date?: Date) => void {
        return (date?: Date) => {
            this.modifyObject({ [stateFieldName]: date ? formatISO(date) : null});
        };
    }

    handleTabChange(): (event: any, value: number) => void {
        return (event: any, value: number) => {
            this.setState({ activeTab: value });
        };
    }

    handleChangeTabIndex(): (index: number) => void {
        return (index: number) => {
            this.setState({ activeTab: index });
        };
    }*/

    handleAclChange(): (e: AclEditorChangeEvent) => void {
        return (e: AclEditorChangeEvent) => {
            this.setState({ canSave: e.canSave, changed: true, editSecurity: e.getAcls });
        }
    }

    render() {
        const { loading, object: targetList, availablePrivileges, classes } = this.props;
        const { AclEditor, Template, router, routes } = this.props.bridge;
        const { editing } = this.state;
        const title = sprintf(editing ? strings.title : strings.newtitle, '');

        if (loading || !availablePrivileges){
            return <Template title={title} backRoute={routes.PrivilegesPage}>
                    <Loader />
                </Template>
        }

        if (!targetList){
            return <Template title={title} backRoute={routes.PrivilegesPage}>
                    <Error>Error loading target list</Error>
                </Template>
        }

        const { changed, canSave, justSaved, errored } = this.state;
        
        let entries: TargetListEntry[] = [];
        if (targetList){
            entries = targetList!.entries;
        } 

        const saveOrCancel = 
            <Paper className={classes.footerActions}>
                <Button onClick={router(routes.PrivilegesPage).onClick} color="secondary">{commonString.action.cancel}</Button>
                <Button onClick={this.handleSave.bind(this)} color="primary"
                    disabled={!canSave || !changed}>{commonString.action.save}</Button>
            </Paper>               

        return <Template title={title} preventNavigation={changed} 
                fixedViewPort={true} 
                backRoute={routes.PrivilegesPage} 
				footer={saveOrCancel} 
				/*
                tabs={
                    <Tabs value={activeTab} onChange={this.handleTabChange()} fullWidth>
                        <Tab label={strings.tab} />
                        <Tab label={entityStrings.edit.tab.permissions} />
                    </Tabs>
                }*/ >
            <MessageInfo title={strings.saved} open={justSaved} 
                onClose={() => this.setState({justSaved:false})} variant="success"/>
            <MessageInfo title={strings.errored} open={errored} 
                onClose={() => this.setState({errored:false})} variant="error"/>
            <div className={classes.body}>
                
                <div className={this.state.activeTab === 0 ? "" : classes.hiddenTab } style={{ height: "100%" }}>
                    { /* TODO: priv list from API */ }
                    <AclEditor 
                        onChange={ this.handleAclChange() }
                        acls={entries} 
                        allowedPrivs={availablePrivileges}/>
                </div>
                <div className={classes.footer}/>
            </div>

        </Template>
    }
}

function mapStateToProps(state: StoreState, ownProps: EditPrivilegeProps): EditPrivilegeStateProps {
    const { acl } = state;
    return {
        loading: acl.loading,
        object: acl.editingObject,
        availablePrivileges: (ownProps.targetNode ? acl.nodes[ownProps.targetNode] : [])
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>): EditPrivilegeDispatchProps {
    const { workers, actions } = aclService;
    
    return {
        loadObject: (id: string) => workers.read(dispatch, {id}),
        saveObject: (object: TargetList) => workers.update(dispatch, { object }),
        modifyObject: (object: TargetList) => dispatch(actions.modify({ object })),
        listPrivileges: (node: string) => aclService.workers.listPrivileges(dispatch, {node}),
        validateObject: (object: TargetList) => workers.validate(dispatch, { object })
    };
}

export default withStyles(styles)(connect(mapStateToProps, mapDispatchToProps)(EditPrivilege));