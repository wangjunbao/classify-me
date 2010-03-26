package classify;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Scanner;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class ClassifyMe {

	private static String appId = "Iu5udbvV34Fcg3uDwfJMTEY8Lb09.yMmFTaf7axWid3g4LmEN3G3iBUs6pa6jrRE";
	private String databaseURL;
	private double specificity;
	private int coverage;
	private Vector<Category> classification;
	private Vector<String> categoryPaths;
	private Category root;
	private int count;
	private int sampleSize = 4;
	private Hashtable<String, Integer> samples = new Hashtable<String, Integer>();

	/**
	 * Initialize the category tree
	 */
	public ClassifyMe() {

		root = new Category();
		root.name = "Root";
		root.specificity = 1;
		root.subcategories = new Vector<Category>();

		Category hardware = new Category();
		hardware.name = "Hardware";

		Category programming = new Category();
		programming.name = "Programming";

		Category computers = new Category();
		computers.name = "Computers";
		computers.subcategories = new Vector<Category>();
		computers.subcategories.add(hardware);
		computers.subcategories.add(programming);
		hardware.parent = computers;
		programming.parent = computers;
		computers.parent = root;
		root.subcategories.add(computers);

		Category fitness = new Category();
		fitness.name = "Fitness";

		Category diseases = new Category();
		diseases.name = "Diseases";

		Category health = new Category();
		health.name = "Health";
		health.subcategories = new Vector<Category>();
		health.subcategories.add(fitness);
		health.subcategories.add(diseases);
		fitness.parent = health;
		diseases.parent = health;
		health.parent = root;
		root.subcategories.add(health);

		Category basketball = new Category();
		basketball.name = "Basketball";

		Category soccer = new Category();
		soccer.name = "Soccer";

		Category sports = new Category();
		sports.name = "Sports";
		sports.subcategories = new Vector<Category>();
		sports.subcategories.add(basketball);
		sports.subcategories.add(soccer);
		basketball.parent = sports;
		soccer.parent = sports;
		sports.parent = root;
		root.subcategories.add(sports);

		classification = new Vector<Category>();
		categoryPaths = new Vector<String>();

		root.printTree();
	}

	/**
	 * return current classification path variable value
	 * 
	 * @return current classification path
	 */
	protected String getClassificationPaths() {
		String res = "";
		for (int i = 0; i < categoryPaths.size(); i++) {
			res = res + categoryPaths.get(i) + ", ";
		}
		if (res.length() >= 2)
			res = res.substring(0, res.length() - 2);
		return res;
	}

	/**
	 * Get the i^th Category path the database belongs to and make summary based
	 * on its classification
	 * 
	 * @param c
	 *            the Child class node that database belongs to
	 */
	protected void getCategoryPath(Category c, int i) {
		// if current node has parents, we will recursively call this function
		// to get the full path
		if (c.parent != null) {
			getCategoryPath(c.parent, i);
			categoryPaths.set(i, categoryPaths.get(i) + '/');
		}
		categoryPaths.set(i, categoryPaths.get(i) + c.name);

		// make summary on current category node
		// hashtable sum is used for counting words
		Hashtable<String, Integer> sum = new Hashtable<String, Integer>();
		// temporary set to store the string set returned from the lynx function
		Set<String> tempWords;
		String tempWord;
		Integer innerTempValue;
		String tempUrl;
		Iterator<String> innerIterator;

		// use tree set to sort words in alphabetical order
		Set<String> keys = new TreeSet<String>();
		// get sample urls of current category node
		Iterator<String> iterator = c.samples.keySet().iterator();
		while (iterator.hasNext()) {
			tempUrl = (String) (iterator.next());
			// make sure no duplicated url from different queries
			if (!samples.containsKey(tempUrl)) {
				samples.put(tempUrl, 1);
				System.out.println("Getting Page : " + tempUrl + "\n\n");
				tempWords = GetWordsLynx.runLynx(tempUrl);
				innerIterator = tempWords.iterator();
				// Calculate document frequency for each word
				while (innerIterator.hasNext()) {
					tempWord = innerIterator.next();
					if (sum.containsKey(tempWord)) {
						innerTempValue = (Integer) sum.get(tempWord);
						innerTempValue = innerTempValue + 1;
						sum.put(tempWord, innerTempValue);
					} else {
						sum.put(tempWord, 1);
						keys.add(tempWord);

					}
				}
			}
		}
		// print the words statistic data to file
		FileOutputStream output;
		try {
			File f = new File(c.name + "-" + databaseURL + ".txt");
			if (f.exists())
				f.delete();
			output = new FileOutputStream(c.name + "-" + databaseURL + ".txt");
			PrintStream file = new PrintStream(output);
			System.out.println("keys size : " + keys.size());
			iterator = keys.iterator();
			while (iterator.hasNext()) {
				tempWord = iterator.next();
				file.println(tempWord + "#" + sum.get(tempWord));
			}
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Classify database from category node c
	 * 
	 * @param c
	 *            Category to start with for classification
	 * @return Return the child node category that database belong to
	 */
	private Vector<Category> classify(Category c) {
		Vector<Category> catList = new Vector<Category>();
		try {
			String categoryName = c.name;
			if (c.subcategories == null) {
				// System.out.println("This category is a leaf");
				catList.add(c);
				return catList;
			}

			c.coverage = 0;
			for (int j = 0; j < c.subcategories.size(); j++) {
				c.subcategories.elementAt(j).coverage = 0;
			}

			File f = new File(categoryName.toLowerCase() + ".txt");
			if (!f.exists()) {
				System.err
						.println("File "
								+ categoryName.toLowerCase()
								+ ".txt"
								+ " is not found. It should provide the queries for category."
								+ categoryName.toLowerCase());
			}
			BufferedReader input = new BufferedReader(new FileReader(
					categoryName.toLowerCase() + ".txt"));
			String line = null;
			try {
				int index;
				String subcatName;
				String probingQuery;
				while ((line = input.readLine()) != null) {
					index = line.indexOf((int) ' ');
					if (index < 0) {
						System.err
								.println("No space found. Error parsing a line containing category and probing query");
						index = 0;
					}
					subcatName = line.substring(0, index);
					probingQuery = line.substring(index + 1);
					probingQuery = probingQuery.trim(); // we want to get rid of white spaces at the end or beginning of the string
					c.queries.addElement(probingQuery);
					c = probe(probingQuery, c);
					c.coverage += count;
					for (int j = 0; j < c.subcategories.size(); j++) {
						if (c.subcategories.elementAt(j).name
								.equals(subcatName)) {
							c.subcategories.elementAt(j).coverage += count;
						}
					}

				}
				input.close();

				// iterate category tree to find class for database by recursive
				// calling classify()
				for (int j = 0; j < c.subcategories.size(); j++) {
					c.subcategories.elementAt(j).specificity = (c.specificity)
							* (c.subcategories.elementAt(j).coverage)
							/ c.coverage;
					System.out.println("Specificity for category:" + c.subcategories.elementAt(j).name
							+ " is "
							+ c.subcategories.elementAt(j).specificity);
					System.out.println("Coverage for category:" + c.subcategories.elementAt(j).name
							+ " is "
							+ c.subcategories.elementAt(j).coverage);
					if (c.subcategories.elementAt(j).specificity > specificity
							&& c.subcategories.elementAt(j).coverage > coverage) {
						catList.addAll(classify(c.subcategories.elementAt(j)));
					}

				}
				if (catList.size() == 0) {
					catList.add(c);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return catList;
	}

	/**
	 * Implement QProbe algorithm
	 * 
	 * @param query
	 *            The query for Prober
	 * @param c
	 *            The current node for Prober query
	 * @return Return updated category node (Update samples)
	 */
	private Category probe(String query, Category c) {
		// replace all spaces with "%20" to get url friendly query
		String urlQuery = query.replaceAll(" ", "%20");
		URL url;
		try {
			url = new URL("http://boss.yahooapis.com/ysearch/web/v1/"
					+ urlQuery + "?appid=" + appId + "&format=xml&sites="
					+ databaseURL);
			URLConnection con = url.openConnection();
			InputStream inStream = con.getInputStream();
			Scanner in = new Scanner(inStream);
			StringBuffer temp = new StringBuffer();
			while (in.hasNextLine()) {
				temp.append(in.nextLine());
			}
			String res = temp.toString();
			return parseSearchResult(res, c, query);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}

	/**
	 * Anaylze the XML for search result returned from Yahoo! to find out result
	 * items and total hits
	 * 
	 * @param response
	 *            Response string (XML)
	 * @param c
	 *            Category node for such search
	 * @return Return updated category (update samples and global varible count)
	 */
	private Category parseSearchResult(String response, Category c, String query) {
		SearchResult sr;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					response)));
			NodeList nodeL = doc.getElementsByTagName("resultset_web");
			Node node = nodeL.item(0);
			NamedNodeMap nodeMap = node.getAttributes();
			node = nodeMap.getNamedItem("totalhits");
			count = Integer.parseInt(node.getTextContent());

			// write down the number of matches found for this query
			File f = new File("cache/" + databaseURL + '/' + query + ".txt");
			if (f.exists()) {
//				System.out.println("Query probe file already exists. Checking if the count is the same...");
				BufferedReader input = new BufferedReader(new FileReader("cache/" + databaseURL + '/' + query + ".txt"));
				String line;
				if ((line = input.readLine()) != null
						&& Integer.parseInt(line) == count) {

				} else {
					System.err.println("Problem verifing query hit count.");
					System.err.println("query: " + query);
					System.err.println("old count:" + line + " new count:"
							+ count);
//					System.exit(1);
				}
				input.close();
			} else {
				FileOutputStream output;
				f = new File("cache/");
				if (!f.exists()) {
					f.mkdir();
					f = new File("cache/" + databaseURL);
					f.mkdir();
				}
				output = new FileOutputStream("cache/" + databaseURL + '/' + query + ".txt");
				PrintStream file = new PrintStream(output);
				file.println(count);
				output.close();
			}

			nodeL = doc.getElementsByTagName("result");
			for (int i = 0; i < sampleSize; i++) {
				node = nodeL.item(i);
				if (node != null) {
					sr = new SearchResult(node);
					if (!c.samples.containsKey(sr.url))
						c.samples.put(sr.url, 1);

				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}

	public static void main(String args[]) {
		if (args.length != 3) {
			System.out
					.println("Usage: ClassifyMe <database-url> <specificity> <coverage> <yahoo appId>");
			System.exit(1);
		}
		ClassifyMe cm = new ClassifyMe();
		cm.databaseURL = args[0];
		File f = new File("cache/" + cm.databaseURL);
		if (f.exists()) { 
			File[] files = f.listFiles(); 
			for (int i=0; i<files.length; i++) {
				files[i].delete();
			}
			System.out.println("The old cache directory has been removed.");
		}
		cm.specificity = Double.parseDouble(args[1]);
		cm.coverage = Integer.parseInt(args[2]);
		System.out.println("Classifying...");
		cm.classification = cm.classify(cm.root);
		for (int k = 0; k < cm.classification.size(); k++) {
			cm.categoryPaths.add("");
			cm.getCategoryPath(cm.classification.elementAt(k), k);
		}
		System.out.println("Classification: " + cm.getClassificationPaths());
		String summary = "";
		summary = cm.getClassificationPaths().replace("/",
				"-" + cm.databaseURL + ".txt and ");
		summary = summary + "-" + cm.databaseURL + ".txt";
		System.out.println("Write summary to file: " + summary);
	}
}
