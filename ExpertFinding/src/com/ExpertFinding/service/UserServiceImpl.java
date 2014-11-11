package com.ExpertFinding.service;

import java.util.ArrayList;
import java.util.List;

import com.ExpertFinding.dao.UserDAO;
import com.ExpertFinding.model.users;

public class UserServiceImpl implements UserService{
	private UserDAO userDAO;
	public UserServiceImpl(UserDAO userDAO){
		this.userDAO=userDAO;
	}
	@Override
	public List<users> findUsersByNodeId(List<String> nodeIds) throws Exception{
		List<users> users=new ArrayList<users>();
		for(int i=0;i<nodeIds.size();i++){
			users.add(userDAO.findUsersByNodeId(nodeIds.get(i)));
		}
		return users;
	}
}
