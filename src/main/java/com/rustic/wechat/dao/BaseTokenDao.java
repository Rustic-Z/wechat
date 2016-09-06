package com.rustic.wechat.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.rustic.wechat.entity.BaseToken;
import com.rustic.wechat.util.Constants;

@Repository
public class BaseTokenDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public BaseToken findUndeletedToken() {
		String hql = "FROM BaseToken WHERE deleted=:deleted ORDER BY id desc";
		Query q = this.sessionFactory.getCurrentSession().createQuery(hql);
		q.setParameter("deleted", Constants.DELETED_NO);
		List<BaseToken> retList = q.list();
		if (retList.isEmpty()) {
			return null;
		} else {
			return retList.get(0);
		}
	}
	
	public void saveOrUpdate(BaseToken token) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(token);
	}

}
