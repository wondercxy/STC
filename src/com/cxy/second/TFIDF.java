package com.cxy.second;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TFIDF {
	// ������дʻ�
	public static Set<String> vocab = new LinkedHashSet<String>();
	// ���� -idf
	public static Map<String, Double> word_idf = new HashMap<String, Double>();

	/**
	 * ѵ��������tfidfֵ��Ҳ��ѵ��tfidfģ��
	 * 
	 * @param raw_data
	 *            ѵ�����ݣ��磺[[���� �� �й� �Ĺ���], [���� �� �׻�֮��]]��tokenΪ�ո�
	 * @param token
	 *            ����֮��ָ���
	 * @return ѵ�����ݶ�Ӧ��tfidf�����б�
	 */
	public static void get_tfidf(String raw_data, String token) {
		String[] words = raw_data.split(token);
		for (String word : words) {
			vocab.add(word);
		}
		// ���㲢�洢ÿ��word��idfֵ
		for (String word : vocab) {
			double idf = 1 * 1.0 / (1 + 1);
			word_idf.put(word, idf);
		}
		// System.out.println(word_idf);
	}

	/**
	 * �������������tfidfֵ
	 * 
	 * @param raw_data
	 *            ��������
	 * @param token
	 *            ����֮��ķָ���
	 * @return �������ݵ�tfidfֵ
	 */
	public static double get_tfidf4test(String raw_data, String split) {
		String[] words = raw_data.split(split);
		List<String> words_list = Arrays.asList(words);
		double sum = 0;
		for (String word : vocab) {
			int word_n = Collections.frequency(words_list, word);
			double tf = word_n * 1.0 / words.length;
			double tfidf = tf * word_idf.get(word);
			sum += tfidf;
		}

		return sum / words.length;

	}

	/**
	 * ���л�����tfidfģ��
	 * 
	 * @param path
	 *            ģ��·��
	 */
	public static double tdidfValue(String locacontent, String interest, String token) {
		get_tfidf(locacontent, token);
		return get_tfidf4test(interest, token);
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

	/**
	 * ����tfidfģ��
	 * 
	 * @param path
	 *            ģ��·��
	 * @return ѵ���õ�TFIDFģ��
	 */
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

	public static void main(String[] args) {
		// TFIDF tfidf = new TFIDF();
		//
		// tfidf.get_tfidf(
		// "atm|convenience|store|gas|deli|bodega|delis|bodegas|atm|convenience|store|gas|deli|bodega|delis|bodegas|squad|hardware|music|phones|retail|shopping|software|stereo|tv|video|games|wifi|xbox|store|stores|",
		// "\\|");
		//
		// System.out.println(tfidf.get_tfidf4test("apple|cameras|geek|squad|hardware|music",
		// "\\|"));
	}
}