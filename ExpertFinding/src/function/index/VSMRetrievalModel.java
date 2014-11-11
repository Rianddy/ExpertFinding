package function.index;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.Constants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.pitt.sis.iris.squirrel.analysis.TextAnalyzer;
import edu.pitt.sis.iris.squirrel.utils.FieldFilter;

public class VSMRetrievalModel {
	private FieldSelector fs_docno;
	private final Set<String> termsSet = new HashSet<String>();
	private final Set<Integer> documentSet = new HashSet<Integer>();
	private Directory directory;
	private IndexReader ixreader;
	private String type;
	private String indexType;
	private TokenStream ts;
	private Analyzer analyzer;
	private Map<String, Double> f2;
	private Map<String, Double> documentScore=new TreeMap<String,Double>();
	protected File dir;
	public VSMRetrievalModel(String fieldtype, String indexType, String query) throws Exception{
		analyzer=TextAnalyzer.get( "lc", "std tk", "indri stop", "nostem" );
		this.type = fieldtype;
		this.indexType = indexType;
		fs_docno = FieldFilter.byName(indexType);
		this.dir = new File(Constants.IndexDirectoryPath);
		this.directory = FSDirectory.open(dir);
		this.ixreader = IndexReader.open(directory);
        f2 = getWeights(ixreader, query);
	}
	public Map<String, Double> getCosineSimilarity() throws Exception{
		for (int docId:documentSet){
			Map<String, Double> f1 = getWeights(ixreader, docId);
			String nodeId=getNodeId(docId);
			if(documentScore.containsKey(nodeId))
				documentScore.put(nodeId, documentScore.get(nodeId)+cosine_similarity(f1,f2));
			else
				documentScore.put(nodeId, cosine_similarity(f1,f2));
		}
		return sortByComparator(documentScore);
    }
	private static Map sortByComparator(Map unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	public double cosine_similarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> both = new HashSet<String>(v1.keySet());
        both.retainAll(v2.keySet());
        double sclar = 0, norm1 = 0, norm2 = 0;
        for (String k : both) sclar += v1.get(k) * v2.get(k);
        for (String k : v1.keySet()) norm1 += v1.get(k) * v1.get(k);
        for (String k : v2.keySet()) norm2 += v2.get(k) * v2.get(k);
        return sclar / Math.sqrt(norm1 * norm2);
	}
	public Map<String, Double> getWeights(IndexReader reader, String query)
            throws IOException {
		String[] queryTerms = query.split(" ");
		Map<String, Integer> docFrequencies = new HashMap<String, Integer>();
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		Map<String, Double> tf_Idf_Weights = new HashMap<String, Double>();
		for (String term : queryTerms) {
			Reader stringreader=new StringReader(term);
			ts = analyzer.tokenStream("myfield", stringreader);
			CharTermAttribute charTermAttribute = ts.getAttribute(CharTermAttribute.class);
			ts.reset();
			while (ts.incrementToken()) {
				String queryTerm = charTermAttribute.toString();
				if(frequencies.containsKey(queryTerm)){
					frequencies.put(queryTerm, frequencies.get(queryTerm)+1);
				}else{
					frequencies.put(queryTerm, 1);
					termsSet.add(queryTerm);
					System.out.println(queryTerm);
		            docFrequencies.put(queryTerm, ixreader.docFreq( new Term( type, queryTerm ) ));
		            TermDocs docs = ixreader.termDocs(new Term( type, queryTerm ));
		            while (docs.next()) {
						int id = docs.doc();
						documentSet.add(id);
					}
				}
			}
		}
		for ( String term : frequencies.keySet() ) {
        	int df = docFrequencies.get(term);
            int tf = frequencies.get(term);
            double idf = ( 1 + Math.log(ixreader.numDocs()) - Math.log(df) );
            double w = tf * idf;
            tf_Idf_Weights.put(term, w);
        }
        return tf_Idf_Weights;
    }

	
	Map<String, Double> getWeights(IndexReader reader, int docId)
            throws IOException {
		TermFreqVector vector = ixreader.getTermFreqVector(docId, type);
        String[] terms=vector.getTerms();
        int[] termsFrequency=vector.getTermFrequencies();
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        Map<String, Integer> docFrequencies = new HashMap<String, Integer>();
        Map<String, Double> tf_Idf_Weights = new HashMap<String, Double>();
        for(int i=0;i<terms.length;i++) {
            frequencies.put(terms[i], termsFrequency[i]);
            termsSet.add(terms[i]);
            docFrequencies.put(terms[i], ixreader.docFreq( new Term( type, terms[i] ) ));
        }
        
        for ( String term : docFrequencies.keySet() ) {
        	Term tm = new Term(type, term);
    		int df = ixreader.docFreq(tm);
            int tf = frequencies.get(term);
            double idf = ( 1 + Math.log(ixreader.numDocs()) - Math.log(df) );
            double w = tf * idf;
            tf_Idf_Weights.put(term, w);
        }
        return tf_Idf_Weights;
    }
	
	public String getDocno(int docid) throws IOException {
		Document doc = ixreader.document(docid, fs_docno);
		return (doc == null) ? null : doc.get(indexType);
	}
	
	public String getNodeId(int docid) throws IOException {
		TermFreqVector vector = ixreader.getTermFreqVector(docid, Constants.NodeId);
		if(vector.getTerms().length>0)
			return vector.getTerms()[0];
		else
			return null;
	}
}
