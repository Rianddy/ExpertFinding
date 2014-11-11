package com.ExpertFinding.dao;

import com.ExpertFinding.model.users;
public interface UserDAO {
	users findUsersByNodeId(String nodeId);
}
