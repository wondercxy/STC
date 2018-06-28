package com.cxy.second;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TFIDF2 implements Serializable {

	private static final long serialVersionUID = -1944746523865028204L;
	// 存放所有词汇
	public static Set<String> vocab = new LinkedHashSet<String>();
	public static List<String> listvocab = new ArrayList<>();
	// 单词 -idf
	public static Map<String, Double> word_idf = new HashMap<String, Double>();

	public static HashMap<Integer, List<Double>> get_tfidf(HashMap<Integer, String> raw_data, String token) {
		HashMap<Integer, List<Double>> res = new HashMap<>();
		// List<List<Double>> res = new ArrayList<List<Double>>();

		Map<String, Set<Integer>> word_docs = new HashMap<String, Set<Integer>>();
		Map<Integer, List<String>> doc_words = new HashMap<Integer, List<String>>();
		int doc_num = 0;

		for (Entry<Integer, String> entry : raw_data.entrySet()) {
			doc_num = entry.getKey();
			String text = entry.getValue();
			String[] words = text.split(token);
			doc_words.put(doc_num, Arrays.asList(words));
			for (String word : words) {
				vocab.add(word);
				if (word_docs.containsKey(word)) {
					word_docs.get(word).add(doc_num);
				} else {
					Set<Integer> docs = new HashSet<Integer>();
					docs.add(doc_num);
					word_docs.put(word, docs);
				}
			}
		}
		// 计算并存储每个word的idf值
		for (String word : vocab) {
			int doc_n = 0;
			if (word_docs.containsKey(word)) {
				doc_n = word_docs.get(word).size();
			}
			double idf = doc_words.size() * 1.0 / (doc_n + 1);
			word_idf.put(word, idf);
		}

		// 计算每篇doc中，vocab中每个word的tfidf值
		for (Entry<Integer, List<String>> e : doc_words.entrySet()) {
			int num = e.getKey();
			List<Double> tmp = new ArrayList<Double>();
			for (String word : vocab) {
				int word_n = Collections.frequency(e.getValue(), word);
				double tf = word_n * 1.0 / e.getValue().size();
				double idf = word_idf.get(word);
				double tfidf = tf * idf;
				tmp.add(tfidf);
			}
			res.put(num, tmp);
		}

		listvocab.addAll(vocab);
		return res;
	}

	public List<List<Double>> get_tfidf4test(List<String> raw_data, String token) {
		List<List<Double>> text_tfidf = new ArrayList<List<Double>>();
		for (String text : raw_data) {
			String[] words = text.split(token);
			List<String> words_list = Arrays.asList(words);
			List<Double> tmp = new ArrayList<Double>();
			for (String word : vocab) {
				int word_n = Collections.frequency(words_list, word);
				double tf = word_n * 1.0 / words.length;
				double tfidf = tf * word_idf.get(word);
				tmp.add(tfidf);
			}
			text_tfidf.add(tmp);
		}
		return text_tfidf;
	}

	public void save_model(String path) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TFIDF load_model(String path) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			TFIDF tfidf = (TFIDF) in.readObject();
			in.close();
			return tfidf;
		} catch (IOException ee) {
			ee.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static HashMap<Integer, Double> count(HashMap<Integer, String> map, String interest, String token) {
		HashMap<Integer, List<Double>> a = get_tfidf(map, token);
		HashMap<Integer, Double> res = new HashMap<>();
		String[] interests = interest.split(token);
		List<Integer> index = new ArrayList<>();
		// System.out.println(listvocab);
		for (String s : interests) {
			index.add(listvocab.indexOf(s));
		}
		for (Entry<Integer, List<Double>> entry : a.entrySet()) {
			int loca = entry.getKey();
			double sum = 0;
			for (int i : index) {
				if (i != -1)
					sum += entry.getValue().get(i);
			}
			if (sum != 0.0)
				res.put(loca, sum);
		}
		// res = (HashMap<Integer, Double>) TA.sortByValue(res);
		return res;
	}

	public static void main(String[] args) {
		HashMap<Integer, String> map = new HashMap<>();
		map.put(0, "我们 是 中国人");
		map.put(2, "他们 是 美国人");
		map.put(5, "你们 来自 哪里 呢 最 无论 不管 the 中国人");

		HashMap<Integer, Double> res = count(map, "是", " ");
		for (Entry<Integer, Double> e : res.entrySet()) {
			System.out.println(e.getKey() + " " + e.getValue());
		}
	}
}