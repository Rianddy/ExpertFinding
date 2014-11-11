package com.ExpertFinding.dao;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.ExpertFinding.model.users;

import java.util.List;
public class UserDAOImpl extends DAOSupport implements UserDAO{
	public UserDAOImpl(HibernateTemplate template){
		super(template);
	}
	@Override
	public users findUsersByNodeId(String nodeId){
		String hql="from Jobs where nodeId = ?";
		List<users> users=template.find(hql,nodeId);
		return users.get(0);
	}
}
