package function.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;

import edu.pitt.sis.iris.squirrel.analysis.TextAnalyzer;

import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import common.Constants;

public class indexWritter {
	private Directory directory;
	private IndexWriter ixwriter;
	TokenStream tokenStream;
	ConnectingToMysql connecting;
	Connection connection;
	String outPutPathDirectory = Constants.IndexDirectoryPath;
	static private String tableName = "users";
	ResultSet rs;
	Analyzer analyzer;
	int tweetId = 0;
	private int collectionFrequency = 0;

	public indexWritter() {
		try {
			connecting = new ConnectingToMysql();
			connection = connecting.Get_Connection();
			directory = FSDirectory
					.open(new File(Constants.IndexDirectoryPath));
			analyzer = new StandardAnalyzer(Version.LUCENE_36);
			ixwriter = new IndexWriter(directory, new IndexWriterConfig(
					Version.LUCENE_36, TextAnalyzer.get("lc", "std tk",
							"indri stop", "nostem")));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void TokenizeAll() {

		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT nodeId,timeline,description FROM "
							+ tableName);
			rs = ps.executeQuery();
			while (rs.next()) {
				String nodeId = rs.getString(1);
				String timeline = rs.getString(2);
				String description = rs.getString(3);
				String[] timelineArray = timeline.split("\\$\\$");
				for (String tweet : timelineArray) {
					Index2(Constants.TweetId, String.valueOf(tweetId),
							Constants.Tweet, tweet, Constants.NodeId, nodeId);
					collectionFrequency += tweet.length();
					tweetId++;
				}
				Index(Constants.NodeId, nodeId, Constants.Description,
						description);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void Index2(String indexType, String index, String type, String s,
			String type1, String s1) throws IOException {
		Document doc = new Document();
		doc.add(new Field(indexType, index, Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field(type, s, Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.YES));
		doc.add(new Field(type1, s1, Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.YES));
		ixwriter.addDocument(doc);
	}

	public void Index(String indexType, String index, String type, String s)
			throws IOException {
		Document doc = new Document();
		doc.add(new Field(indexType, index, Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field(type, s, Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.YES));
		ixwriter.addDocument(doc);
	}

	public void close() throws Exception {
		BufferedWriter outputCF = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Constants.IndexDirectoryPath
						+ Constants.CollectionFrequencyPath), "UTF-8"));
		outputCF.write(String.valueOf(collectionFrequency));
		outputCF.close();
		ixwriter.close();
		directory.close();
	}

	public static void main(String[] args) {
		try {
			indexWritter index = new indexWritter();
			index.TokenizeAll();
			index.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
