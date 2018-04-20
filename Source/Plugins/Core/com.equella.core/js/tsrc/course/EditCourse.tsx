import * as React from 'react';
import { Button, TextField, Grid, Select, InputLabel, Input, 
    MenuItem, Checkbox, FormGroup, FormControl, FormControlLabel, IconButton, Icon } from 'material-ui';
import { DatePicker } from 'material-ui-pickers';
import { connect, Dispatch } from 'react-redux';
import { push } from 'react-router-redux';
import { format, parse } from 'date-fns';

import { Course } from '../api';
import courseService from './index';
import { StoreState } from '../store';
import { Routes, Route } from '../api/routes';

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

interface EditCourseProps {
    routes: (route: any) => Route;
    loadCourse: (uuid: string) => void;
    saveCourse: (course: Course) => void;
    course: Course;
}

interface EditCourseState {
    uuid?: string;
    code?: string;
    name?: string;
    description?: string;
    departmentName?: string;
    citation?: string;
    students?: number;
    from?: Date | null;
    until?: Date | null;
    versionSelection?: string;
    archived?: boolean;
}

class EditCourse extends React.Component<EditCourseProps, EditCourseState> {

    constructor(props: EditCourseProps){
        super(props);

        this.state = {
            name: '',
            code: '',
            description: '',
            citation: '',
            from: null,
            until: null
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
            const fromDate = (from ? parse(from!, 'YYYY-MM-DDTHH:mm:ss', new Date()) : null);
            const untilDate = (until ? parse(until!, 'YYYY-MM-DDTHH:mm:ss', new Date()) : null);
            this.setState({ uuid, name, code, description, departmentName, citation, students, from: fromDate, until: untilDate, versionSelection, archived });
        }
    }

    handleSave() {
        const { uuid, name, code, description, departmentName, citation, students, from, until, versionSelection, archived } = this.state;
        const fromStr = (from ? format(from, 'YYYY-MM-DDTHH:mm:ss') : undefined);
        const untilStr = (until ? format(until, 'YYYY-MM-DDTHH:mm:ss') : undefined);
        if (code){
            let course = {
                uuid,
                name: name!,
                code: code!,
                description,
                departmentName,
                citation,
                students,
                from: fromStr,
                until: untilStr,
                versionSelection,
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

    handleDateChange(stateFieldName: string): (date: string) => void {
        return (date: string) => {
            this.setState({ [stateFieldName]: date });
        };
    }

    render() {
        const { code, name, description, departmentName, citation, students, /* from, until, AARON PLS*/ versionSelection, archived } = this.state;
        /*
        <Stepper>
                <Step title="Basic Details" active>
                    
                </Step>
                <Step title="Permissions">
                </Step>
            </Stepper>*/
        return  <div>
                    <IconButton aria-label="Back" 
                        onClick={this.props.routes(Routes().CoursesPage.value).onClick}>
                            <Icon>arrow_back</Icon>
                    </IconButton>
                    
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
                        label="Students" 
                        //helperText=""
                        value={students}
                        onChange={this.handleChange('students')}
                        fullWidth
                        margin="normal"
                        />

                    <DatePicker id="from"
                        label="Date From"
                        // value={from}
                        onChange={this.handleDateChange('from')}
                        fullWidth
                        clearable
                        margin="normal"
                        />
                    
                    <DatePicker id="until"
                        label="Date Until"
                        // value={until}
                        onChange={this.handleDateChange('until')}
                        fullWidth
                        clearable
                        margin="normal"
                        />
                    
                    <TextField id="versionSelection" 
                        label="Version Selection" 
                        //helperText=""
                        value={versionSelection}
                        onChange={this.handleChange('versionSelection')}
                        fullWidth
                        margin="normal"
                        />

                    <FormGroup>
                        <FormControlLabel 
                            label="Archived"
                            control={<Checkbox id="archived" 
                                checked={archived || false} 
                                onChange={this.handleChange('archived')} />}
                        />
                    </FormGroup>

                    <Button color="primary" onClick={this.handleSave.bind(this)} variant="raised">Save</Button>
                    
                </div>
            </Grid>
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