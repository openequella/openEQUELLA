import { Button, Checkbox, CircularProgress, FormControlLabel, Paper, Theme, Typography } from '@material-ui/core';
import List from '@material-ui/core/List';
import withStyles, { StyleRules, WithStyles } from '@material-ui/core/styles/withStyles';
import AddIcon from '@material-ui/icons/Add';
import * as React from 'react';
import { Dispatch, connect } from 'react-redux';
import { searchCourses } from '.';
import { Course } from '../api';
import { Bridge } from '../api/bridge';
import AppBarQuery from '../components/AppBarQuery';
import ConfirmDialog from '../components/ConfirmDialog';
import SearchResult from '../components/SearchResult';
import { courseService } from '../services';
import { StoreState } from '../store';
import { prepLangStrings, sizedString } from '../util/langstrings';
import VisibilitySensor = require('react-visibility-sensor');

const styles = (theme: Theme) => ({
    overall: {
      padding: theme.spacing.unit * 3, 
      height: "100%"
    }, 
    results: {
      padding: theme.spacing.unit * 2, 
      position: "relative"
    }, 
    resultList: {
    },
    fab: {
      zIndex: 1000,
      position: 'fixed',
      bottom: theme.spacing.unit * 2,
      right: theme.spacing.unit * 5,
    }, 
    resultHeader: {
      display: "flex",
      justifyContent: "flex-end"
    }, 
    resultText: {
        flexGrow: 1
    }, 
    progress: {
        display: "flex", 
        justifyContent: "center"
    }
} as StyleRules)

interface SearchCourseProps extends WithStyles<'results' | 'overall' | 'fab' 
    | 'resultHeader' | 'resultText' | 'resultList' | 'progress'> {
    bridge: Bridge;
    deleteCourse: (uuid: string) => Promise<{uuid:string}>;
    checkCreate: () => Promise<boolean>;
}

interface SearchCourseState {
    query: string;
    confirmOpen: boolean;
    canCreate: boolean;
    includeArchived: boolean;
    searching: boolean;
    totalAvailable?: number;
    resumptionToken?: string;
    bottomVisible: boolean;
    courses: Course[];
    deleteDetails?: {
        uuid: string;
        name: string;
    }
}

const MaxCourses = 200;

export const strings = prepLangStrings("courses", {
    title: "Courses",
    sure: "Are you sure you want to delete - '%s'?", 
    confirmDelete: "It will be permanently deleted.", 
    coursesAvailable: {
        zero: "No courses available",
        one: "%d course",
        more: "%d courses"
    }, 
    includeArchived: "Include archived",
    archived: "Archived"
});

class SearchCourse extends React.Component<SearchCourseProps, SearchCourseState> {

    constructor(props: SearchCourseProps){
        super(props);
        this.state = {
            query: "",
            confirmOpen: false,
            canCreate: false,
            includeArchived: false,
            courses: [],
            searching: false, 
            bottomVisible: true
        }
    }

    maybeKeepSearching = () => {
        if (this.state.bottomVisible) 
        {
            this.fetchMore();
        }
    }

    fetchMore = () => {
        const {resumptionToken,searching, query, includeArchived, courses} = this.state;
        if (resumptionToken && !searching && courses.length < MaxCourses)
        {
            this.doSearch(query, includeArchived, false);
        }
    }

    nextSearch : NodeJS.Timer | null = null;

    doSearch = (q: string, includeArchived: boolean, reset: boolean) => {
        const resumptionToken = reset ? undefined : this.state.resumptionToken;
        const doReset = resumptionToken == undefined;
        const { bottomVisible } = this.state;
        this.setState({searching:true});
        searchCourses(q, includeArchived, resumptionToken, 30).then(sr => {
            if (sr.resumptionToken && bottomVisible) setTimeout(this.maybeKeepSearching, 250);
            this.setState((prevState) => ({...prevState, 
                courses: doReset ? sr.results : prevState.courses.concat(sr.results), 
                totalAvailable: sr.available, 
                resumptionToken: sr.resumptionToken, 
                searching: false
            }));
        });
    }

    searchFromState = () => {
        const {query,includeArchived} = this.state;
        this.doSearch(query, includeArchived, true);
    }
    handleQuery = (q: string) => {
        this.setState({query:q});
        if (this.nextSearch)
        {
            clearTimeout(this.nextSearch);
        }
        this.nextSearch = setTimeout(this.searchFromState, 250);
    }

