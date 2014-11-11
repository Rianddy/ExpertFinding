package common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import function.index.VSMRetrievalModel;

public class GetExpertidsFromIndex {
	private String query;
	public GetExpertidsFromIndex(String query) {
		this.query = query;
	}

	public List<String> Search() throws Exception {
		VSMRetrievalModel vsmRetrievalModel=new VSMRetrievalModel(Constants.Tweet,Constants.TweetId,query);
		Map<String, Double> temp=vsmRetrievalModel.getCosineSimilarity();
		int i=0;
		List<String> jobids=new ArrayList<String>();
		for (Map.Entry<String, Double> entry : temp.entrySet()) {
			jobids.add(entry.getKey());
			System.out.println(entry);
		}
		return jobids;
	}
}
