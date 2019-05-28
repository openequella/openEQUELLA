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
  const { code, error, description } = props.error;
  return (
    <div className={classes.errorPage}>
      <Card>
        <CardContent>
          <Typography variant="display2" color="error">
            {code} : {error}
          </Typography>
          {description && (
            <Typography variant="headline">{description}</Typography>
          )}
        </CardContent>
      </Card>
    </div>
  );
});
