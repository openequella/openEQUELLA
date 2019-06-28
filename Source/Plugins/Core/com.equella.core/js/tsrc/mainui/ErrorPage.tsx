import * as React from "react";
import { ErrorResponse } from "../api/errors";
import { makeStyles } from "@material-ui/styles";
import { CardContent, Card, Typography } from "@material-ui/core";

const useStyles = makeStyles(t => ({
  errorPage: {
    display: "flex",
    justifyContent: "center",
    marginTop: t.spacing.unit * 8
  }
}));

export default React.memo(function ErrorPage(props: { error: ErrorResponse }) {
  const classes = useStyles();
  const { code, error, error_description } = props.error;
  return (
    <div id="errorPage" className={classes.errorPage}>
      <Card>
        <CardContent>
          <Typography variant="h3" color="error">
            {code} : {error}
          </Typography>
          {error_description && (
            <Typography variant="h5">{error_description}</Typography>
          )}
        </CardContent>
      </Card>
    </div>
  );
});
