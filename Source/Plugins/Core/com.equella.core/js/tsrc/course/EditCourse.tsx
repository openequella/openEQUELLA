import * as React from 'react';
import { Button, TextField, Stepper, Step, Drawer, Grid } from 'material-ui';
import { Course } from './CourseModel';
import { saveCourse, searchCourses } from './actions';
import { CourseStoreState } from './CourseStore';
import { connect, Dispatch } from 'react-redux';
import { push } from 'react-router-redux'


interface EditCoursesProps {
    onSave: (course: Course) => void;
    onCancel: () => void;
    course: Course;
    //fixme: remove
    doSearchAgain: (query?: string) => void;
}

class EditCourse extends React.Component<EditCoursesProps, object> {

    textName: HTMLInputElement;
    textCode: HTMLInputElement;

    handleSave() {
        let course = {
            name: this.textName.value,
            code: this.textCode.value
        };
        this.props.onSave(course);

        //FIXME: can we listen to onSaved?
        this.props.onCancel();

        //FIXME: remove
        this.props.doSearchAgain();
    }

    handleCancel() {
        this.props.onCancel();
    }

    render() {
        return <Drawer anchor="right" open>
            <Grid> 
            <Stepper>
                <Step title="Basic Details" active>
                    Basic Details
                </Step>
                <Step title="Permissions">
                        <div>TODO</div>
                </Step>
            </Stepper>

            <div>
                AAA
            <TextField id="txtCode" 
                        label="Code" 
                        helperText="Course code, e.g. EQ101" 
                        inputRef={(input: any) => { this.textCode = input; }}
                        fullWidth
                        //required
                            />
                    <TextField id="txtName" 
                        label="Name" 
                        helperText="Course name, e.g. Advanced EQUELLA studies"
                        inputRef={(input: any) => { this.textName = input; }} 
                        fullWidth
                        //required
                        />
                    
                    <Button color="primary" onClick={this.handleSave.bind(this)} variant="raised">Save</Button>
                    <Button onClick={this.handleCancel.bind(this)} variant="raised">Cancel</Button>
                </div>
            </Grid>
            </Drawer>
    }
}

function mapStateToProps(state: CourseStoreState) {
    return {
        course: state.editingCourse
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    return {
        onSave: (course: Course) => dispatch(saveCourse(course)),
        onCancel: () => dispatch(push('/')),
        doSearchAgain: (query?: string) => dispatch(searchCourses(query))
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(EditCourse as any);