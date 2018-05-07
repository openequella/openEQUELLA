import * as React from 'react';
import { Course } from '../api';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import List from 'material-ui/List';
import SearchResult from '../components/SearchResult';
import { Bridge } from '../api/bridge';
import { Paper } from 'material-ui';
import AppBarQuery from '../components/AppBarQuery';
import { courseService } from '../services';
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
    bridge: Bridge;
    courses: Course[];
    onSearch: (query?: string) => void
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
        const {query} = this.state;
        return <Template title="Search Courses" titleExtra={<AppBarQuery query={query} onChange={this.handleQuery}/>}>
                <Paper>
                    <List>
                    {
                        (this.props.courses ?
                            this.props.courses.map((course) => {
                                const courseEditRoute = router(routes.CourseEdit(course.uuid));
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
                </Paper>
            </Template>
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
    const { workers } = courseService;
    return {
        onSearch: (query?: string) => workers.search(dispatch, {query})
    }
}

//export default withStyles(styles)<{}>(connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any) as any);
export default connect(mapStateToProps, mapDispatchToProps)(SearchCourse as any);