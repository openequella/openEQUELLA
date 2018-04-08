import * as React from 'react';
import { Button, TextField } from 'material-ui';
import { Course } from '../api';
import * as actions from './actions';
import { Routes, Route } from '../api/routes';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import List from 'material-ui/List';
import SearchResult from '../components/SearchResult';
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
    routes: (route: any) => Route;
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

    render() {
        
        return <div className={this.classes.root}><div className="courses">
                <div className="coursesSearch">
                    <TextField id="txtCourseSearch" inputRef={(input: any) => { this.textInput = input; }} />
                    <Button color="primary" onClick={this.onButtonClick.bind(this)} variant="raised">Search</Button>
                </div>
                <List>
                {
                    (this.props.courses ?
                        this.props.courses.map((course) => {
                            const courseEditRoute = this.props.routes(Routes.CourseEdit.create(course.uuid));
                            return <SearchResult key={course.uuid} 
                                href={courseEditRoute.href}
                                onClick={courseEditRoute.onClick}
                                primaryText={course.code + " - " + course.name}
                                secondaryText={course.description} />
                        })
                        : <div>No Results</div>
                    )
                }
                </List>
            </div>
            </div>
    }
}

function mapStateToProps(state: StoreState) {
    const { course } = state;
    return {
        query: course.query,
        courses: course.entities
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    return {
        onSearch: (query?: string) => actions.searchCoursesWorker(dispatch, {query})
    }
}

//export default withStyles(styles)<{}>(connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any) as any);
export default connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any);