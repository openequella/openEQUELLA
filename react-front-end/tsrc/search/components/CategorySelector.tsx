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
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useState } from "react";
import {
  Classification,
  SelectedCategories,
} from "../../modules/SearchFacetsModule";
import { languageStrings } from "../../util/langstrings";

const PREFIX = "CategorySelector";

const classes = {
  classificationList: `${PREFIX}-classificationList`,
  classificationListItem: `${PREFIX}-classificationListItem`,
  categoryListCheckbox: `${PREFIX}-categoryListCheckbox`,
  categoryListItemCount: `${PREFIX}-categoryListItemCount`,
};

const StyledList = styled(List)(({ theme }) => {
  return {
    [`& .${classes.classificationList}`]: {
      maxHeight: 500,
      overflow: "auto",
    },
    [`& .${classes.classificationListItem}`]: {
      paddingTop: theme.spacing(1),
    },
    [`& .${classes.categoryListCheckbox}`]: {
      overflow: "visible",
    },
    [`& .${classes.categoryListItemCount}`]: {
      paddingLeft: theme.spacing(1),
    },
  };
});

export interface CategorySelectorProps {
  /**
   * A list of Classifications which will be rendered to sections for each
   * Classifications under which will be the categories as clickable checkboxes.
   */
  classifications: Classification[];
  /**
   * A list of selected categories which are grouped by their Classification ID.
   */
  selectedCategories?: SelectedCategories[];
  /**
   * Handler for selecting/deselecting categories.
   * @param terms A list of currently selected terms.
   */
  onSelectedCategoriesChange: (categories: SelectedCategories[]) => void;
}