    visiblityCheck = (bottomVisible: boolean) => this.setState((prevState) => 
        ({...prevState, bottomVisible: prevState.bottomVisible && bottomVisible}))

    componentWillUnmount() {
        window.removeEventListener('scroll', this.onScroll, false);        
    }
  
    onScroll = () => {
        if ((window.innerHeight + window.scrollY) >= (document.body.offsetHeight - 400))
        {
            this.fetchMore();
        }
    }

    componentDidMount()
    {
        window.addEventListener('scroll', this.onScroll, false);
        this.doSearch("", false, true);
        this.props.checkCreate().then(canCreate => this.setState({canCreate}));
    }

    handleArchived = (includeArchived: boolean) => {
        const {query} = this.state;
        this.setState({includeArchived})
        this.doSearch(query, includeArchived, true)
    }

    handleClose = () => {
        this.setState({confirmOpen:false});
    }

    handleDelete = () => {
        if (this.state.deleteDetails) {
            const { uuid } = this.state.deleteDetails;
            this.handleClose();
            const {includeArchived, query} = this.state;
            this.props.deleteCourse(uuid).then(
                _ => this.doSearch(query, includeArchived, true)
            );
        }
    }

    render() {
        const {routes,router, Template} = this.props.bridge;
        const {classes} = this.props;
        const {query,confirmOpen,canCreate,courses,totalAvailable,searching} = this.state;
        const {onClick:clickNew, href:hrefNew} = router(routes.NewCourse)
        return <Template title={strings.title} titleExtra={<AppBarQuery query={query} onChange={this.handleQuery}/>}>
            {canCreate && 
                <Button variant="fab" className={classes.fab} 
                href={hrefNew} onClick={clickNew} color="primary"><AddIcon/></Button>}
            <div className={classes.overall}>
                {this.state.deleteDetails && 
                    <ConfirmDialog open={confirmOpen} 
                        title={sprintf(strings.sure, this.state.deleteDetails.name)} 
                        onConfirm={this.handleDelete} onCancel={this.handleClose}>
                        {strings.confirmDelete}
                    </ConfirmDialog>}
                <Paper className={classes.results}>
                    <div className={classes.resultHeader}>
                        <Typography className={classes.resultText} variant="subheading">{
                            courses.length == 0 ? strings.coursesAvailable.zero : 
                            sprintf(sizedString(totalAvailable||0, strings.coursesAvailable), totalAvailable||0)
                        }</Typography>
                        <FormControlLabel 
                        control={<Checkbox onChange={(e,includeArchived) => this.handleArchived(includeArchived)}/>} 
                            label={strings.includeArchived}/>
                    </div>
                    <List className={classes.resultList}>
                    {
                    courses.map((course) => {
                            const courseEditRoute = router(routes.CourseEdit(course.uuid));
                            var onDelete;
                            if (course.uuid && course.readonly && course.readonly.granted.indexOf("DELETE_COURSE_INFO") != -1)
                            {
                                const deleteDetails = {uuid: course.uuid, name: course.name};
                                onDelete = () => this.setState({confirmOpen:true, deleteDetails});
                            }
                            var text = course.code + " - " + course.name;
                            if (course.archived){
                                text = text + ' (' + strings.archived + ')';
                            }
                            return <SearchResult key={course.uuid} 
                                href={courseEditRoute.href}
                                onClick={courseEditRoute.onClick}
                                primaryText={text}
                                secondaryText={course.description} 
                                onDelete={onDelete} />
                        })
                    }
                    <VisibilitySensor onChange={this.visiblityCheck}/>
                    </List>
                    {searching && <div className={classes.progress}><CircularProgress/></div>}
                </Paper>
            </div>
        </Template>
    }
}

function mapStateToProps(state: StoreState) {
    return {};
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = courseService;
    return {
        deleteCourse: (uuid: string) => workers.delete(dispatch, {uuid}), 
        checkCreate: () => workers.checkPrivs(dispatch, {privilege:["CREATE_COURSE_INFO"]}).then(p => p.indexOf("CREATE_COURSE_INFO") != -1)
    }
}

// What's with these any's? 
export default withStyles(styles)(connect(mapStateToProps, mapDispatchToProps)(SearchCourse));