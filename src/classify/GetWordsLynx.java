package classify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

public class GetWordsLynx {

	public static Set<String> runLynx(String url) {

		int buffersize = 40000;
		StringBuffer buffer = new StringBuffer(buffersize);

		try {

			String cmdline[] = { "/usr/bin/lynx", "--dump", url };
			Process p = Runtime.getRuntime().exec(cmdline);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			char[] cbuf = new char[1];

			while (stdInput.read(cbuf, 0, 1) != -1
					|| stdError.read(cbuf, 0, 1) != -1) {
				buffer.append(cbuf);
			}
			p.waitFor();
			stdInput.close();
			stdError.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		// Remove the References at the end of the dump
		int end = buffer.indexOf("\nReferences\n");

		if (end == -1) {
			end = buffer.length();
		}
		// Remove everything inside [ ] and do not write more than two
		// consecutive spaces
		boolean recording = true;
		boolean wrotespace = false;
		StringBuffer output = new StringBuffer(end);

		for (int i = 0; i < end; i++) {
			if (recording) {
				if (buffer.charAt(i) == '[') {
					recording = false;
					if (!wrotespace) {
						output.append(' ');
						wrotespace = true;
					}
					continue;
				} else {
					if (Character.isLetter(buffer.charAt(i))
							&& buffer.charAt(i) < 128) {
						output.append(Character.toLowerCase(buffer.charAt(i)));
						wrotespace = false;
					} else {
						if (!wrotespace) {
							output.append(' ');
							wrotespace = true;
						}
					}
				}
			} else {
				if (buffer.charAt(i) == ']') {
					recording = true;
					continue;
				}
			}
		}
		// Set<String> document = new TreeSet<String>();
		StringTokenizer st = new StringTokenizer(output.toString());
		// use hashtable instead of TreeSet to make sure there is no duplicated
		// words in the result set
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();

			if (!hash.containsKey(tok)) {
				hash.put(tok, 1);
				// document.add(tok);
			}
		}
		return hash.keySet();
	}

	public static void main(String args[]) {
		runLynx(args[0]);
	}
}