package com.tle.integration.blackboard.gbfixer;

import java.util.List;

import blackboard.persist.Id;
import blackboard.persist.impl.SimpleSelectQuery;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.impl.GradableItemDAO;
import blackboard.platform.query.Criteria;
import blackboard.platform.query.CriterionBuilder;

/**
 * @author Aaron
 *
 */
public class EqGradeableItemDAO extends GradableItemDAO
{
	public List<GradableItem> getForContentId(Id contentId)
	{
		SimpleSelectQuery q = new SimpleSelectQuery(getMap());
		Criteria c = q.getCriteria();
		CriterionBuilder cb = c.createBuilder(new String[0]);
		c.add(cb.equal("courseContentId", contentId));
		c.add(cb.notEqual("deleted", Boolean.valueOf(true)));
		return getDAOSupport().loadList(q);
	}
}
