package classify;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
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
	private Category root;
	private int count;
	private int sampleSize = 4;
	private Hashtable<String, Integer> samples = new Hashtable<String, Integer>();
	private String categoryPath = "";

	/**
	 * Initialize the category tree from file
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

		root.printTree();
	}

	public String getClassificationPath() {
		return categoryPath;
	}

	private ArrayList<String> addElementToList(ArrayList<String> array,
			int start, int end, String element) {
		if (start == end) {
			if (end == 0)
				array.add(element);
			else {
				array.add(start, element);
			}

		} else {
			String temp = array.get(array.size() / 2);
			if (element.compareTo(temp) < 0)
				array = addElementToList(array, start, array.size() / 2,
						element);
			else
				array = addElementToList(array, array.size() / 2 , array
						.size() - 1, element);
		}
		return array;

	}

	/**
	 * Print out the Category path the database belongs to and make summary
	 * based on its classification
	 * 
	 * @param c
	 *            the Child class node that database belongs to
	 */
	protected void printCategoryPath(Category c) {
		if (c.parent != null) {
			printCategoryPath(c.parent);
			categoryPath = categoryPath + '/';

		}
		categoryPath = categoryPath + c.name;
		Iterator<String> innerIterator;
		Hashtable<String, Integer> sum = new Hashtable<String, Integer>();
		String tempWord;
		Set<String> tempWords;
		Integer innerTempValue;
		String tempUrl;
		ArrayList<String> keys = new ArrayList<String>();
		Iterator<String> iterator = c.samples.keySet().iterator();
		while (iterator.hasNext()) {
			tempUrl = (String) (iterator.next());
			// make sure no duplicated url from different queries
			if (!samples.containsKey(tempUrl)) {
				samples.put(tempUrl, 1);
				System.out.println("Crawling : " + tempUrl);
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
						keys = addElementToList(keys, 0, keys.size(),
								tempWord);
						System.out.println(tempWord);
					}
				}
			}
		}
		FileOutputStream output;
		try {
			output = new FileOutputStream(c.name + "-" + databaseURL + ".txt");
			PrintStream file = new PrintStream(output);
			System.out.println("keys size : " + keys.size());
			// Sort words to alphabetical order
			for (int i = 0; i < keys.size(); i++) {
				for (int j = 0; j < keys.size() - i - 1; j++) {
					if ((keys.get(j).compareTo(keys.get(j + 1))) < 0) {
						tempWord = keys.get(j);
						keys.remove(j);
						keys.add(j + 1, tempWord);
					}
				}
				System.out.println("Writing to file " + c.name + "-"
						+ databaseURL + ".txt : "
						+ keys.get(keys.size() - i - 1) + " : "
						+ sum.get(keys.get(keys.size() - i - 1)));
				file.println(keys.get(keys.size() - i - 1) + " : "
						+ sum.get(keys.get(keys.size() - i - 1)));
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
								.println("No space found. Error parsing a line containg category and probing query");
						index = 0;
					}
					subcatName = line.substring(0, index);
					probingQuery = line.substring(index + 1);
					c.queries.addElement(probingQuery);
					// System.out.println("name:" + subcatName + " query:" +
					// probingQuery);
					c = probe(probingQuery, c);
					// System.out.println(count);
					c.coverage += count;
					for (int j = 0; j < c.subcategories.size(); j++) {
						if (c.subcategories.elementAt(j).name
								.equals(subcatName)) {
							c.subcategories.elementAt(j).coverage += count;
						}
					}

				}
				System.out.println(categoryName + " coverage:" + c.coverage);

				// iterate category tree to find class for database by recursive
				// calling classify()
				for (int j = 0; j < c.subcategories.size(); j++) {
					System.out.println(c.subcategories.elementAt(j).name
							+ " coverage:"
							+ c.subcategories.elementAt(j).coverage);
					c.subcategories.elementAt(j).specificity = (c.specificity)
							* (c.subcategories.elementAt(j).coverage)
							/ c.coverage;
					System.out.println(c.subcategories.elementAt(j).name
							+ " specificity:"
							+ c.subcategories.elementAt(j).specificity);
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
			url = new URL("http://boss.yahooapis.com/ysearch/web/v1/'"
					+ urlQuery + "'?appid=" + appId + "&format=xml&sites="
					+ databaseURL);
			System.out.println(url);
			URLConnection con = url.openConnection();
			InputStream inStream = con.getInputStream();
			Scanner in = new Scanner(inStream);
			StringBuffer temp = new StringBuffer();
			while (in.hasNextLine()) {
				temp.append(in.nextLine());
			}
			String res = temp.toString();
			return parseSearchResult(res, c);

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
	private Category parseSearchResult(String response, Category c) {
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
		cm.specificity = Double.parseDouble(args[1]);
		cm.coverage = Integer.parseInt(args[2]);
		cm.classification = cm.classify(cm.root);
		for (int k = 0; k < cm.classification.size(); k++) {
			// System.out.println(cm.classification.elementAt(k).name);
			cm.printCategoryPath(cm.classification.elementAt(k));
		}
		System.out.println("Classification: " + cm.getClassificationPath());
		String summary = "";
		summary = cm.getClassificationPath().replace("/",
				"-" + cm.databaseURL + ".txt and ");
		summary = summary + "-" + cm.databaseURL + ".txt";
		System.out.println("Write summary to file: " + summary);
	}
}