export const CategorySelector = ({
  classifications,
  selectedCategories = [],
  onSelectedCategoriesChange,
}: CategorySelectorProps) => {
  const [expandedClassifications, setExpandedClassifications] = useState<
    Map<number, boolean>
  >(
    new Map(
      classifications.map((classification) => [classification.id, false]),
    ),
  );

  /**
   * Handler for clicking the 'SHOW MORE' and 'SHOW LESS' buttons.
   * @param classificationID The ID of a Classification whose SHOW MORE' and 'SHOW LESS' buttons is clicked.
   * @param expanded Whether the section of this Classification is expanded or not.
   */
  const onShowMore = (classificationID: number, expanded: boolean) => {
    const copiedMap = new Map(expandedClassifications);
    copiedMap.set(classificationID, expanded);
    setExpandedClassifications(copiedMap);
  };

  /**
   * The list of selected categories are grouped by Classification ID.
   * If there is a group matching the ID, then update this group's selected categories,
   * and otherwise add a new group for the ID and its firstly selected category.
   *
   * @param classificationID The ID of a Classification
   * @param category The selected or unselected category
   */
  const handleSelectCategories = (
    classificationID: number,
    category: string,
  ) => {
    const categoryGroupIndex = selectedCategories.findIndex(
      (c) => c.id === classificationID,
    );
    const copiedCategoryGroups = [...selectedCategories];

    // If there is no group for this category then add a new group,
    // and otherwise update the category list of this group.
    if (categoryGroupIndex === -1) {
      copiedCategoryGroups.push({
        id: classificationID,
        categories: [category],
      });
    } else {
      const copiedSelectedCategories = [
        ...selectedCategories[categoryGroupIndex].categories,
      ];
      const categoryIndex = copiedSelectedCategories.indexOf(category);
      if (categoryIndex === -1) {
        copiedSelectedCategories.push(category);
      } else {
        copiedSelectedCategories.splice(categoryIndex, 1);
      }
      copiedCategoryGroups.splice(categoryGroupIndex, 1, {
        id: classificationID,
        categories: copiedSelectedCategories,
      });
    }

    onSelectedCategoriesChange(copiedCategoryGroups);
  };

  interface ShowMoreButtonProps {
    /**
     * The ID of a Classification.
     */
    classificationID: number;

    /**
     * Whether the section of a Classification has been expanded or not.
     */
    expanded: boolean;
  }

  /**
   * Create a button to show more/less categories for each Classification.
   */
  const ShowMoreButton = ({
    classificationID,
    expanded,
  }: ShowMoreButtonProps) => (
    <ListItem>
      <Grid container justifyContent="center">
        <Grid>
          <Button
            variant="text"
            onClick={() => onShowMore(classificationID, !expanded)}
            color="inherit"
          >
            {expanded
              ? languageStrings.common.action.showLess
              : languageStrings.common.action.showMore}
          </Button>
        </Grid>
      </Grid>
    </ListItem>
  );

  /**
   * Generate texts in the format of 'term (count)' for displaying a Category.
   * @param category The text of a category
   * @param count The count of a category
   */
  const CategoryLabel = ({ term: category, count }: OEQ.SearchFacets.Facet) => (
    <>
      <Typography display="inline">{category}</Typography>
      <Typography
        color="textSecondary"
        display="inline"
        className={classes.categoryListItemCount}
      >
        {`(${count})`}
      </Typography>
    </>
  );

  interface CategoryListItemProps {
    /**
     * The ID of a Classification
     */
    classificationID: number;
    /**
     * A category to be displayed
     */
    category: OEQ.SearchFacets.Facet;
  }

  /**
   * Build a ListItem consisting of a MUI Checkbox and a Label for a category.
   */
  const CategoryListItem = ({
    classificationID,
    category,
  }: CategoryListItemProps) => {
    const { term } = category;
    return (
      <ListItem disableGutters className={classes.classificationListItem}>
        <FormControlLabel
          style={{ alignItems: "flex-start" }}
          control={
            <Checkbox
              style={{ paddingTop: 0, paddingBottom: 0 }}
              checked={
                selectedCategories
                  ?.find((c) => c.id === classificationID)
                  ?.categories?.includes(term) ?? false
              }
              onChange={() => handleSelectCategories(classificationID, term)}
              TouchRippleProps={{
                classes: { root: classes.categoryListCheckbox },
              }}
            />
          }
          label={<CategoryLabel {...category} />}
        />
      </ListItem>
    );
  };

  /**
   * A category list of one Classification should include all categories that are
   * applicable to current search criteria as well as categories that are not
   * applicable but have been selected.
   *
   * The order is selectedApplicable -> selectedNotApplicable -> notSelected.
   *
   * @param id The ID of a Classification
   * @param categories A list of categories to be ordered.
   */
  const getOrderedCategories = ({
    id,
    categories,
  }: Classification): OEQ.SearchFacets.Facet[] => {
    const selectedTerms: string[] =
      selectedCategories?.find((c) => c.id === id)?.categories ?? [];

    // Generate two arrays for previously selected categories. One for applicable categories
    // and the other one for non-applicable categories.
    const [selectedApplicable, selectedNotApplicable] = selectedTerms.reduce<
      [OEQ.SearchFacets.Facet[], OEQ.SearchFacets.Facet[]]
    >(
      ([applicable, notApplicable], term) => {
        const applicableCategory = categories.find((c) => c.term === term);
        return applicableCategory
          ? [applicable.concat(applicableCategory), notApplicable]
          : [applicable, notApplicable.concat({ term: term, count: 0 })];
      },
      [[], []],
    );

    // Categories that apply to current search criteria but have not been selected.
    const notSelected = categories.filter(
      (c) => !selectedTerms.includes(c.term),
    );

    return [...selectedApplicable, ...selectedNotApplicable, ...notSelected];
  };

  interface ListCategoryProps {
    /**
     * Classification to build from
     */
    classification: Classification;
    /**
     * Whether to show more categories or not
     */
    expanded: boolean;
  }

  /**
   * Build a list for a Classification's categories. Some categories may have facets
   * not displayed due to the configured maximum display number.
   */
  const ListCategories = ({
    classification: { id, categories, maxDisplay },
    expanded,
  }: ListCategoryProps) => (
    <>
      {categories.slice(0, expanded ? undefined : maxDisplay).map((facet) => (
        <CategoryListItem
          classificationID={id}
          category={facet}
          key={`${id}:${facet.term}`}
        />
      ))}
    </>
  );

  /**
   * Sort and build Classifications that have categories.
   * For each Classification, a scroll bar and a 'Show more' button may or may not
   * be added, depending on whether a classification has more categories to show or not.
   */
  const buildClassifications = classifications
    .filter((classification) => classification.categories.length > 0)
    .sort(
      (prevClassification, nextClassification) =>
        prevClassification.orderIndex - nextClassification.orderIndex,
    )
    .map((classification) => {
      const { id, name, maxDisplay } = classification;
      const expanded = expandedClassifications.get(id) ?? false;
      const orderedCategories = getOrderedCategories(classification);
      return (
        <ListItem divider key={id}>
          <Grid container direction="column">
            <Grid>
              <Typography variant="subtitle1" style={{ fontWeight: 500 }}>
                {name}
              </Typography>
            </Grid>
            <Grid>
              <StyledList
                className={expanded ? classes.classificationList : ""}
              >
                <ListCategories
                  classification={{
                    ...classification,
                    categories: orderedCategories,
                  }}
                  expanded={expanded}
                />
                {orderedCategories.length > maxDisplay && (
                  <ShowMoreButton classificationID={id} expanded={expanded} />
                )}
              </StyledList>
            </Grid>
          </Grid>
        </ListItem>
      );
    });

  return <List>{buildClassifications}</List>;
};
