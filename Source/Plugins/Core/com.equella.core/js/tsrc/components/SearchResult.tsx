import * as React from 'react'
import { ListItem, ListItemText } from 'material-ui/List';
import { Theme, withStyles, WithStyles } from 'material-ui/styles';
import { Typography } from 'material-ui';

const styles = withStyles((theme: Theme) => (
{
    searchResultContent: {
        display: "flex",
        marginTop: "8px"
    },
    itemThumb: {
        maxWidth: "88px",
        maxHeight: "66px",
        marginRight: "12px"
    },
    displayNode: {
        padding: 0
    },
    resultLink: {
        textDecoration: "none",
        fontSize: "1.3125rem",
        // fontWeight: 500,
        fontFamily: "Roboto, Helvetica, Arial, sans-serif",
        lineHeight: "1.16667em"
    }
}));

export interface SearchResultProps {
    href: string;
    onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    primaryText: string;
    secondaryText?: string;
}

type PropsWithStyles = SearchResultProps & WithStyles<"searchResultContent" | "itemThumb" | "displayNode" | "resultLink">

class SearchResult extends React.Component<PropsWithStyles> {
    render() {
        const {classes} = this.props
    const link: any = <a href={this.props.href} className={classes.resultLink} onClick={this.props.onClick}>{this.props.primaryText}</a>
        //
        const content: any = <div className={classes.displayNode}>{this.props.secondaryText}</div>;
        return <ListItem button onClick={this.props.onClick}>
                <ListItemText disableTypography primary={link} secondary={content} />
        </ListItem>;
    }
}

export default styles<SearchResultProps>(SearchResult);