import * as React from 'react';
import { Button, TextField } from 'material-ui';
import { Course } from './CourseModel';
import * as actions from './actions';
import { Routes, Route } from '../api/routes';
import { CourseStoreState } from './CourseStore';
import { connect, Dispatch } from 'react-redux';
//import { push } from 'react-router-redux';

import List, { ListItem, ListItemText } from 'material-ui/List';
/*
import withStyles, { StyleRulesCallback } from 'material-ui/styles/withStyles';
const styles: StyleRulesCallback<'root'> = (theme: Theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper
    }
  });*/

interface SearchCourseProps {
    onSearch: (query?: string) => void;
    routes: (route: Route) => {href:string, onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void };
    query?: string;
    courses: Course[];
    root: any;
}

class SearchCourse extends React.Component<SearchCourseProps, object> {

    textInput: HTMLInputElement;
    classes: any;

    constructor(props: SearchCourseProps){
        super(props);
        this.classes = props;
    }

    onButtonClick() {
        this.props.onSearch(this.textInput.value);
    }

    onView(uuid: string){
        //push(`/edit/${uuid}`);
        
        var href = (this.props.editHref ? this.props.editHref(uuid) : `/edit/${uuid}`);
        //const { history } = this.context.router;
        //history.pushState(null, '', href);
        window.location.href = href;
    }

    render() {
        return <div className={this.classes.root}><div className="courses">
                <div className="coursesSearch">
                    <TextField id="txtCourseSearch" inputRef={(input: any) => { this.textInput = input; }} />
                    <Button color="primary" onClick={this.onButtonClick.bind(this)} variant="raised">Search</Button>
                </div>
                <List>
                {
                    (this.props.courses ?
                        this.props.courses.map((course) => (
<<<<<<< HEAD
                            <ListItem key={course.uuid}>
                            <a key={course.uuid} href={this.props.routes(Routes.CourseEdit.create(course.uuid)).href} onClick={
                                this.props.routes(Routes.CourseEdit.create(course.uuid)).onClick}>
=======
                            <ListItem key={course.uuid} button onClick={() => {this.onView(course.uuid!)}}>
>>>>>>> wip
                                <ListItemText primary={course.code + " - " + course.name} secondary={course.description} />
                            </ListItem>))
                        : <div>No Results</div>
                    )
                }
                </List>
            </div>
            </div>
    }
}

function mapStateToProps(state: CourseStoreState) {
    return {
        query: state.query,
        courses: state.courses
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    return {
        onSearch: (query?: string) => actions.searchCoursesWorker(dispatch, {query})
    }
}

//export default withStyles(styles)<{}>(connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any) as any);
export default connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any);