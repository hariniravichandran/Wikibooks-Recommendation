package recommendation;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import java.util.*;
public class wikiCrawl {
	public static HashMap<String, String> mainLinks = 
			new HashMap<String, String>();
	public static HashMap<String, HashMap<String, String>> content = 
			new HashMap<String, HashMap<String, String>>();
	private static void parseMainPage(String url) 
			throws IOException {
		
		Document doc = Jsoup.connect(url).get();
		Elements listItems = doc.select("#mw-content-text > ul > li");
		for (Element li : listItems) {
			String link = li.select("a[href]").last().attr("abs:href");
			String topic = li.select("a[href]").last().text();
			mainLinks.put(topic, link);
		}
	}
	private static void writeToFile(String title, String body) 
			throws IOException {
		File file = new File("data/" + title + ".txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		System.out.print("Writing file: "+title+".txt\n");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(body);
		bw.close();
	}
	private static void parsePage(String url) throws IOException {
		HashMap<String, String> pageContent = 
				new HashMap<String, String>();
		
		Document doc = Jsoup.connect(url).get();
		Element wrapper = doc.getElementById("mw-content-text");
		Elements e = wrapper.getElementsByClass("noprint").remove();
		e = wrapper.getElementsByClass("wikitable").remove();
		e = wrapper.getElementsByClass("collapsible").remove();
		e = wrapper.getElementsByClass("mw-editsection").remove();
		e = wrapper.select("a:contains(Edit)").remove();
		
		//String fullText = wrapper.text();

		Elements code = wrapper.getElementsByTag("table").remove();
		code.addAll(wrapper.getElementsByTag("pre").remove());
		String text = wrapper.text();
		pageContent.put("text", text);
		pageContent.put("code", code.text());
		
		int last = url.lastIndexOf("/");
		String topic = url.substring(last+1);
		content.put(topic, pageContent);
		//System.out.println(content.toString());
		System.out.print("Crawled: "+url+"\n");
		String body = text + "\n" + code.text();
		writeToFile(topic, body);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String url = "https://en.wikibooks.org/wiki/Java_Programming";
		parseMainPage(url);
		
		for (String link : mainLinks.values()) {
			System.out.print("URL: " + link + "\n");
			parsePage(link);
		}
		
		/*
		parsePage("https://en.wikibooks.org/wiki/Java_Programming/"
				+ "Preventing_NullPointerException");
		*/
	} 	 	

}

