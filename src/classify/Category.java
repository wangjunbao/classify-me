package classify;

import java.util.Hashtable;
import java.util.Vector;

public class Category {
	public String name;
	public double specificity;
	public int coverage;
	public Category parent;
	public Vector<Category> subcategories;
	public Vector<String> queries = new Vector<String>();
	public Hashtable<String, Integer> samples = new Hashtable<String, Integer>();

	/**
	 * class init function: set its specificity and coverage both to 0
	 */
	public Category() {
		name = "";
		specificity = 0;
		coverage = 0;
		parent = null;
	}

	/**
	 * This function will print out the entire subtree of which current node is
	 * the root
	 */
	public void printTree() {
		System.out.println("---:" + name);
		if (subcategories == null)
			return;
		System.out.println("-: ");
		for (int i = 0; i < subcategories.size(); i++) {
			subcategories.elementAt(i).printTree();
		}
		System.out.println("-: ");
	}
}
