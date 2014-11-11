package function.index;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import common.Constants;
public class MyOwnScoreQuery extends CustomScoreQuery {

	private Query query;

	public MyOwnScoreQuery(Query query) {
		super(query);
		this.query = query;
	}

	@Override
	public CustomScoreProvider getCustomScoreProvider(final IndexReader reader) {
		return new CustomScoreProvider(reader) {
			@Override
			public float customScore(int doc, float subQueryScore,
					float valSrcScore) throws IOException {
				TermFreqVector freqVector = reader.getTermFreqVector(doc, Constants.Timeline);
	            int freqs[] = freqVector.getTermFrequencies();
	            Set<Term> terms = new HashSet<Term>();
	            query.extractTerms(terms);
	            int total = 0;
	            for (Term term : terms) {
	                int index = freqVector.indexOf(term.text());
	                if (index != -1) {
	                    total += freqs[index];
	                }
	            }
	            return total;
			}
		};
	}
}
