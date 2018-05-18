import * as React from 'react';
import { Course, SearchResults } from '../api';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import List from '@material-ui/core/List';
import SearchResult from '../components/SearchResult';
import { Bridge } from '../api/bridge';
import { Paper, Theme, Typography } from '@material-ui/core';
import AppBarQuery from '../components/AppBarQuery';
import { courseService } from '../services';
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles';

const styles = (theme: Theme) => ({
    overall: {
      padding: theme.spacing.unit * 3, 
      height: "100%"
    }, 
    results: {
      height: "100%",
      padding: theme.spacing.unit * 2
    }
});

interface SearchCourseProps extends WithStyles<'results' | 'overall'> {
    bridge: Bridge;
    courses: Course[];
    onSearch: (query: string) => Promise<{results: SearchResults<Course>}>
}

interface SearchCourseState {
    query: string;
}

class SearchCourse extends React.Component<SearchCourseProps, SearchCourseState> {

    constructor(props: SearchCourseProps){
        super(props);
        this.props.onSearch("");
        this.state = {
            query: ""
        }
    }

    handleQuery = (q: string) => {
        this.setState({query:q});
        this.props.onSearch(q);
    }

    render() {
        const {routes,router, Template} = this.props.bridge;
        const {classes,courses} = this.props;
        const {query} = this.state;
        return <Template title="Courses" titleExtra={<AppBarQuery query={query} onChange={this.handleQuery}/>}>
            <div className={classes.overall}>
                <Paper className={classes.results}>
                {
                    courses.length == 0 ? <Typography variant="subheading">No results available</Typography> :
                    <List>
                    {
                        courses.map((course) => {
                                const courseEditRoute = router(routes.CourseEdit(course.uuid));
                                return <SearchResult key={course.uuid} 
                                    href={courseEditRoute.href}
                                    onClick={courseEditRoute.onClick}
                                    primaryText={course.code + " - " + course.name}
                                    secondaryText={course.description} />
                            })
                    }
                    </List>
                }
                </Paper>
            </div>
        </Template>
    }
}

function mapStateToProps(state: StoreState) {
    const { course } = state;
    return {
        courses: course.entities
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = courseService;
    return {
        onSearch: (query: string) => workers.search(dispatch, {query, privilege:"EDIT_COURSE_INFO"})
    }
}

// What's with these any's? 
export default withStyles(styles)(connect(mapStateToProps, mapDispatchToProps)(SearchCourse));