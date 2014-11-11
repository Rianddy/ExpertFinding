package com.ExpertFinding.service;
import java.util.List;

import com.ExpertFinding.model.users;
public interface UserService {
	public List<users> findUsersByNodeId(List<String> nodeIds) throws Exception;
}
