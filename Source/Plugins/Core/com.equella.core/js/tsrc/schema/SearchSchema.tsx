import * as React from 'react';
import { Button, TextField } from 'material-ui';
import { Schema } from '../api';
import schemaService from './index';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import List from 'material-ui/List';
import SearchResult from '../components/SearchResult';
import { Bridge } from '../api/bridge';
/*
import withStyles, { StyleRulesCallback } from 'material-ui/styles/withStyles';
const styles: StyleRulesCallback<'root'> = (theme: Theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper
    }
  });*/

interface SearchSchemaProps {
    onSearch: (query?: string) => void;
    bridge: Bridge;
    query?: string;
    schemas: Schema[];
    root: any;
}

class SearchSchema extends React.Component<SearchSchemaProps, object> {

    textInput: HTMLInputElement;
    classes: any;

    constructor(props: SearchSchemaProps){
        super(props);
        this.classes = props;
    }

    onButtonClick() {
        this.props.onSearch(this.textInput.value);
    }

    render() {
        const {router,routes} = this.props.bridge;
        return <div className={this.classes.root}><div className="schemas">
                <div className="schemasSearch">
                    <TextField id="txtSchemaSearch" inputRef={(input: any) => { this.textInput = input; }} />
                    <Button color="primary" onClick={this.onButtonClick.bind(this)} variant="raised">Search</Button>
                </div>
                <List>
                {
                    (this.props.schemas ?
                        this.props.schemas.map((schema) => (
                            <SearchResult key={schema.uuid} 
                                href={router(routes.SchemaEdit.create(schema.uuid)).href}
                                onClick={router(routes.SchemaEdit.create(schema.uuid)).onClick}
                                primaryText={schema.name}
                                secondaryText={schema.description} />))
                        : <div>No Results</div>
                    )
                }
                </List>
            </div>
            </div>
    }
}

function mapStateToProps(state: StoreState) {
    const { schema } = state;
    return {
        query: schema.query,
        schemas: schema.entities
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = schemaService;
    return {
        onSearch: (query?: string) => workers.search(dispatch, {query})
    }
}

//export default withStyles(styles)<{}>(connect(mapStateToProps, mapDispatchToProps)(SearchSchema as any) as any);
export default connect(mapStateToProps, mapDispatchToProps)(SearchSchema as any);