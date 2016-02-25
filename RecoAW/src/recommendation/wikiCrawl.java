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
	public static String location = "<project_main_folder_path>";
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
			if (!(topic.equals("Index") || topic.equals("Glossary") 
					|| topic.equals("Links") 
					|| topic.equals("Libraries, extensions and frameworks") 
					|| topic.equals("Understanding gradients")
					|| topic.equals("About this book")
					|| topic.equals("History of Java")
					|| topic.equals("Overview of the Java programming language")
					|| topic.equals("The Java platform (JRE & JDK)")
					|| topic.equals("Installing Java on Your Computer")
					|| topic.equals("Compiling programs")
					|| topic.equals("Running Java programs")
					|| topic.equals("Understanding a Java program")
					|| topic.equals("Java IDEs")
					|| topic.equals("Compiling programs")))
				mainLinks.put(topic, link);
		}
	}
	private static void writeToFile(String title, HashMap<String, String> map) 
			throws IOException {
		File dir = new File(location + title);
		dir.mkdirs();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String filename = entry.getKey();
			String content = entry.getValue();
			String fullPath = dir.getAbsolutePath() + "/" + filename;
			File file = new File(fullPath + ".txt");
			file.createNewFile();
			System.out.print("Writing file: "+ fullPath +".txt\n");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			fw.write(content);
			fw.close();
		}
	}

	private static void parsePage(String url) throws IOException {
		HashMap<String, String> pageContent = 
				new HashMap<String, String>();
		
		Document doc = Jsoup.connect(url).get();
		Element wrapper = doc.getElementById("mw-content-text");
		Elements e = wrapper.getElementsByClass("noprint").remove();
		e = wrapper.getElementsByClass("wikitable");
		if (e.size() > 0)
			wrapper.getElementsByClass("wikitable").first().remove();
		e = wrapper.getElementsByClass("collapsible").remove();
		e = wrapper.getElementsByClass("mw-editsection").remove();
		e = wrapper.select("a:contains(Edit)").remove();
		e = wrapper.getElementsByClass("metadata topicon").remove();
		e = wrapper.getElementsByTag("noscript").remove();
		e = wrapper.getElementsByTag("script").remove();

		Elements allElements = wrapper.getAllElements();
		Elements temp = allElements;
		
		String heading = "";
		if (allElements.select("h2").size() > 0) 
			heading = "h2";
		else if (allElements.select("h3").size() > 0)
			heading = "h3";
		else
			heading = "null";

		String text = "";
		String key = "Introduction";
		if (!heading.equals("null")) {
			Elements elem = allElements.select(heading);
			int c = 0;
			Element i = allElements.get(0).child(0);
			while (i != null && !(i.tagName().equals(heading))) {
				//text += i.text();
				pageContent.put(key+"_"+c, i.text());
				c++;
				i = i.nextElementSibling();
			}
			System.out.println(key + "\n");
			pageContent.put(key, text);
			if (i != null) {
				for (Element el : elem) {
					int ctr = 0;
					text = "";
					key = el.text().replace("/", "-");
					i = i.nextElementSibling();
					if (i == null)
						break;
					while (!(i.tagName().equals(heading))) {
						text = i.text();
						pageContent.put(key+"_"+ctr, text);
						ctr++;
						if (i.nextElementSibling() == null) {
							break;
						}
						i = i.nextElementSibling();
					}
					System.out.print(key + "\n");
				}
			}
		} else {
			key = "Introduction";
			Elements paras = wrapper.select("p");
			int count = 0;
			for (Element p: paras) {
				text = p.text();
				pageContent.put(key+"_"+count, text);
				count++;
			}
		}
		int last = url.lastIndexOf("/");
		String topic = url.substring(last+1);
		writeToFile(topic, pageContent);
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String url = "https://en.wikibooks.org/wiki/Java_Programming";
		parseMainPage(url);
		
		for (String link : mainLinks.values()) {
			System.out.print("URL: " + link + "\n");
			parsePage(link);
		}

	} 	 	

}

