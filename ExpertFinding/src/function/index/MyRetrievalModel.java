package function.index;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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
import java.lang.Math;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import edu.pitt.sis.iris.squirrel.analysis.TextAnalyzer;
import function.index.indexReader;

public class MyRetrievalModel {

	protected indexReader ixreader;
	private int u = 20;
	private TokenStream ts;
	private Analyzer analyzer;
	public MyRetrievalModel() {
		// you should implement this method
		try {
			// get the stopwords inputstream
			analyzer=TextAnalyzer.get( "lc", "std tk", "indri stop", "nostem" );
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public MyRetrievalModel setIndex(indexReader ixreader) {
		this.ixreader = ixreader;
		return this;
	}

	/**
	 * Search for the topic information. The returned results should be ranked
	 * by the score (from the most relevant to the least). max_return specifies
	 * the maximum number of results to be returned.
	 * 
	 * @param topic
	 *            The topic information to be searched for.
	 * @param max_return
	 *            The maximum number of returned document
	 * @return
	 */
	public Map<String,Double> search(String query, int max_return)
			throws IOException {
		// you should implement this method
		String[] queryTerms = query.split(" ");
		// store all the terms except current term
		ArrayList<String> allTermsConsidered = new ArrayList<String>();
		// store all documents except current documents
		Set<Integer> allDocumentsConsidered = new HashSet<Integer>();
		// store all the documents and their probability
		Map<Integer, Double> documentProbability = new TreeMap<Integer, Double>();
		for (String term : queryTerms) {
			Reader reader=new StringReader(term);
			ts = analyzer.tokenStream("myfield", reader);
			CharTermAttribute charTermAttribute = ts.getAttribute(CharTermAttribute.class);
			ts.reset();
			while (ts.incrementToken()) {
				String queryTerm = charTermAttribute.toString();
				// get postinglist of this term
				int[][] postingList = ixreader.getPostingList(queryTerm);
				if (postingList != null) {
					Set<Integer> currentTermDocuments = new HashSet<Integer>();
					for (int i = 0; i < postingList.length; i++) {
						double beforeProbability;
						int docid = postingList[i][0];
						currentTermDocuments.add(docid);
						if (allDocumentsConsidered.contains(docid)) {
							// if the previous documents contain this
							// document
							// then calculate the normal probability
							beforeProbability = documentProbability
									.get(docid);
						} else {
							// if the previous documents don't contain this
							// document
							// calculate the previous terms' smoothing
							// probabilities
							beforeProbability = 1;
							for (String beforeTerm : allTermsConsidered) {
								beforeProbability *= calculateSmoothingUnseenTerm(
										beforeTerm, docid);
							}
						}
						// get current term probability in current document
						double currentTermProbability = CalculateProbability(
								queryTerm, docid, postingList[i][1]);
						documentProbability.put(docid, beforeProbability
								* currentTermProbability);
					}
					for (Integer docid : allDocumentsConsidered) {
						// if there is any document before that does not
						// exist in current term document
						// calculate the smoothing probability of current
						// term
						if (!currentTermDocuments.contains(docid)) {
							double currentTermSmoothProbability = calculateSmoothingUnseenTerm(
									queryTerm, docid);
							double beforeProbability = documentProbability
									.get(docid);
							documentProbability.put(docid,
									beforeProbability
											* currentTermSmoothProbability);
						}
					}
					allTermsConsidered.add(queryTerm);
					allDocumentsConsidered.addAll(currentTermDocuments);
				}
			}
		}
		Map<String, Double> result=new HashMap<String,Double>();
		Map<Integer, Double> sortedMap = sortByComparator(documentProbability);
		int index=0;
		for (Map.Entry entry : sortedMap.entrySet()) {
			// create SearchResult by sort and add them to list
			if (index == max_return)
				break;
			else {
				result.put(ixreader.getDocno((Integer) entry.getKey()), (Math.log10((Double) entry.getValue())));
			}
			index++;
		}
		return result;
	}

	private static Map sortByComparator(Map unsortMap) {
		// Sort map by value
		// these codes come from internet
		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// put sorted list into map again
		// LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public double CalculateProbability(String queryTerm, Integer docid,
			int countTermInDocument) {
		// Calculate the normal probability
		try {
			double score = 0;
			double smoothingDenominator = (double) u
					+ (double) ixreader.docLength(docid);
			double smoothingMolecular = countTermInDocument
					+ (double) u * (double) ixreader.CollectionFreq(queryTerm)
					/ ixreader.getCollectionLength();
			score = smoothingMolecular / smoothingDenominator;
			return score;
		} catch (Exception e) {
			System.out.println(e);
			return 0;
		}
	}

	public double calculateSmoothingUnseenTerm(String term, int docid) {
		// Calculate the smoothing probability
		try {
			double smoothingDenominator = (double) u
					+ (double) ixreader.docLength(docid);
			double smoothingMolecular = ((double) u * (double) (ixreader
					.CollectionFreq(term)))
					/ (ixreader.getCollectionLength());
			return smoothingMolecular / smoothingDenominator;
		} catch (Exception e) {
			System.out.println(e);
		}
		return 0;
	}
}
