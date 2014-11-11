package com.ExpertFinding.action;

import java.util.List;
import com.ExpertFinding.model.users;
import com.ExpertFinding.service.UserService;
import common.GetExpertidsFromIndex;
public class GetAllExpertsAction extends BasicAction{
	private List<users> users;
	private String query;
	public List<users> getUsers() {
		return users;
	}
	public void setUsers(List<users> users) {
		this.users = users;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	@Override
	public String execute() throws Exception {
		try {
			GetExpertidsFromIndex getJobids=new GetExpertidsFromIndex(query);
			List<String> jobids=getJobids.Search();
			UserService userService = serviceManager.getUserService();
			users=userService.findUsersByNodeId(jobids);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return SUCCESS;
	}
}
