package function.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import common.Constants;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.pitt.sis.iris.squirrel.utils.FieldFilter;
import edu.pitt.sis.iris.squirrel.utils.IndexUtils;

public class indexReader {

	private FieldSelector fs_docno;

	protected File dir;
	private Directory directory;
	private IndexReader ixreader;
	private String type;
	private int collectionLength;
	private String indexType;

	public indexReader(String fieldtype, String indexType) throws IOException {
		fs_docno = FieldFilter.byName(indexType);
		this.indexType = indexType;
		this.dir = new File(Constants.IndexDirectoryPath);
		this.directory = FSDirectory.open(dir);
		this.ixreader = IndexReader.open(directory);
		this.type = fieldtype;
		BufferedReader cfReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(Constants.IndexDirectoryPath
						+ Constants.CollectionFrequencyPath)));
		collectionLength=Integer.valueOf(cfReader.readLine());
		cfReader.close();
	}

	public int getDocid(String docno) throws IOException {
		return IndexUtils.find(ixreader, indexType, docno);
	}

	public String getDocno(int docid) throws IOException {
		Document doc = ixreader.document(docid, fs_docno);
		return (doc == null) ? null : doc.get(indexType);
	}

	public int[][] getPostingList(String token) throws IOException {
		Term tm = new Term(type, token);
		int df = ixreader.docFreq(tm);
		int[][] posting = new int[df][];
		TermDocs docs = ixreader.termDocs(tm);
		if (docs != null) {
			int ix = 0;
			while (docs.next()) {
				int id = docs.doc();
				int freq = docs.freq();
				posting[ix] = new int[] { id, freq };
				ix++;
			}
		}
		return posting;
	}

	public int DocFreq(String token) throws IOException {
		Term tm = new Term(type, token);
		int df = ixreader.docFreq(tm);
		return df;
	}

	public long CollectionFreq(String token) throws IOException {
		Term tm = new Term(type, token);
		long ctf = 0;
		TermDocs docs = ixreader.termDocs(tm);
		if (docs != null) {
			while (docs.next()) {
				ctf = ctf + docs.freq();
			}
		}
		return ctf;
	}

	public int docLength(int docid) throws IOException {
		TermFreqVector vector = ixreader.getTermFreqVector(docid, type);
		int doc_length = 0;
		if (vector != null) {
			for (int freq : vector.getTermFrequencies()) {
				doc_length = doc_length + freq;
			}
			return doc_length;
		} else
			return 0;

	}

	public int getCollectionLength() {
		return collectionLength;
	}

	public void setCollectionLength(int collectionLength) {
		this.collectionLength = collectionLength;
	}

	public String getNodeId(int docid) throws IOException {
		TermFreqVector vector = ixreader.getTermFreqVector(docid, Constants.NodeId);
		if(vector.getTerms().length>0)
			return vector.getTerms()[0];
		else
			return null;
	}

	public void close() throws IOException {
		ixreader.close();
		directory.close();
	}
}
