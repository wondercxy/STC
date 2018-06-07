package com.cxy.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.BetaDistribution;

public class JIM {
	// Static number of user,location,region,topic in foursuqre dataset
	public Short UserNumber = 4164;
	public Integer LocationNumber = 119896;
	public Byte RegionNumber = 70;
	public Byte TopicNumber = 54;
	// public Short a=1;

	// prior value
	public Double alpha;
	public Double alphaSum;
	public Double gamma;
	public Double gammaSum;
	public Double tao;
	public Double taoSum;
	public HashMap<Byte, Double[]> mu;
	public HashMap<Byte, Double[]> sigma;

	// number of count for user-topic, region-location, user-region, topic-word
	public HashMap<Short, HashMap<Byte, Integer>> userTopicCount;
	public HashMap<Byte, Integer> userTopicSum;
	public HashMap<Short, HashMap<Byte, Double>> userTopicDis;

	public HashMap<Short, HashMap<Byte, Integer>> userRegionCount;
	public HashMap<Byte, Integer> userRegionSum;
	public HashMap<Short, HashMap<Byte, Double>> userRegionDis;

	public HashMap<Byte, HashMap<Integer, Integer>> regionLocationCount;
	public HashMap<Byte, Integer> regionLocationSum;
	public HashMap<Byte, HashMap<Integer, Double>> regionLocationDis;

	/**
	 * Actually not used in foursuqre datraset, since we donot have words in dataset
	 *** public HashMap<Integer,HashMap<Integer,Integer>> topicWordCount; public
	 * HashMap<Integer,Integer> topicWordSum;
	 */
	public HashMap<String, Byte> topic_dictionary;
	public HashMap<Short, HashMap<Byte, HashMap<Byte, Double>>> user_topic_time_density;

	public JIM() {

		this.alpha = 1.0 / TopicNumber;
		this.alphaSum = 1.0;
		this.gamma = 1.0 / RegionNumber;
		this.gammaSum = 0.0;
		this.tao = 1.0 / LocationNumber;
		this.taoSum = 1.0;
		userTopicCount = new HashMap<Short, HashMap<Byte, Integer>>();
		userTopicSum = new HashMap<Byte, Integer>();
		userTopicDis = new HashMap<Short, HashMap<Byte, Double>>();

		userRegionCount = new HashMap<Short, HashMap<Byte, Integer>>();
		userRegionSum = new HashMap<Byte, Integer>();
		userRegionDis = new HashMap<Short, HashMap<Byte, Double>>();

		regionLocationCount = new HashMap<Byte, HashMap<Integer, Integer>>();
		regionLocationSum = new HashMap<Byte, Integer>();
		regionLocationDis = new HashMap<Byte, HashMap<Integer, Double>>();

		topic_dictionary = new HashMap<String, Byte>();
		user_topic_time_density = new HashMap<Short, HashMap<Byte, HashMap<Byte, Double>>>();
	}

