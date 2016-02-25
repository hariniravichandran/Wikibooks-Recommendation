# Wikibooks-Recommendation
Indexing the content of Java Programming wikibooks and recommending resources to Stack Overflow posts based on similarity of the content.

To view the web app:
	If you're using Firefox, index.html file is the direct entry point.

	If using Chrome,
	1. In terminal, go to the folder where all the project files are present. 
	2. Run this command: python -m SimpleHTTPServer <port_number>
	3. Open Chrome and go to https://localhost:<portnumber>



To re-execute the whole project from scratch:
	1. Four jar files from the Lucene package have to be added to the classpath.
		i) lucene-demo-5.4.1.jar
		ii) lucene-core-5.4.1.jar
		iii) lucene-queryparser-5.4.1.jar
		iv) lucene-analyzers-common-5.4.1.jar

	2. To use Jsoup, add the jar file to classpath, or include the following Maven dependency in pom.xml:
		<dependency>
	  		<groupId>org.jsoup</groupId>
	  		<artifactId>jsoup</artifactId>
  		<version>1.8.3</version>
  	</dependency>
	3. Change the value of the variable "location" in src/recommendation/wikiCrawl.java to your project location.
	4. Run wikiCrawl.java.
	5. Change "mainDir" variable to your project main folder path in src/recommendation/oracleCrawl.java. Run oracleCrawl.java.
	6. Change "mainDir" variable to your project main folder path in src/recommendation/customAnalyzer.java. Make sure InputPosts.txt is present in this path.
	7. Run customAnalyzer.java.
	8. The output file recoOutput.json should now be generated in the project main folder path.
	9. Run the python server as mentioned above and go to https://localhost:<port_number> to view the web app.
