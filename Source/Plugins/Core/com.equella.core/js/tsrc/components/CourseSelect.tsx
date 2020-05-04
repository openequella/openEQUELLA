/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from "react";
import Downshift, { GetItemPropsOptions } from "downshift";
import {
  withStyles,
  Theme,
  createStyles,
  WithStyles,
} from "@material-ui/core/styles";
import { Course, PagingResults } from "../api";
import { TextField, MenuItem, Paper } from "@material-ui/core";
import { searchCourses } from "../course";
import { TextFieldProps } from "@material-ui/core/TextField";

const styles = (theme: Theme) =>
  createStyles({
    container: {
      position: "relative",
    },
    paper: {
      position: "absolute",
      zIndex: 1,
      marginTop: theme.spacing(1),
      left: 0,
      right: 0,
    },
    chip: {
      margin: `${theme.spacing(0.5)}px ${theme.spacing(0.25)}px`,
    },
    inputRoot: {
      flexWrap: "wrap",
    },
    divider: {
      height: theme.spacing(2),
    },
  });

interface CourseItemProps {
  query: string;
  maxResults: number;

  getItemProps(options: GetItemPropsOptions<Course>): any;
}

interface CourseItemState {
  courses: Course[];
}

function courseToString(c: Course): string {
  return `${c.code} - ${c.name}`;
}

class CourseItems extends React.Component<CourseItemProps, CourseItemState> {
  constructor(props: CourseItemProps) {
    super(props);
    this.state = { courses: [] };
  }

  loadCourses = () => {
    const { query, maxResults } = this.props;
    searchCourses(
      query,
      false,
      maxResults
    ).then((value: PagingResults<Course>) =>
      this.setState({ courses: value.results })
    );
  };

  componentDidMount() {
    this.loadCourses();
  }

  componentDidUpdate(oldProps: CourseItemProps) {
    const { query } = this.props;
    if (oldProps.query != query) {
      this.loadCourses();
    }
  }

  render() {
    const { courses } = this.state;
    const { getItemProps } = this.props;
    return courses.map((course: Course) => {
      const itemProps = getItemProps({ item: course });
      return (
        <MenuItem {...itemProps} key={course.name} component="div">
          {courseToString(course)}
        </MenuItem>
      );
    });
  }
}

interface CourseSelectProps {
  course: Course | null;
  TextFieldProps?: TextFieldProps;
  title: string;
  maxResults: number;
  onCourseSelect: (course: Course | null) => void;
}

interface CourseSelectState {
  suggestions: Course[];
  inputValue: string;
}

class CourseSelect extends React.Component<
  CourseSelectProps & WithStyles<typeof styles>,
  CourseSelectState
> {
  constructor(props: CourseSelectProps & WithStyles<typeof styles>) {
    super(props);
    this.state = {
      suggestions: [],
      inputValue: props.course ? props.course.name : "",
    };
  }

  itemChanged = (selectedItem: Course) => {
    this.setState({ inputValue: courseToString(selectedItem) });
    this.props.onCourseSelect(selectedItem);
  };
  handleInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    const inputValue = event.currentTarget.value;
    this.setState({ inputValue });
    this.props.onCourseSelect(null);
  };

  render() {
    const { classes, title, course } = this.props;
    const { inputValue } = this.state;
    return (
      <Downshift
        itemToString={(c: Course) => ""}
        inputValue={inputValue}
        selectedItem={course}
        onChange={this.itemChanged}
      >
        {({
          getInputProps,
          getItemProps,
          getMenuProps,
          highlightedIndex,
          inputValue,
          isOpen,
          selectedItem,
        }) => (
          <div className={classes.container}>
            <TextField
              {...this.props.TextFieldProps}
              label={title}
              placeholder={"Search on name and code..."}
              InputProps={...getInputProps({
                onChange: this.handleInputChange,
                onFocus: (e) => e.target.select(),
              })}
            />
            {isOpen && inputValue ? (
              <Paper className={classes.paper} square {...getMenuProps()}>
                <CourseItems
                  query={inputValue}
                  maxResults={this.props.maxResults}
                  getItemProps={getItemProps}
                />
              </Paper>
            ) : null}
          </div>
        )}
      </Downshift>
    );
  }
}

export default withStyles(styles)(CourseSelect);
