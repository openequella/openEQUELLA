
import * as React from 'react';
import Downshift, { GetItemPropsOptions } from 'downshift';
import { withStyles, Theme, createStyles, WithStyles } from '@material-ui/core/styles';
import { Course, PagingResults } from '../api';
import { TextField, MenuItem, Paper } from '@material-ui/core';
import { searchCourses } from '../course';

const styles = (theme : Theme) => createStyles({
  root: {
    flexGrow: 1,
    height: 250,
  },
  container: {
    flexGrow: 1,
    position: 'relative',
  },
  paper: {
    position: 'absolute',
    zIndex: 1,
    marginTop: theme.spacing.unit,
    left: 0,
    right: 0,
  },
  chip: {
    margin: `${theme.spacing.unit / 2}px ${theme.spacing.unit / 4}px`,
  },
  inputRoot: {
    flexWrap: 'wrap',
  },
  divider: {
    height: theme.spacing.unit * 2,
  },
  });

interface CourseItemProps {
  query: string;
  getItemProps(options: (GetItemPropsOptions<Course>)): any;
}

interface CourseItemState {
  courses: Course[];
}

class CourseItems extends React.Component<CourseItemProps, CourseItemState>
{
  constructor(props: CourseItemProps)
  {
        super(props);
        this.state = {courses:[] };
  }
  loadCourses = () => {
    const {query} = this.props;
    searchCourses(query, false, 5).then(
      (value: PagingResults<Course>) => this.setState({courses: value.results})
    )
  };

  componentDidMount() {
    this.loadCourses();
  }

  componentDidUpdate(oldProps: CourseItemProps) {
    const {query} = this.props;
    if (oldProps.query != query)
    {
      this.loadCourses();
    }
  }
  render() {
    const {courses} = this.state;
    const {getItemProps} = this.props;
    return courses.map((course: Course) => {
      let itemProps = getItemProps({item: course});
      return <MenuItem {...itemProps} 
        key={course.name} 
        component="div">
        {course.name}
      </MenuItem>
    });
  }
}

interface CourseSelectProps  {
  course: Course | null;
  onCourseSelect: (course: Course) => void;
}

interface CourseSelectState {
    suggestions: Course[];
    inputValue: string;
}

class CourseSelect extends React.Component<CourseSelectProps & WithStyles<typeof styles>, CourseSelectState> {
    constructor(props: CourseSelectProps & WithStyles<typeof styles>)
    {
        super(props);
        this.state = {suggestions:[], inputValue: props.course ? props.course.name : ""};
    }

    itemChanged = (selectedItem: Course) => {
      this.setState({inputValue: selectedItem.name});
      this.props.onCourseSelect(selectedItem)
    }
    handleInputChange = (event: any) => {
      let inputValue = event.target.value;
      this.setState({ inputValue });
    };
    render() {
        const {classes, course} = this.props;
        const {inputValue } = this.state;
        return <Downshift itemToString={(c: Course) => ""} inputValue={inputValue} selectedItem={course} onChange={ this.itemChanged } 
        >
        {({
          getInputProps,
          getItemProps,
          getMenuProps,
          highlightedIndex,
          inputValue,
          isOpen,
          selectedItem,
        }) => <div className={classes.container}>
            <TextField 
            InputProps={...getInputProps(
              {onChange: this.handleInputChange}
            )}
            />
            {isOpen && inputValue ? 
            <Paper className={classes.paper} square {...getMenuProps()}>
              <CourseItems query={inputValue} getItemProps={getItemProps}/> 
            </Paper> : null}
          </div>
        }
      </Downshift>
    }
}

export default withStyles(styles)(CourseSelect);
