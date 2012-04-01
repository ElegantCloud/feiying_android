package com.ivyinfo.feiying.utity;

import java.util.Vector;

public class TextUtility {

	/**
	 * split the text with given split word
	 * 
	 * @param text
	 * @param splitWord
	 * @return
	 */
	public static String[] splitText(String text, String splitWord) {
		Vector<String> sentences = new Vector<String>();
		String[] ret = null;
		if (!text.endsWith(splitWord)) {
			text += splitWord;
		}

		if (text != null && !text.equals("")) {
			for (int i = 0, j = 0;;) {
				i = text.indexOf(splitWord, j);

				if (i >= 0 && i > j) {
					String tmp = text.substring(j, i);
					// System.out.println("[TextUtility] splitText - " + tmp);
					// // @test
					sentences.addElement(tmp);
				}
				if (i < 0 || i == (text.length() - splitWord.length())) {
					break;
				}
				j = i + splitWord.length();
			}

		}

		if (sentences.size() > 0) {
			ret = new String[sentences.size()];
			sentences.copyInto(ret);
		}

		return ret;

	}

	/**
	 * split the text between split1 & split2 by improved algorithm it can deal
	 * with the condition that split1 is the same as split2
	 * 
	 * @param text
	 * @param split1
	 * @param split2
	 * @return
	 */
	public static String[] splitText(String text, String split1, String split2) {
		Vector<String> words = new Vector<String>();
		String[] ret = null;

		int i = 0;
		int j = 0;

		do {
			// get the first matched word
			i = text.indexOf(split1, j);
			// get the following matched word
			if ((i + split1.length()) < text.length()) {
				j = text.indexOf(split2, i + split1.length());
			}
			// if the i & j are not out of bound length of text
			if (j > i && j < text.length() && i < text.length()) {

				String tmp = text.substring(i + split1.length(), j);

				// System.out.println("[TextUtility] splitTextPro: " + tmp);
				// //@test
				words.addElement(tmp);

			}

			j = j + split2.length();
		} while (text.indexOf(split1, j) > 0 && text.indexOf(split2, j) > 0);

		if (words.size() > 0) {
			ret = new String[words.size()];
			words.copyInto(ret);
		}
		return ret;

	}

	/**
	 * parse and get the phone number from the input text
	 * 
	 * @param text
	 * @return
	 */
	public static String getPhoneNumberFromString(String text) {
		StringBuffer sb = new StringBuffer();

		if (text == null) {
			return "";
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((c <= '9' && c >= '0') || c == '+') {
				sb.append(c);
			}
		}

		return sb.toString();

	}

	/**
	 * parse and get the number from the input text
	 * 
	 * @param text
	 * @return
	 */
	public static String getNumberFromString(String text) {
		StringBuffer sb = new StringBuffer();

		if (text == null) {
			return "";
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c <= '9' && c >= '0') {
				sb.append(c);
			}
		}

		return sb.toString();

	}

	/**
	 * replace the search word with replacement word in the input text
	 * 
	 * @param input
	 * @param search
	 * @param replacement
	 * @return
	 */
	public static String replace(String input, String search, String replacement) {
		int pos = input.indexOf(search);
		if (pos != -1) {
			StringBuffer buffer = new StringBuffer();
			int lastPos = 0;
			do {
				buffer.append(input.substring(lastPos, pos))
						.append(replacement);
				lastPos = pos + search.length();
				pos = input.indexOf(search, lastPos);
			} while (pos != -1);
			buffer.append(input.substring(lastPos));
			input = buffer.toString();
		}
		return input;
	}

}