	/**
	 * The key of topic_dictionary is topic, and the value of topic_dictionary is
	 * specific id number This specific id number is used for topic difference
	 **/

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
				return (e2.getValue()).compareTo(e1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public void TopicDictionaryMaker() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("WORDS.txt")));
		String line = reader.readLine();
		Byte value = 1;
		while (line != null) {
			String[] ar = line.split(" ");
			String topic = ar[0];

			if (!topic_dictionary.containsKey(topic)) {
				topic_dictionary.put(topic, value);
				value++;
			}
			line = reader.readLine();

		}
		reader.close();
		System.out.println("TopicDictionaryMaker() is done!");
	}

	public void getUserTopicBeta() throws IOException {
		// user_topic_time_density=new HashMap<Short,HashMap<Byte,HashMap<Byte,Double>>>
		// ();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("UserTopicBetaTrain.txt")));
		String line = reader.readLine();
		while (line != null) {
			String[] ar = line.split("\t");
			Short u_id = Short.valueOf(ar[0]);

			HashMap<Byte, HashMap<Byte, Double>> ttd = new HashMap<Byte, HashMap<Byte, Double>>();
			for (int k = 1; k < ar.length; k++) {
				String[] toIn = ar[k].split(" ");
				BetaDistribution bd = new BetaDistribution(Double.valueOf(toIn[1]), Double.valueOf(toIn[2]));
				HashMap<Byte, Double> td = new HashMap<Byte, Double>();
				for (Byte t = 0; t < 24; t++) {
					Double myX = 0.0;
					if (t == 0)
						myX = 0.01;
					else
						myX = Double.valueOf(t) / 24.0;

					td.put(t, bd.density(myX));

				}
				ttd.put(Byte.valueOf(toIn[0]), td);

			}

			user_topic_time_density.put(u_id, ttd);

			line = reader.readLine();
		}

		System.out.println("User_topic_time_desntiy is made");

	}

	public void InitialState() {
		for (Short i = 1; i < UserNumber; i++) {
			HashMap<Byte, Integer> hm = new HashMap<Byte, Integer>();
			HashMap<Byte, Double> hm2 = new HashMap<Byte, Double>();
			for (Byte b = 1; b < TopicNumber; b++) {
				hm.put(b, 0);
				hm2.put(b, 0.0);

			}
			userTopicCount.put(i, hm);
			userTopicDis.put(i, hm2);
		}
		for (Short i = 1; i < UserNumber; i++) {
			HashMap<Byte, Integer> hm1 = new HashMap<Byte, Integer>();
			HashMap<Byte, Double> hm2 = new HashMap<Byte, Double>();
			for (Byte b = 0; b < RegionNumber; b++) {
				hm1.put(b, 0);
				hm2.put(b, 0.0);
			}
			userRegionCount.put(i, hm1);
			userRegionDis.put(i, hm2);
		}
		for (Byte b = 0; b < RegionNumber; b++) {
			HashMap<Integer, Integer> hm1 = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> hm2 = new HashMap<Integer, Double>();
			for (Integer i = 0; i < LocationNumber; i++) {
				hm1.put(i, 0);
				hm2.put(i, 0.0);
			}
			regionLocationCount.put(b, hm1);
			regionLocationDis.put(b, hm2);
		}
		System.out.println("Initial State Done");

	}

	/**
	 * Actually we know the specific topic not a latent variable So calculate it
	 * directly
	 */

	public void GibbsSampling() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("train5JIM.txt")));
		String line = reader.readLine();
		Integer number = 0;
		while (line != null) {
			String[] info = line.split("\t");
			Short u_id = Short.valueOf(info[0]);
			Integer l_id = Integer.valueOf(info[2]);
			String[] topic = info[5].split(" ");
			Byte r_id = Byte.valueOf(info[7]);

			for (Byte b = 0; b < topic.length; b++) {
				if (!topic[b].equals("")) {
					Byte index = topic_dictionary.get(topic[b]);
					number = userTopicCount.get(u_id).get(index);
					number++;
					userTopicCount.get(u_id).remove(index);
					userTopicCount.get(u_id).put(index, number);
				}
			}
			number = userRegionCount.get(u_id).get(r_id);
			number++;
			userRegionCount.get(u_id).remove(r_id);
			userRegionCount.get(u_id).put(r_id, number);

			number = regionLocationCount.get(r_id).get(l_id);
			number++;
			regionLocationCount.get(r_id).remove(l_id);
			regionLocationCount.get(r_id).put(l_id, number);

			line = reader.readLine();
		}
		reader.close();
		System.out.println("Gibbs Sampling is done");

	}

	/**
	 * DISTRIBUTION_user_topic, user_region, region_location is calculate the
	 * distribution based on the sampling
	 **
	 **/
	public void DISTRIBUTION_user_topic() {
		for (Short s = 1; s < UserNumber; s++) {
			HashMap<Byte, Double> hm = new HashMap<Byte, Double>();
			Integer total = 0;
			for (Byte b = 1; b < TopicNumber; b++) {
				total += userTopicCount.get(s).get(b);
			}

			for (Byte b = 1; b < TopicNumber; b++) {
				Integer ut = userTopicCount.get(s).get(b);
				Double value = (Double.valueOf(ut) + alpha) / (Double.valueOf(total) + alphaSum);
				hm.put(b, value);
			}
			userTopicDis.put(s, hm);
		}
	}

	public void DISTRIBUTION_user_region() {
		for (Short s = 1; s < UserNumber; s++) {
			HashMap<Byte, Double> hm = new HashMap<Byte, Double>();
			Integer total = 0;
			for (Byte b = 1; b < TopicNumber; b++) {
				total += userRegionCount.get(s).get(b);
			}

			for (Byte b = 1; b < RegionNumber; b++) {
				Integer ur = userRegionCount.get(s).get(b);
				Double value = (Double.valueOf(ur) + gamma) / (Double.valueOf(total) + gammaSum);
				hm.put(b, value);
			}
			userRegionDis.put(s, hm);
		}
	}

	public void DISTRIBUTION_region_location() {
		for (Byte b = 1; b < RegionNumber; b++) {
			HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
			Integer total = 0;
			for (Integer i = 1; i < LocationNumber; i++) {
				total += regionLocationCount.get(b).get(i);
			}
			for (Integer i = 1; i < LocationNumber; i++) {
				Integer rl = regionLocationCount.get(b).get(i);
				Double value = (Double.valueOf(rl) + tao) / (Double.valueOf(total) + taoSum);
				hm.put(i, value);
			}
			regionLocationDis.put(b, hm);

		}
	}

	public void CalDistribution() {
		DISTRIBUTION_user_topic();
		DISTRIBUTION_user_region();
		DISTRIBUTION_region_location();
		// DISTRIBUTION topic_word();

		System.out.println("CalDistribution done");
	}

	public void Result() throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("JIMTEST5.txt")));
		BufferedWriter wr0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("RESULTJIM.txt"), "utf-8"));
		String line = reader.readLine();
		int re_count = 0;
		while (line != null) {
			re_count++;
			if (re_count % 100 == 0)
				System.out.println("line =" + re_count + "in result");

			String[] info = line.split("\t");
			Short u_id = Short.valueOf(info[0]);
			Byte time = Byte.valueOf(info[1]);
			Integer l_id = Integer.valueOf(info[2]);
			String[] topic = info[5].split(" ");
			Byte r_id = Byte.valueOf(info[7]);

			HashMap<Integer, Double> regionloc = regionLocationDis.get(r_id);
			HashMap<Integer, Double> rank = new HashMap<Integer, Double>();
			for (Integer loc_id : regionloc.keySet()) {
				Double lp = regionloc.get(loc_id);
				for (Byte t = 1; t < TopicNumber; t++) {
					Double U2Z = userTopicDis.get(u_id).get(t) * user_topic_time_density.get(u_id).get(t).get(time);
					Double value = lp + U2Z;
					rank.put(loc_id, value);
				}
			}
			rank = (HashMap<Integer, Double>) sortByValue(rank);

			Iterator<Integer> iter = rank.keySet().iterator();
			Integer count = 0;
			wr0.write(u_id + " ");
			while (iter.hasNext()) {
				int key = iter.next();
				Double value = rank.get(key);
				wr0.write("(" + key + "," + value + ") ");
				count++;
				if (count >= 30)
					break;
			}
			wr0.write("\n");

			line = reader.readLine();
		}
		wr0.flush();
		wr0.close();
		reader.close();
	}

	public static void main(String[] args) throws IOException {
		JIM jim = new JIM();
		jim.TopicDictionaryMaker();
		;
		jim.getUserTopicBeta();
		jim.InitialState();
		jim.GibbsSampling();
		jim.CalDistribution();
		jim.Result();
	}
}
