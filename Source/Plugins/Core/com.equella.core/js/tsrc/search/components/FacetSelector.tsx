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
import {
  Button,
  Checkbox,
  FormControlLabel,
  Grid,
  List,
  ListItem,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { ReactElement, useState } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { Classification } from "../../modules/SearchFacetsModule";
import { languageStrings } from "../../util/langstrings";

const useStyles = makeStyles({
  classificationList: {
    maxHeight: 500,
    overflow: "auto",
  },
});

/**
 * Represent a schema node and a list of terms.
 */
export interface NodeAndTerms {
  /**
   * One Schema node.
   */
  node: string;
  /**
   * Terms related to this node.
   */
  terms: string[];
}

export interface FacetSelectorProps {
  /**
   * A list of Classifications.
   */
  classifications: Classification[];
  /**
   * A map where the key is a Classification's ID and value is
   * a list of terms.
   */
  selectedClassificationTerms?: Map<number, NodeAndTerms>;
  /**
   * Handler for selecting/deselecting Classification terms.
   * @param terms A list of currently selected terms.
   */
  onSelectTermsChange: (terms: Map<number, NodeAndTerms>) => void;
}

export const FacetSelector = ({
  classifications,
  selectedClassificationTerms,
  onSelectTermsChange,
}: FacetSelectorProps) => {
  const classes = useStyles();
  const [showMoreMap, setShowMoreMap] = useState<Map<number, boolean>>(
    new Map(classifications.map((classification) => [classification.id, true]))
  );

  const onShowMore = (classificationID: number, showMore: boolean) => {
    const copiedMap = new Map(showMoreMap);
    copiedMap.set(classificationID, showMore);
    setShowMoreMap(copiedMap);
  };
  /**
   * Updates the list of selected Classification terms. If the term exists then remove it
   * from the list. Add it to the list otherwise.
   * A copy of the map and a copy of the array of terms are created internally
   * to avoid mutating parent component's state.
   *
   * @param classificationID The ID of a Classification
   * @param schemaNode The Schema node of a Classification
   * @param term The selected or unselected term
   */
  const handleSelectTerms = (
    classificationID: number,
    schemaNode: string,
    term: string
  ) => {
    const nodeAndTerms = selectedClassificationTerms?.get(classificationID);
    const copiedTerms = nodeAndTerms ? [...nodeAndTerms.terms] : [];
    const termIndex = copiedTerms.indexOf(term);
    if (termIndex === -1) {
      copiedTerms.push(term);
    } else {
      copiedTerms.splice(termIndex, 1);
    }
    const copiedMap = new Map(selectedClassificationTerms ?? []);
    copiedMap.set(classificationID, { node: schemaNode, terms: copiedTerms });
    onSelectTermsChange(copiedMap);
  };

  /**
   * Create a button to show more/less categories for each Classification.
   * @param classificationID The ID of a Classification.
   * @param showMore Whether to display 'Show more' or 'Show less'.
   */
  const showMoreButton = (
    classificationID: number,
    showMore: boolean
  ): ReactElement => (
    <ListItem>
      <Grid container justify="center">
        <Grid item>
          <Button
            variant="text"
            onClick={() => onShowMore(classificationID, !showMore)}
          >
            {showMore
              ? languageStrings.searchpage.facetSelector.showMoreButton
              : languageStrings.searchpage.facetSelector.showLessButton}
          </Button>
        </Grid>
      </Grid>
    </ListItem>
  );

  /**
   * Generate texts in the format of 'term (count)' for displaying a facet.
   * @param term The term of a facet
   * @param count The count of a facet
   */
  const facetLabel = ({
    term,
    count,
  }: OEQ.SearchFacets.Facet): ReactElement => (
    <Grid container spacing={1}>
      <Grid item>
        <Typography>{term}</Typography>
      </Grid>
      <Grid item>
        <Typography color="textSecondary">{`(${count})`}</Typography>
      </Grid>
    </Grid>
  );

  /**
   * Build a ListItem consisting of a MUI Checkbox and a Label for a facet.
   * @param classificationID The name of a Classification
   * @param schemaNode The Schema node of a Classification
   * @param facet A facet
   */
  const facetListItem = (
    classificationID: number,
    schemaNode: string,
    facet: OEQ.SearchFacets.Facet
  ): ReactElement => {
    const { term } = facet;
    return (
      <ListItem key={`${classificationID}:${term}`} style={{ padding: 0 }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={
                selectedClassificationTerms
                  ?.get(classificationID)
                  ?.terms.includes(term) ?? false
              }
              onChange={() =>
                handleSelectTerms(classificationID, schemaNode, term)
              }
            />
          }
          label={facetLabel(facet)}
        />
      </ListItem>
    );
  };

  /**
   * Build a list for a Classification's categories. Some categories may have facets
   * not displayed due to the configured maximum display number.
   *
   * @param id The ID of a Classification
   * @param categories A list of terms to build into a list
   * @param schemaNode The Schema node of a Classification
   * @param maxDisplay Default maximum number of displayed facets
   * @param showMore Whether to show more facets or not
   */
  const listCategories = (
    { id, categories, schemaNode, maxDisplay }: Classification,
    showMore: boolean
  ): ReactElement[] => {
    const selectedTerms = selectedClassificationTerms?.get(id)?.terms ?? [];
    const selectedCategories = categories.filter((c) =>
      selectedTerms.includes(c.term)
    );
    const unselectedCategories = categories.filter(
      (c) => !selectedTerms.includes(c.term)
    );
    // Concatenate again to ensure selected ones have higher priority for display.
    return selectedCategories
      .concat(unselectedCategories)
      .slice(0, showMore ? maxDisplay : undefined)
      .map((facet) => facetListItem(id, schemaNode, facet));
  };
  /**
   * Sort and build Classifications that have categories.
   * For each Classification, a scroll bar and a 'Show more' button may or may not
   * be added, depending on whether a classification has more categories to show or not.
   */
  const buildClassifications: ReactElement[] = classifications
    .filter((classification) => classification.categories.length > 0)
    .sort(
      (prevClassification, nextClassification) =>
        prevClassification.orderIndex - nextClassification.orderIndex
    )
    .map((classification) => {
      const { id, name, categories, maxDisplay } = classification;
      const showMore = showMoreMap.get(id) ?? true;
      return (
        <ListItem divider key={id}>
          <Grid container direction="column">
            <Grid item>
              <Typography variant="subtitle1">{name}</Typography>
            </Grid>
            <Grid item>
              <List
                dense
                className={!showMore ? classes.classificationList : ""}
              >
                {listCategories(classification, showMore)}
                {categories.length > maxDisplay && showMoreButton(id, showMore)}
              </List>
            </Grid>
          </Grid>
        </ListItem>
      );
    });

  return <List>{buildClassifications}</List>;
};
