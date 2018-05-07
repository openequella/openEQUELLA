import * as React from 'react';
import { Button, TextField, Grid, Select, InputLabel, Input, 
    MenuItem, Switch, FormGroup, FormControl, FormControlLabel, FormHelperText,
    /*IconButton, Icon,*/ Tabs, Tab, /*Typography,*/ Theme, Paper } from 'material-ui';
import withStyles, { WithStyles, StyleRulesCallback } from 'material-ui/styles/withStyles';
//import SwipeableViews from 'react-swipeable-views';
import { DatePicker } from 'material-ui-pickers';
import { connect, Dispatch } from 'react-redux';
import { format, parse } from 'date-fns';

import { Course, Entity } from '../api';
import courseService from './index';
import { StoreState } from '../store';
import { Bridge } from '../api/bridge';
import { TargetListEntry, AclEditorChangeEvent } from '../api/acleditor';

const styles: StyleRulesCallback<'form' | 'formControl'> = (theme: Theme) => {
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
            height: `calc(100% - ${footerHeight}px)`
        },
        footer: {
            position: 'absolute',
            bottom: 0,
            left: 0,
            width: "100%",
            height: `${footerHeight}px`
        },
        footerActions: {
            float: 'right',
            padding: '4px',
            paddingRight: '20px'
        }, 
        hiddenTab: {
            display: "none"
        }
    };
};

interface EditEntityProps<E extends Entity>
{
    bridge: Bridge;
    loadEntity: (uuid: string) => void;
    saveEntity: (entity: E) => void;
    modifyEntity: (entity: E) => void;
    uuid?: string;
    entity: E;
}

interface EditCourseProps extends EditEntityProps<Course> {
    
}

type Props = EditCourseProps & 
    WithStyles<'form' |
        'formControl' |
        'formControl2' |
        'body' |
        'footer' |
        'footerActions' |
        'hiddenTab' |
        'tabBar'>;

interface EditCourseState {
    activeTab?: number;
    canSave: boolean;
}

class EditCourse extends React.Component<Props, EditCourseState> {

    constructor(props: Props){
        super(props);

        this.state = {
            activeTab: 0,
            canSave: true
        };
        if (this.props.uuid)
        {
            this.props.loadEntity(this.props.uuid);
        }
        else 
        {
            this.props.modifyEntity({
                code: '',
                name: ''
            });
        }
    }

    handleSave() {
        const { from, until, versionSelection } = this.props.entity;
        const fromStr = (from ? format(from, 'YYYY-MM-DDTHH:mm:ss.SSSZ') : undefined);
        const untilStr = (until ? format(until, 'YYYY-MM-DDTHH:mm:ss.SSSZ') : undefined);
        const vs = (versionSelection === "DEFAULT" ? undefined : versionSelection);
        
        let course = {
            ...this.props.entity,
            from: fromStr,
            until: untilStr,
            versionSelection: vs
        };
        this.props.saveEntity(course);
    }

    handleChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.props.modifyEntity({ ...this.props.entity, [stateFieldName]: event.target.value });
        };
    }

    handleCheckboxChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.props.modifyEntity({ ...this.props.entity, [stateFieldName]: event.target.checked });
        };
    }

    handleDateChange(stateFieldName: string): (date: string) => void {
        return (date: string) => {
            this.props.modifyEntity({ ...this.props.entity, [stateFieldName]: date });
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
    }

    handleAclChange(): (e: AclEditorChangeEvent) => void {
        return (e: AclEditorChangeEvent) => {
            this.setState({ canSave: e.canSave });
            this.props.modifyEntity({ ...this.props.entity, security: { rules: e.getAcls() } })
        }
    }

    render() {
        if (!this.props.entity){
            return <div></div>
        }

        const { code, name, description, type, departmentName, citation, students, from, 
            until, versionSelection, archived, security } = this.props.entity;
        const { activeTab } = this.state;
        const { classes } = this.props;
        const vs = (versionSelection ? versionSelection : "DEFAULT");
        const fromDate = (from ? parse(from!, 'YYYY-MM-DDTHH:mm:ss.SSSZ', new Date()) : null);
        const untilDate = (until ? parse(until!, 'YYYY-MM-DDTHH:mm:ss.SSSZ', new Date()) : null);
        const { AclEditor, Template, router, routes } = this.props.bridge;

        let rules: TargetListEntry[] = [];
        if (security){
            rules = security!.rules;
        } 
        /*
        <IconButton aria-label="Back" 
                        onClick={router(routes.CoursesPage).onClick}>
                            <Icon>arrow_back</Icon>
                    </IconButton>*/


        return <Template title="Edit Course" tabs={
                <Tabs value={activeTab} onChange={this.handleTabChange()} fullWidth>
                    <Tab label="Course Details" />
                    <Tab label="Permissions" />
                </Tabs>}>
                    
            <div className={classes.body}>
                {/* <SwipeableViews
                    axis="x"
                    index={activeTab}
                    onChangeIndex={this.handleChangeTabIndex()}> */}
                    <div className={this.state.activeTab === 0 ? "" : classes.hiddenTab} style={{ padding: 24 }}>
                        <Grid>
                            <div className={classes.form}>

                                <TextField id="name" 
                                    label="Name" 
                                    helperText="Course name, e.g. Advanced EQUELLA studies"
                                    value={name}
                                    onChange={this.handleChange('name')}
                                    margin="normal"
                                    className={classes.formControl2}
                                    required
                                    />

                                <TextField id="description" 
                                    label="Description" 
                                    helperText="A brief description"
                                    value={description}
                                    onChange={this.handleChange('description')}
                                    multiline
                                    rows={3}
                                    margin="normal"
                                    className={classes.formControl2}
                                    />

                                <TextField id="code" 
                                    label="Code" 
                                    helperText="Course code, e.g. EQ101"
                                    value={code}
                                    onChange={this.handleChange('code')}
                                    margin="normal"
                                    className={classes.formControl}
                                    required
                                        />

                                <FormControl margin="normal" className={classes.formControl}>
                                    <InputLabel htmlFor="type">Course Type</InputLabel>
                                    <Select id="type" 
                                        value={type}
                                        input={<Input id="type-inp" />}
                                        onChange={this.handleChange('type')}
                                    >
                                        <MenuItem key={"i"} value={"i"}>Internal</MenuItem>
                                        <MenuItem key={"e"} value={"e"}>External</MenuItem>
                                        <MenuItem key={"s"} value={"s"}>Staff</MenuItem>
                                    </Select>
                                </FormControl>

                                <TextField id="departmentName" 
                                    label="Department Name" 
                                    //helperText=""
                                    value={departmentName}
                                    onChange={this.handleChange('departmentName')}
                                    margin="normal"
                                    className={classes.formControl}
                                    />

                                <FormControl margin="normal" className={classes.formControl}>
                                    <InputLabel htmlFor="citation">Citation</InputLabel>
                                    <Select id="citation" 
                                        value={citation || ''}
                                        input={<Input id="citation-inp" />}
                                        onChange={this.handleChange('citation')}
                                    >
                                        <MenuItem key={"harvard"} value={"harvard"}>
                                            harvard
                                        </MenuItem>
                                    </Select>
                                </FormControl>

                                <DatePicker id="from"
                                    label="Start Date"
                                    value={fromDate}
                                    onChange={this.handleDateChange('from')}
                                    clearable 
                                    margin="normal"
                                    className={classes.formControl}
                                    />
                                
                                <DatePicker id="until"
                                    label="End Date"
                                    value={untilDate}
                                    onChange={this.handleDateChange('until')}
                                    clearable
                                    margin="normal"
                                    className={classes.formControl}
                                    />
                                
                                <FormControl margin="normal" className={classes.formControl}>
                                    <InputLabel htmlFor="versionSelection">Version Selection</InputLabel>
                                    <Select id="versionSelection" 
                                        value={vs}
                                        input={<Input id="versionSelection-inp" />}
                                        onChange={this.handleChange('versionSelection')}
                                        
                                    >
                                        <MenuItem key={"DEFAULT"} value={"DEFAULT"}>Default</MenuItem>
                                        <MenuItem key={"FORCE_LATEST"} value={"FORCE_LATEST"}>Force selection to be the resource version the user is viewing</MenuItem>
                                        <MenuItem key={"FORCE_CURRENT"} value={"FORCE_CURRENT"}>Force selection to always be the latest live resource version</MenuItem>
                                        <MenuItem key={"DEFAULT_TO_LATEST"} value={"DEFAULT_TO_LATEST"}>User can choose, but default to be the resource version the user is viewing</MenuItem>
                                        <MenuItem key={"DEFAULT_TO_CURRENT"} value={"DEFAULT_TO_CURRENT"}>User can choose, but default to be the latest live resource version</MenuItem>
                                    </Select>
                                    <FormHelperText>When accessing EQUELLA via this course in an external system, all resources added to the external system will use this version selection strategy</FormHelperText>
                                </FormControl>

                                <TextField id="students" 
                                    label="Unique Individuals" 
                                    //helperText=""
                                    value={students || ''}
                                    onChange={this.handleChange('students')}
                                    margin="normal"
                                    className={classes.formControl}
                                    />

                                <FormGroup className={classes.formControl}>
                                    <FormControlLabel 
                                        label="Archived"
                                        control={<Switch
                                            checked={archived || false}
                                            onChange={this.handleCheckboxChange('archived')}
                                            value="archived"
                                        />}
                                    />
                                </FormGroup>
                            </div>
                        </Grid>
                    </div>

                    <div className={this.state.activeTab === 1 ? "" : classes.hiddenTab } style={{ height: "100%", overflowY: "hidden" }}>
                        { /* TODO: priv list from API */ }
                        <AclEditor 
                            onChange={ this.handleAclChange() }
                            acls={rules} 
                            allowedPrivs={["EDIT_COURSE_INFO", "DELETE_COURSE_INFO", "LIST_COURSE_INFO", "VIEW_COURSE_INFO"]}/>
                    </div>
                {/* </SwipeableViews> */}
            </div>

            <Paper component="footer" className={classes.footer}>
                <div className={classes.footerActions}>
                    <Button onClick={router(routes.CoursesPage).onClick} color="primary">Cancel</Button>
                    <Button onClick={this.handleSave.bind(this)} color="primary"
                        disabled={!this.state.canSave}>Save</Button>
                </div>               
            </Paper>
        </Template>
    }
}

function mapStateToProps(state: StoreState) {
    const { course } = state;
    return {
        entity: course.editingEntity
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers, actions } = courseService;
    return {
        loadEntity: (uuid: string) => workers.read(dispatch, {uuid}),
        saveEntity: (entity: Course) => workers.update(dispatch, {entity}),
        modifyEntity: (entity: Course) => dispatch(actions.modify({entity: entity}))
    };
}

export default withStyles(styles)(connect(mapStateToProps, mapDispatchToProps)(EditCourse as any) as any);