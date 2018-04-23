import * as React from 'react';
import { Button, TextField, Grid, Select, InputLabel, Input, 
    MenuItem, Switch, FormGroup, FormControl, FormControlLabel, FormHelperText,
    IconButton, Icon, Tabs, Tab, Typography } from 'material-ui';
import SwipeableViews from 'react-swipeable-views';
import { DatePicker } from 'material-ui-pickers';
import { connect, Dispatch } from 'react-redux';
import { push } from 'react-router-redux';
import { format, parse } from 'date-fns';

import { Course } from '../api';
import courseService from './index';
import { StoreState } from '../store';
import { Bridge } from '../api/bridge';

//import List, { ListItem, ListItemText } from 'material-ui/List';
/*
import { withStyles } from 'material-ui/styles';
const styles = (theme: Theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper
    }
  });*/
/*
interface TabContainerProps {
    dir: string;
}
const TabContainer: React.SFC<TabContainerProps> = (props: TabContainerProps) => {
    return (
        <Typography component="div" dir={props.dir} style={{ padding: 8 * 3 }}>
        {props.children}
        </Typography>
    );
}
*/

interface EditCourseProps {
    bridge: Bridge;
    loadCourse: (uuid: string) => void;
    saveCourse: (course: Course) => void;
    course: Course;
}

interface EditCourseState {
    uuid?: string;
    code?: string;
    type?: string;
    name?: string;
    description?: string;
    departmentName?: string;
    citation?: string;
    students?: number | null;
    from?: Date | null;
    until?: Date | null;
    versionSelection?: string;
    archived?: boolean;
    activeTab?: number;
}

class EditCourse extends React.Component<EditCourseProps, EditCourseState> {

    constructor(props: EditCourseProps){
        super(props);

        this.state = {
            name: '',
            code: '',
            type: 'i',
            description: '',
            citation: '',
            students: null,
            from: null,
            until: null,
            versionSelection: 'DEFAULT',
            activeTab: 0
        };

        var uuids = window.location.href.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
        if (uuids){
            this.props.loadCourse(uuids[0]);
        }
    }

    componentWillReceiveProps(nextProps: EditCourseProps){
        const course = nextProps.course;
        if (course){
            const { uuid, name, code, description, departmentName, citation, students, from, until, versionSelection, archived } = course;
            const fromDate = (from ? parse(from!, 'YYYY-MM-DDTHH:mm:ss.SSSZ', new Date()) : null);
            const untilDate = (until ? parse(until!, 'YYYY-MM-DDTHH:mm:ss.SSSZ', new Date()) : null);
            this.setState({ uuid, name, code, description, departmentName, citation, students, from: fromDate, until: untilDate, versionSelection, archived });
        }
    }

    handleSave() {
        const { uuid, name, code, type, description, departmentName, citation, students, from, until, versionSelection, archived } = this.state;
        const fromStr = (from ? format(from, 'YYYY-MM-DDTHH:mm:ss.SSSZ') : undefined);
        const untilStr = (until ? format(until, 'YYYY-MM-DDTHH:mm:ss.SSSZ') : undefined);
        const vs = (versionSelection === "DEFAULT" ? undefined : versionSelection);
        if (code){
            let course = {
                uuid,
                name: name!,
                code: code!,
                type: type!,
                description,
                departmentName,
                citation,
                students: students || undefined,
                from: fromStr,
                until: untilStr,
                versionSelection: vs,
                archived
            };
            this.props.saveCourse(course);
        }
    }

    handleChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.setState({ [stateFieldName]: event.target.value });
        };
    }

    handleCheckboxChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.setState({ [stateFieldName]: event.target.checked });
        };
    }

    handleDateChange(stateFieldName: string): (date: string) => void {
        return (date: string) => {
            this.setState({ [stateFieldName]: date });
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

    render() {
        const { code, name, description, type, departmentName, citation, students, from, 
            until, versionSelection, archived, activeTab } = this.state;
        const vs = (versionSelection ? versionSelection : "DEFAULT");
        const {routes, router, AclEditor} = this.props.bridge;
        /*
        <Stepper>
                <Step title="Basic Details" active>
                    
                </Step>
                <Step title="Permissions">
                </Step>
            </Stepper>*/


        return  <div>
                    <IconButton aria-label="Back" 
                        onClick={router(routes.CoursesPage.value).onClick}>
                            <Icon>arrow_back</Icon>
                    </IconButton>
                    
                    <Tabs value={activeTab} onChange={this.handleTabChange()} fullWidth>
                        <Tab label="Course Details" />
                        <Tab label="Permissions" />
                    </Tabs>

                    <SwipeableViews
                        axis="x"
                        index={activeTab}
                        onChangeIndex={this.handleChangeTabIndex()}>

                        <Typography component="div" dir="ltr" style={{ padding: 8 * 3 }}>
                            <Grid>
                                <div>
                                    <TextField id="name" 
                                        label="Name" 
                                        helperText="Course name, e.g. Advanced EQUELLA studies"
                                        value={name}
                                        onChange={this.handleChange('name')}
                                        fullWidth
                                        margin="normal"
                                        required
                                        />

                                    <TextField id="description" 
                                        label="Description" 
                                        helperText="A brief description"
                                        value={description}
                                        onChange={this.handleChange('description')}
                                        fullWidth
                                        multiline
                                        rows={3}
                                        margin="normal"
                                        />

                                    <TextField id="code" 
                                        label="Code" 
                                        helperText="Course code, e.g. EQ101"
                                        value={code}
                                        onChange={this.handleChange('code')}
                                        fullWidth
                                        margin="normal"
                                        required
                                            />

                                    <FormControl fullWidth margin="normal">
                                        <InputLabel htmlFor="type">Course Type</InputLabel>
                                        <Select id="type" 
                                            value={type}
                                            input={<Input id="type-inp" />}
                                            onChange={this.handleChange('type')}
                                            fullWidth
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
                                        fullWidth
                                        margin="normal"
                                        />

                                    <FormControl fullWidth margin="normal">
                                        <InputLabel htmlFor="citation">Citation</InputLabel>
                                        <Select id="citation" 
                                            value={citation || ''}
                                            input={<Input id="citation-inp" />}
                                            onChange={this.handleChange('citation')}
                                            fullWidth
                                        >
                                            <MenuItem key={"harvard"} value={"harvard"}>
                                                harvard
                                            </MenuItem>
                                        </Select>
                                    </FormControl>

                                    <TextField id="students" 
                                        label="Unique Individuals" 
                                        //helperText=""
                                        value={students || ''}
                                        onChange={this.handleChange('students')}
                                        fullWidth
                                        margin="normal"
                                        />

                                    <DatePicker id="from"
                                        label="Start Date"
                                        value={from}
                                        onChange={this.handleDateChange('from')}
                                        fullWidth
                                        clearable 
                                        margin="normal"
                                        />
                                    
                                    <DatePicker id="until"
                                        label="End Date"
                                        value={until}
                                        onChange={this.handleDateChange('until')}
                                        fullWidth
                                        clearable
                                        margin="normal"
                                        />
                                    
                                    <FormControl fullWidth margin="normal">
                                        <InputLabel htmlFor="versionSelection">Version Selection</InputLabel>
                                        <Select id="versionSelection" 
                                            value={vs}
                                            input={<Input id="versionSelection-inp" />}
                                            onChange={this.handleChange('versionSelection')}
                                            fullWidth
                                        >
                                            <MenuItem key={"DEFAULT"} value={"DEFAULT"}>Default</MenuItem>
                                            <MenuItem key={"FORCE_LATEST"} value={"FORCE_LATEST"}>Force selection to be the resource version the user is viewing</MenuItem>
                                            <MenuItem key={"FORCE_CURRENT"} value={"FORCE_CURRENT"}>Force selection to always be the latest live resource version</MenuItem>
                                            <MenuItem key={"DEFAULT_TO_LATEST"} value={"DEFAULT_TO_LATEST"}>User can choose, but default to be the resource version the user is viewing</MenuItem>
                                            <MenuItem key={"DEFAULT_TO_CURRENT"} value={"DEFAULT_TO_CURRENT"}>User can choose, but default to be the latest live resource version</MenuItem>
                                        </Select>
                                        <FormHelperText>When accessing EQUELLA via this course in an external system, all resources added to the external system will use this version selection strategy</FormHelperText>
                                    </FormControl>

                                    <FormGroup>
                                        <FormControlLabel 
                                            label="Archived"
                                            control={<Switch
                                                checked={archived || false}
                                                onChange={this.handleCheckboxChange('archived')}
                                                value="archived"
                                            />}
                                        />
                                    </FormGroup>

                                    <Button color="primary" onClick={this.handleSave.bind(this)} variant="raised">Save</Button>
                                    
                                </div>
                            </Grid>
                        </Typography>

                        <Typography component="div" dir="ltr" style={{ padding: 8 * 3 }}><AclEditor acls={[
                            {privilege: "TEST_IT", granted:true, override:false, who: "*"}
                        ]} allowedPrivs={["TEST_IT"]}/></Typography>
                    </SwipeableViews>

            </div>
    }
}

function mapStateToProps(state: StoreState) {
    const { course } = state;
    return {
        course: course.editingEntity
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = courseService;
    return {
        loadCourse: (uuid: string) => workers.read(dispatch, {uuid}),
        saveCourse: (entity: Course) => workers.update(dispatch, {entity}),
        onCancel: () => dispatch(push('/')),
        doSearchAgain: (query?: string) => workers.search(dispatch, {query})
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(EditCourse as any);