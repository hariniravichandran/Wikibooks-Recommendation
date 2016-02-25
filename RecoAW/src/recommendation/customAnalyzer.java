package recommendation;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.hunspell.*;
import org.tartarus.snowball.ext.PorterStemmer;
//import org.apache.lucene.analysis.PorterStemFilter;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class customAnalyzer extends Analyzer {	
	public static String mainDir = "/Users/hariniravichandran/Documents/"
			+ "AWAssign2/";
	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		// TODO Auto-generated method stub
		Tokenizer source = new StandardTokenizer();              
		TokenStream filter = new LowerCaseFilter(source);
		filter = new PorterStemFilter(filter);
		return new TokenStreamComponents(source, filter);
	}

	private static void indexDirectory(IndexWriter writer, File dir) 
			throws IOException {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				indexDirectory(writer, f); // recurse
			} else if (f.getName().endsWith(".txt")) {
				// call indexFile to add the title of the txt file to 
				// your index (you can also index html)
				indexFile(writer, f);
			}
		}
	}

	private static void indexFile(IndexWriter writer, File f) 
			throws IOException {
		System.out.println("Indexing " + f.getName());
		Document doc = new Document();
		doc.add(new TextField("filename", f.getPath(), TextField.Store.YES));


		//open each file to index the content
		try{			
			FileInputStream is = new FileInputStream(f);
			BufferedReader reader = new BufferedReader
					(new InputStreamReader(is));
			StringBuffer stringBuffer = new StringBuffer();
			String line = null;
			while((line = reader.readLine())!=null){
				stringBuffer.append(line).append("\n");
			}
			reader.close();
			doc.add(new TextField("contents", stringBuffer.toString(), 
					TextField.Store.YES));

		}catch (Exception e) {

			System.out.println("something wrong with indexing content "
					+ "of the files");
		}    
		writer.addDocument(doc);

	}
	private static void writeToFile(String fileName, String text) throws IOException {
		File dest = new File(mainDir);
		//dest.mkdirs();
		String fullPath = dest + "/" + fileName;
		File file = new File(fullPath);
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		fw.write(text);
		fw.close();
	}
	
	public static void main(String[] args) 
			throws IOException, ParseException {

		File dataDir = new File(mainDir + "WikiData/");
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new IOException(
					dataDir + " does not exist or is not a directory");
		}

		Directory indexDir = new RAMDirectory();
		customAnalyzer analyzer = new customAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(indexDir, config);

		indexDirectory(writer, dataDir);
		writer.close();
		
		//Read input posts from file.
		File file = new File(mainDir + "InputPosts.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		int postCount = 0;
		HashMap<String, ArrayList<String>> recoMap = 
				new HashMap<String, ArrayList<String>>();
		while ((line = br.readLine()) != null) {
			//System.out.println(line);
			postCount++;
			String querystr = line;
			//Parse every query.
			Query q = new QueryParser("contents", analyzer).parse(querystr);
			int hitsPerPage = 10;
			IndexReader reader = null;	

			TopScoreDocCollector collector = null;
			IndexSearcher searcher = null;
			reader = DirectoryReader.open(indexDir);
			searcher = new IndexSearcher(reader);
			
			//Set BM25 similarity for the searcher.
			searcher.setSimilarity(new BM25Similarity());
			collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);

			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			System.out.println();
			
			ArrayList<String> recoArray = new ArrayList<String>();
			String key = "Post " + Integer.toString(postCount);
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d;
				d = searcher.doc(docId);			
				System.out.println((i + 1) + ". " + d.get("filename"));
				String recommendation = d.get("contents");
				if (recommendation.startsWith("Code") 
						|| recommendation.startsWith("Test") 
						|| recommendation.startsWith("COM_DATA")
						|| recommendation.startsWith("ComServer")
						|| recommendation.startsWith("public"))
					recommendation = "<pre><code>"
							+ "<span style='background-color':'#F2A4A4'>" 
							+ recommendation 
							+ "</span></code></pre>";
				recoArray.add(recommendation);
				
//				String destination = d.get("filename");
//				int index = destination.lastIndexOf("/");
//				destination = destination.substring(index+1);
//				writeToFile(destination, recommendation);
//				System.out.println(d.get("contents"));
			}
			//System.out.println(key + recoArray.toString());
			recoMap.put(key, recoArray);
			reader.close();
		}
		//System.out.println(recoMap.toString());
		Gson gson = new Gson();
		String json = gson.toJson(recoMap);
		//System.out.println(json);
		writeToFile("recoOutput.json", json);
		
	}

}
