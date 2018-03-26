import * as React from 'react';
import { Button, TextField, Grid } from 'material-ui';
import { Course } from '../api';
import { loadCourseWorker, saveCourseWorker, searchCoursesWorker } from './actions';
import { CourseStoreState } from './CourseStore';
import { connect, Dispatch } from 'react-redux';
import { push } from 'react-router-redux'

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
    loadCourse: (uuid: string) => void;
    saveCourse: (course: Course) => void;
    onCancel: () => void;
    course: Course;
    //fixme: remove
    doSearchAgain: (query?: string) => void;
    //root: any;
}

interface EditCourseState {
    uuid?: string;
    code?: string;
    name?: string;
    description?: string;
    departmentName?: string;
    citation?: string;
    students?: number;
    from?: string;
    until?: string;
    versionSelection?: string;
    archived?: boolean;
}

class EditCourse extends React.Component<EditCourseProps, EditCourseState> {

    constructor(props: EditCourseProps){
        super(props);
        var uuids = window.location.href.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
        if (uuids){
            this.props.loadCourse(uuids[0]);
        }

        this.state = {
            name: '',
            code: '',
            description: ''
        };
    }

    componentWillReceiveProps(nextProps: EditCourseProps){
        const course = nextProps.course;
        if (course){
            const { uuid, name, code, description, departmentName } = course;
            this.setState({ uuid, name, code, description, departmentName });
        }
    }

    handleSave() {
        const { uuid, name, code, description } = this.state;
        if (code){
            let course = {
                uuid,
                name: name!,
                code: code!,
                description
            };
            this.props.saveCourse(course);
        }
    }

    handleCancel() {
        this.props.onCancel();
    }

    handleChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.setState({ [stateFieldName]: event.target.value });
        };        
    }

    render() {
        const { code, name, description, departmentName } = this.state;
        /*
        <Stepper>
                <Step title="Basic Details" active>
                    
                </Step>
                <Step title="Permissions">
                </Step>
            </Stepper>*/
        return             <Grid>
                <div>
                    <TextField id="code" 
                        label="Code" 
                        helperText="Course code, e.g. EQ101"
                        value={code}
                        onChange={this.handleChange('code')}
                        fullWidth
                        margin="normal"
                        required
                            />

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

                    <TextField id="departmentName" 
                        label="Department Name" 
                        //helperText=""
                        value={departmentName}
                        onChange={this.handleChange('departmentName')}
                        fullWidth
                        margin="normal"
                        />

                    <Button color="primary" onClick={this.handleSave.bind(this)} variant="raised">Save</Button>
                    <Button onClick={this.handleCancel.bind(this)} variant="raised">Cancel</Button>
                </div>
            </Grid>
    }
}

function mapStateToProps(state: CourseStoreState) {
    return {
        course: state.editingCourse
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    return {
        loadCourse: (uuid: string) => loadCourseWorker(dispatch, {uuid}),
        saveCourse: (course: Course) => saveCourseWorker(dispatch, {course}),
        onCancel: () => dispatch(push('/')),
        doSearchAgain: (query?: string) => searchCoursesWorker(dispatch, {query})
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(EditCourse as any);