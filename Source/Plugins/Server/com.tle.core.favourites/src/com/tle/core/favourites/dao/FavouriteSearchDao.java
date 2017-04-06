package com.tle.core.favourites.dao;

import java.util.Date;
import java.util.List;

import com.tle.beans.Institution;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

public interface FavouriteSearchDao extends GenericInstitutionalDao<FavouriteSearch, Long>
{
	List<FavouriteSearch> search(String freetext, Date[] dates, int offset, int perPage, String order, boolean reverse,
		String userId, Institution institution);

	long count(String freetext, Date[] dates, String userId, Institution institution);

	void deleteAll();

	FavouriteSearch getById(long id);
}
