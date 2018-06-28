package com.cxy.model;

/*
 * 对用户细分时间
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cxy.data.GetConnection;
import com.cxy.data.TimeAndContent;
import com.cxy.data.TimeAndLoca;

public class STCI {
	public int UserNumber = 0;// 用户数量
	public int LocationNumber = 0;// 地点数量
	public int HourNumber = 24;// 时间
	public int RegionNumber = 70;// 区域
	public int TopicNumber = 70;// 主题数
	public int distance = 15;// 用户活动范围
	public int distanceHome = 100;

	public int ITERATIONS = 1000;// 迭代次数
	public int SAMPLE_LAG = 10;
	public int BURN_IN = ITERATIONS - 20;

	public String outputPath;
	public String inputPath = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\checkin2.txt";// 签到数据

	public ArrayList<String> trainData;// 训练数据
	public ArrayList<String> predictData;// 测试数据

	public ArrayList<Integer> userList = new ArrayList<>();
	public HashMap<Integer, Integer> userToIndex = new HashMap<>();
	public ArrayList<Integer> locaList = new ArrayList<>();
	public HashMap<Integer, Integer> locaToIndex = new HashMap<>();

	public Double alpha;// 用户兴趣
	public Double alphaSum;
	public Double gamma;// 区域
	public Double gammaSum;
	public Double tao;// 区域位置
	public Double taoSum;

	// 用户
	public int[][][] user;
	public int[][][] z;
	public int[][][] userTopicCount;
	public int[][] topicItemCount;
	public int[][] userTopicCountSum;
	public int[] topicItemCountSum;
	double[][] topicItemDistribution;
	double[][][] userTopicDistribution;

	public int[][] region;
	public int[][] r;
	public int[][] userAreaCount;
	public int[] userAreaCountSum;
	public int[][] areaItemCount;
	public int[] areaItemCountSum;
	public double[][] userAreaDistribution;
	public double[][] areaItemDistribution;

	public HashMap<Integer, List<String>> hourcontent;

	public Map<String, Integer> termToIndexMap;
	public ArrayList<String> indexToTermMap;
	public Map<String, Integer> termCountMap;

	public Map<String, Integer> hourtermToIndexMap;
	public ArrayList<String> indexToHourTermMap;
	public Map<String, Integer> hourtermCountMap;

	public Map<Integer, Integer> locaToIndexMap;
	public ArrayList<Integer> indexToLocaMap;
	public Map<Integer, Integer> locaCountMap;

	public HashMap<Integer, Point> home_location;// 记录用户去过的所有地点
	public HashMap<Integer, HashMap<Integer, ArrayList<String>>> user_content;
	public HashMap<Integer, ArrayList<Integer>> user_location;
	public HashMap<Integer, String> location_content;
	// public HashMap<Integer, HashMap<Integer, Integer>> region_point;
	public HashMap<Integer, Point> location_map;
	public HashMap<Integer, Integer> location_region;
	public HashMap<Integer, HashSet<Integer>> region_location;
	public Point mu[];
	public double covariance[][];
	public double muSum[][];
	public double covarianceSum[][];

	public HashMap<String, Integer> topic_dictionary;
	public HashMap<Short, HashMap<Byte, HashMap<Byte, Double>>> user_topic_time_density;

	public STCI() {

		this.alpha = 0.5;
		this.alphaSum = 1.0;
		this.gamma = 0.5;
		this.gammaSum = 1.0;

		trainData = new ArrayList<>();
		predictData = new ArrayList<>();

		location_region = new HashMap<>();
		hourcontent = new HashMap<>();

		termToIndexMap = new HashMap<>();
		indexToTermMap = new ArrayList<>();
		termCountMap = new HashMap<>();

		hourtermToIndexMap = new HashMap<>();
		indexToHourTermMap = new ArrayList<>();
		hourtermCountMap = new HashMap<>();

		termToIndexMap = new HashMap<>();
		indexToTermMap = new ArrayList<>();
		termCountMap = new HashMap<>();

		locaToIndexMap = new HashMap<>();
		indexToLocaMap = new ArrayList<>();
		locaCountMap = new HashMap<>();

		home_location = new HashMap<>();
		user_content = new HashMap<>();
		user_location = new HashMap<>();
		location_content = new HashMap<>();
		// region_point = new HashMap<>();
		location_map = new HashMap<>();
		region_location = new HashMap<>();
		mu = new Point[RegionNumber];
		covariance = new double[RegionNumber][2];
		muSum = new double[RegionNumber][2];
		covarianceSum = new double[RegionNumber][2];

		topic_dictionary = new HashMap<String, Integer>();
		user_topic_time_density = new HashMap<Short, HashMap<Byte, HashMap<Byte, Double>>>();
	}

	// 从数据库读取训练数据
	public void readTrain() throws Exception {
		Connection con = GetConnection.getCon();
		String sql = "select user_id,latitude,longitude,location_id,time,content from checkinnewtrain";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			String tmp = rs.getInt(1) + "--" + rs.getDouble(2) + "--" + rs.getDouble(3) + "--" + rs.getInt(4) + "--"
					+ rs.getString(5) + "--" + rs.getString(6);
			trainData.add(tmp);
		}
		rs.close();
		ps.close();
		con.close();
		System.out.println("TrainData is done!");
	}

	// 从数据库读取测试数据
	public void readPredict() throws Exception {
		Connection con = GetConnection.getCon();
		String sql = "select user_id,latitude,longitude,location_id,time,content from checkinnewpredict";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			String tmp = rs.getInt(1) + "--" + rs.getDouble(2) + "--" + rs.getDouble(3) + "--" + rs.getInt(4) + "--"
					+ rs.getString(5) + "--" + rs.getString(6);
			predictData.add(tmp);
		}
		rs.close();
		ps.close();
		con.close();
		System.out.println("PredictData is done!");
	}

	// 类别索引
	public void TopicDictionaryMaker() throws IOException {
		// BufferedReader reader = new BufferedReader(new InputStreamReader(new
		// FileInputStream(inputPath)));
		// String line = reader.readLine();
		int value = 0;
		// while (line != null) {
		for (String line : trainData) {
			String[] ar = line.split("--");
			String[] con = ar[5].split(",");

			for (String s : con) {
				if (!topic_dictionary.containsKey(s)) {
					topic_dictionary.put(s, value);
					value++;
				}
			}
			// line = reader.readLine();
		}
		// reader.close();

		// System.out.println(topic_dictionary.size());
		System.out.println("TopicDictionaryMaker() is done!");

	}

	// 读取每个地点kmeans聚类之后所属区域
	public List<Integer> getRegion() throws Exception {
		List<Integer> regionList = new ArrayList<>();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("F:\\AnacondaWork\\cluster\\kmeans.txt")));
		String line = reader.readLine();
		while (line != null) {
			int region = Integer.parseInt(line);
			regionList.add(region);
			line = reader.readLine();
		}

		reader.close();

		return regionList;
	}

	// 用于每个区域中的location_id--point
	public void RegionLocationMaker() throws Exception {
		int id = 0;
		List<Integer> regionList = getRegion();// 区域id
		// BufferedReader reader = new BufferedReader(new InputStreamReader(new
		// FileInputStream(inputPath)));
		// String line = reader.readLine();
		// while (line != null) {
		for (String line : trainData) {
			String[] ar = line.split("--");
			int u_id = Integer.valueOf(ar[0]);
			// int region = Integer.valueOf(ar[6]);
			int location_id = Integer.valueOf(ar[3]);
			// int region = Integer.parseInt(ar[6]);
			int region = regionList.get(id++);
			int hour = Integer.valueOf(ar[4].split(" ")[1].split(":")[0]);
			double latitude = Double.valueOf(ar[1]);
			double longitude = Double.valueOf(ar[2]);
			String contents = ar[5];

			if (!userList.contains(u_id)) {
				userList.add(u_id);
				int size = userList.size();
				userToIndex.put(u_id, size - 1);// 用户索引
			}

			if (!locaList.contains(location_id)) {
				locaList.add(location_id);
				int size = locaList.size();
				locaToIndex.put(location_id, size - 1);// 地点索引
			}

			if (user_content.containsKey(u_id)) {
				String[] cs = contents.split(",");
				HashMap<Integer, ArrayList<String>> map = user_content.get(u_id);
				if (map.containsKey(hour)) {
					ArrayList<String> list = map.get(hour);
					for (String s : cs) {
						list.add(s);
					}
					map.put(hour, list);
					user_content.put(hour, map);
				} else {
					ArrayList<String> list = new ArrayList<>();
					for (String s : cs) {
						list.add(s);
					}
					map.put(hour, list);
					user_content.put(hour, map);
				}
			} else {
				HashMap<Integer, ArrayList<String>> map = new HashMap<>();
				ArrayList<String> list = new ArrayList<>();
				String[] cs = contents.split(",");
				for (String s : cs) {
					list.add(s);
				}
				map.put(hour, list);
				user_content.put(u_id, map);
			}

			if (user_location.containsKey(u_id)) {
				ArrayList<Integer> list = user_location.get(u_id);
				list.add(location_id);
				user_location.put(u_id, list);
			} else {
				ArrayList<Integer> list = new ArrayList<>();
				list.add(location_id);
				user_location.put(u_id, list);
			}

			if (!location_map.containsKey(location_id)) {
				Point u = new Point(latitude, longitude, location_id);
				location_map.put(location_id, u);// 地点-经纬度
			}

			if (!location_content.containsKey(location_id)) {
				location_content.put(location_id, contents);// 地点-类别
			}

			if (!location_region.containsKey(location_id))
				location_region.put(location_id, region);// 地点-区域

			if (region_location.containsKey(region)) {
				HashSet<Integer> set = region_location.get(region);
				set.add(location_id);
				region_location.put(region, set);// 区域地点
			} else {
				HashSet<Integer> set = new HashSet<>();
				set.add(location_id);
				region_location.put(region, set);
			}

			// if (region_point.containsKey(region)) {
			// HashMap<Integer, Integer> map = region_point.get(region);
			// if (map.containsKey(location_id))
			// map.put(location_id, map.get(location_id) + 1);
			// else {
			// map.put(location_id, 1);
			// }
			// } else {
			// HashMap<Integer, Integer> map = new HashMap<>();
			// map.put(location_id, 1);
			// region_point.put(region, map);
			// }
			// line = reader.readLine();
		}
		// reader.close();

		hourcontent = TimeAndContent.timecontent();

		UserNumber = userList.size();
		LocationNumber = locaList.size();

		System.out.println(UserNumber + " " + LocationNumber);

		this.tao = 0.5;
		this.taoSum = 1.0;

		System.out.println("LocationMapMaker() is done!");
	}

	public void InitialState() throws Exception {
		userTopicCount = new int[UserNumber][HourNumber][TopicNumber];// 用户：时间中的k主题出现的次数
		topicItemCount = new int[TopicNumber][topic_dictionary.size()];// 用户：m文档中k主题出现的单词数
		userTopicCountSum = new int[UserNumber][HourNumber];// 用户时间中主题总数
		topicItemCountSum = new int[TopicNumber];// k主题对应的总词数

		topicItemDistribution = new double[TopicNumber][topic_dictionary.size()];
		userTopicDistribution = new double[UserNumber][HourNumber][TopicNumber];

		userAreaCount = new int[UserNumber][RegionNumber];
		userAreaCountSum = new int[UserNumber];
		areaItemCount = new int[RegionNumber][LocationNumber];
		areaItemCountSum = new int[RegionNumber];

		userAreaDistribution = new double[UserNumber][RegionNumber];
		areaItemDistribution = new double[RegionNumber][LocationNumber];

		user = new int[UserNumber][HourNumber][];
		for (int m = 0; m < UserNumber; m++) {
			int uid = userList.get(m);
			if (user_content.containsKey(uid)) {
				HashMap<Integer, ArrayList<String>> map = user_content.get(uid);
				for (Entry<Integer, ArrayList<String>> e : map.entrySet()) {
					int hour = e.getKey();
					int N = e.getValue().size();
					if (m == 0 && hour == 2)
						System.out.println(N);
					int[] docWords = new int[N];// 记录单词索引的位置
					for (int i = 0; i < N; i++) {
						String word = e.getValue().get(i);
						if (!termToIndexMap.containsKey(word)) {
							int newIndex = termToIndexMap.size();
							termToIndexMap.put(word, newIndex);
							indexToTermMap.add(word);
							termCountMap.put(word, new Integer(1));
							docWords[i] = newIndex;
						} else {
							docWords[i] = termToIndexMap.get(word);
							termCountMap.put(word, termCountMap.get(word) + 1);// 记录单词出现的次数
						}
					}
					user[m][hour] = new int[N];
					for (int n = 0; n < N; n++) {
						user[m][hour][n] = docWords[n];// 得到文档-单词矩阵
					}
				}
			}
		}

		z = new int[UserNumber][HourNumber][];
		for (int m = 0; m < UserNumber; m++) {
			int uid = userList.get(m);
			if (user_content.containsKey(uid)) {
				HashMap<Integer, ArrayList<String>> map = user_content.get(uid);
				for (Entry<Integer, ArrayList<String>> e : map.entrySet()) {
					int hour = e.getKey();
					int N = e.getValue().size();// 获取文档中不同单词的个数
					z[m][hour] = new int[N];
					for (int n = 0; n < N; n++) {
						int initTopic = (int) (Math.random() * TopicNumber);
						z[m][hour][n] = initTopic;// 随机分配一个主题给单词
						// number of words in doc m assigned to topic initTopic add 1
						userTopicCount[m][hour][initTopic]++;// 累加随机分配主题的单词个数
						// number of terms doc[m][n] assigned to topic initTopic add 1
						// System.out.println(N + " " + initTopic + " " + m + " " + hour + " " + n);
						topicItemCount[initTopic][user[m][hour][n]]++;// 累加分配给主题K文档m中的单词数
						// total number of words assigned to topic initTopic add 1
						topicItemCountSum[initTopic]++;// 累加分配给主题的单词总数
					}
					// total number of words in document m is N
					userTopicCountSum[m][hour] = N;// m文档中主题总数
				}
			}
		}

		region = new int[UserNumber][];
		for (int m = 0; m < UserNumber; m++) {
			int uid = userList.get(m);
			if (user_location.containsKey(uid)) {
				int N = user_location.get(uid).size();
				int[] docWords = new int[N];// 记录单词索引的位置
				for (int i = 0; i < N; i++) {
					int loca = user_location.get(uid).get(i);
					if (!locaToIndexMap.containsKey(loca)) {
						int newIndex = locaToIndexMap.size();
						locaToIndexMap.put(loca, newIndex);
						indexToLocaMap.add(loca);
						locaCountMap.put(loca, new Integer(1));
						docWords[i] = newIndex;
					} else {
						docWords[i] = locaToIndexMap.get(loca);
						locaCountMap.put(loca, locaCountMap.get(loca) + 1);// 记录单词出现的次数
					}
				}
				region[m] = new int[N];
				for (int n = 0; n < N; n++) {
					region[m][n] = docWords[n];// 得到文档-单词矩阵
				}
			}
		}

		r = new int[UserNumber][];
		for (int m = 0; m < UserNumber; m++) {
			int uid = userList.get(m);
			if (user_location.containsKey(uid)) {
				int N = user_location.get(uid).size();// 获取文档中不同单词的个数
				r[m] = new int[N];
				for (int n = 0; n < N; n++) {
					int initRegion = location_region.get(user_location.get(uid).get(n));
					r[m][n] = initRegion;// 随机分配一个主题给单词
					userAreaCount[m][initRegion]++;// 累加随机分配主题的单词个数
					areaItemCount[initRegion][region[m][n]]++;// 累加分配给主题K文档m中的单词数
					areaItemCountSum[initRegion]++;// 累加分配给主题的单词总数
				}
				userAreaCountSum[m] = N;// m文档中主题总数
			}
		}

		for (int i = 0; i < areaItemCount.length; i++) {

			Point u = new Point();
			double[] covariance = new double[2];
			for (int j = 0; j < areaItemCount[i].length; j++) {
				if (areaItemCount[i][j] > 0) {
					int index = indexToLocaMap.get(j);
					u.x += areaItemCount[i][j] * location_map.get(index).x;
					u.y += areaItemCount[i][j] * location_map.get(index).y;
				}
			}

			u.x = u.x / areaItemCountSum[i];
			u.y = u.y / areaItemCountSum[i];

			mu[i] = u;

			System.out.println("Innitilzied mu " + i + ":" + u.x + "," + u.y);

			for (int j = 0; j < areaItemCount[i].length; j++) {
				if (areaItemCount[i][j] > 0) {
					int index = indexToLocaMap.get(j);
					covariance[0] += (areaItemCount[i][j]) * (location_map.get(index).getX() - u.x)
							* (location_map.get(index).getX() - u.x);
					covariance[1] += (areaItemCount[i][j]) * (location_map.get(index).getY() - u.y)
							* (location_map.get(index).getY() - u.y);
					// covariance[0][1]+=(areaItemCount[i][j])*(location_map.get(j).getX()-u.x)*(location_map.get(j).getY()-u.y);
				}
			}

			covariance[0] /= areaItemCountSum[i];
			covariance[1] /= areaItemCountSum[i];

			this.covariance[i] = covariance;

			System.out.println("covariance " + i + ":" + this.covariance[i][0] + ":" + this.covariance[i][1]);

		}
		System.out.println("Initial is done");
	}

	public void inferenceModel() throws IOException {
		for (int it = 0; it < ITERATIONS; it++) {

			System.out.println("Iteration " + it);

			if ((it >= BURN_IN) && (((it - BURN_IN) % SAMPLE_LAG) == 0)) {
				System.out.println("Saving model at iteration " + it + " ... ");
				updateEstimatedParameters();
				updateEstimatedParametersRegion();
				// saveIteratedModel(it);
			}

			for (int m = 0; m < UserNumber; m++) {
				int uid = userList.get(m);
				if (user_content.containsKey(uid)) {
					for (int i = 0; i < HourNumber; i++) {
						if (user_content.get(uid).containsKey(i)) {
							int N = user_content.get(uid).get(i).size();
							for (int n = 0; n < N; n++) {
								// Sample from p(z_i|z_-i, w)
								int newTopic = sampleTopicZ(m, i, n);
								z[m][i][n] = newTopic;
							}
						}
					}
				}
			}

			for (int m = 0; m < UserNumber; m++) {
				int uid = userList.get(m);
				if (user_location.containsKey(uid)) {
					int N = user_location.get(uid).size();
					for (int n = 0; n < N; n++) {
						int newRegion = sampleTopicR(m, n);
						r[m][n] = newRegion;
					}
				}
			}

			for (int i = 0; i < areaItemCount.length; i++) {

				Point u = new Point();
				double[] covariance = new double[2];
				for (int j = 0; j < areaItemCount[i].length; j++) {
					if (areaItemCount[i][j] > 0) {
						int index = indexToLocaMap.get(j);
						u.x += areaItemCount[i][j] * location_map.get(index).x;
						u.y += areaItemCount[i][j] * location_map.get(index).y;
					}
				}

				u.x = u.x / areaItemCountSum[i];
				u.y = u.y / areaItemCountSum[i];

				this.mu[i] = u;

				for (int j = 0; j < areaItemCount[i].length; j++) {
					if (areaItemCount[i][j] > 0) {
						int index = indexToLocaMap.get(j);
						covariance[0] += areaItemCount[i][j] * (location_map.get(index).getX() - u.x)
								* (location_map.get(index).getX() - u.x);
						covariance[1] += areaItemCount[i][j] * (location_map.get(index).getY() - u.y)
								* (location_map.get(index).getY() - u.y);
					}
				}

				covariance[0] /= areaItemCountSum[i];
				covariance[1] /= areaItemCountSum[i];

				this.covariance[i] = covariance;
				System.out.println("Area " + i + ":" + u.x + "," + u.y);
				System.out.println("Variance " + i + ":" + covariance[0] + "," + covariance[1]);

			}
		}
		System.out.println("Inference done");
	}

	public void updateEstimatedParameters() {
		for (int k = 0; k < TopicNumber; k++) {
			for (int t = 0; t < topic_dictionary.size(); t++) {
				topicItemDistribution[k][t] = (topicItemCount[k][t] + 0.1)
						/ (topicItemCountSum[k] + topic_dictionary.size() * 0.1);
			}
		}

		for (int m = 0; m < UserNumber; m++) {
			for (int h = 0; h < HourNumber; h++) {
				for (int k = 0; k < TopicNumber; k++) {
					userTopicDistribution[m][h][k] = (userTopicCount[m][h][k] + alpha)
							/ (userTopicCountSum[m][h] + TopicNumber * alpha);
				}
			}
		}
	}

	public void updateEstimatedParametersRegion() {
		for (int k = 0; k < RegionNumber; k++) {
			for (int t = 0; t < LocationNumber; t++) {
				areaItemDistribution[k][t] = (areaItemCount[k][t] + 0.1) / (areaItemCountSum[k] + LocationNumber * 0.1);
			}
		}

		for (int m = 0; m < UserNumber; m++) {
			for (int k = 0; k < RegionNumber; k++) {
				userAreaDistribution[m][k] = (userAreaCount[m][k] + gamma)
						/ (userAreaCountSum[m] + RegionNumber * gamma);
			}
		}
	}

	public int sampleTopicR(int m, int n) {

		int oldRegion = r[m][n];
		userAreaCount[m][oldRegion]--;
		areaItemCount[oldRegion][region[m][n]]--;
		userAreaCountSum[m]--;
		areaItemCountSum[oldRegion]--;

		double[] p = new double[RegionNumber];
		for (int k = 0; k < RegionNumber; k++) {
			p[k] = (areaItemCount[k][region[m][n]] + 0.1) / (areaItemCountSum[k] + LocationNumber * 0.1)
					* (userAreaCount[m][k] + gamma) / (userAreaCountSum[m] + RegionNumber * gamma);
		}

		for (int k = 1; k < RegionNumber; k++) {
			p[k] += p[k - 1];
		}
		double u = Math.random() * p[RegionNumber - 1];
		int newRegion;
		for (newRegion = 0; newRegion < RegionNumber; newRegion++) {
			if (u < p[newRegion]) {
				break;
			}
		}

		userAreaCount[m][newRegion]++;
		areaItemCount[newRegion][region[m][n]]++;
		userAreaCountSum[m]++;
		areaItemCountSum[newRegion]++;

		return newRegion;
	}

	public int sampleTopicZ(int m, int h, int n) {

		int oldTopic = z[m][h][n];
		userTopicCount[m][h][oldTopic]--;
		topicItemCount[oldTopic][user[m][h][n]]--;
		userTopicCountSum[m][h]--;
		topicItemCountSum[oldTopic]--;

		double[] p = new double[TopicNumber];
		for (int k = 0; k < TopicNumber; k++) {
			p[k] = (topicItemCount[k][user[m][h][n]] + 0.1) / (topicItemCountSum[k] + topic_dictionary.size() * 0.1)
					* (userTopicCount[m][h][k] + alpha) / (userTopicCountSum[m][h] + TopicNumber * alpha);
		}

		for (int k = 1; k < TopicNumber; k++) {
			p[k] += p[k - 1];
		}
		double u = Math.random() * p[TopicNumber - 1];
		int newTopic;
		for (newTopic = 0; newTopic < TopicNumber; newTopic++) {
			if (u < p[newTopic]) {
				break;
			}
		}

		userTopicCount[m][h][newTopic]++;
		topicItemCount[newTopic][user[m][h][n]]++;
		userTopicCountSum[m][h]++;
		topicItemCountSum[newTopic]++;

		return newTopic;
	}

	public double GaussianProbability(Point p, int x) {
		double probability;

		if (this.covariance[x][0] == 0 && this.covariance[x][1] == 0) {
			return 0;
		} else if (this.covariance[x][0] == 0 || this.covariance[x][1] == 0) {
			this.covariance[x][0] = (this.covariance[x][0] + this.covariance[x][1]) / 2;
			this.covariance[x][1] = this.covariance[x][0];
		}

		double normalization = 1.0 / (2 * Math.sqrt(this.covariance[x][0]) * Math.sqrt(this.covariance[x][1]));

		double X = (p.getX() - this.mu[x].getX());
		double Y = (p.getY() - this.mu[x].getY());

		double body = (X * X) / (-2 * this.covariance[x][0]) + (Y * Y) / (-2 * this.covariance[x][1]);

		double main = Math.exp(body);

		probability = normalization * main;

		return probability;
	}

	public double GaussianProbability(Point p, double[] covariance, Point center) {
		double probability;

		if (covariance[0] == 0 && covariance[1] == 0) {
			return 0;
		} else if (covariance[0] == 0 || covariance[1] == 0) {
			covariance[0] = (covariance[0] + covariance[1]) / 2;
			covariance[1] = covariance[0];
		}

		double normalization = 1.0 / (2 * Math.sqrt(covariance[0]) * Math.sqrt(covariance[1]));

		double X = (p.getX() - center.getX());
		double Y = (p.getY() - center.getY());

		double body = (X * X) / (-2 * covariance[0]) + (Y * Y) / (-2 * covariance[1]);

		double main = Math.exp(body);

		probability = normalization * main;

		return probability;
	}

	// public void saveIteratedModel(int iters) throws IOException {
	// String resPath = "F:\\WorkSpace\\JIM\\JIM\\input\\MidOutput\\";
	// String modelName = "STC-" + iters;
	//
	// BufferedWriter writer = new BufferedWriter(new FileWriter(resPath + modelName
	// + ".topicItem"));
	// for (int i = 0; i < TopicNumber; i++) {
	// for (int j = 0; j < topic_dictionary.size(); j++) {
	// writer.write(topicItemDistribution[i][j] + "\t");
	// }
	// writer.write("\n");
	// }
	// writer.close();
	//
	// writer = new BufferedWriter(new FileWriter(resPath + modelName +
	// ".userTopic"));
	// for (int i = 0; i < UserNumber; i++) {
	// for (int j = 0; j < TopicNumber; j++) {
	// writer.write(userTopicDistribution[i][j] + "\n");
	// }
	// writer.write("\n");
	// }
	// writer.close();
	// }

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

	public class TwordsComparable implements Comparator<Integer> {

		public double[] sortProb; // Store probability of each word in topic k

		public TwordsComparable(double[] sortProb) {
			this.sortProb = sortProb;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			if (sortProb[o1] > sortProb[o2])
				return -1;
			else if (sortProb[o1] < sortProb[o2])
				return 1;
			else
				return 0;
		}
	}

	public void ComputeHomeLocation() {

		for (int user : user_location.keySet()) {
			ArrayList<Point> li = new ArrayList<Point>();
			for (int each : user_location.get(user)) {
				Point p1 = location_map.get(each);
				li.add(p1);
			}

			Point p = getHomeLocation(li);
			home_location.put(user, p);// 统计用户去过的所有地点
		}

		System.out.println("Home Location Inference Finished.");
	}

	public Point getHomeLocation(ArrayList<Point> checkins) {
		Point home = new Point();
		int num = 1;

		HashMap<Double, HashMap<Double, ArrayList<Point>>> result = new HashMap<Double, HashMap<Double, ArrayList<Point>>>();
		for (Point each : checkins) {

			double x = getApproximation(each.getX(), num);
			double y = getApproximation(each.getY(), num);
			if (result.containsKey(x)) {

				if (result.get(x).containsKey(y)) {
					result.get(x).get(y).add(each);
					// int count=result.get(x).get(y)+1;
					// result.get(x).put(y, count);
				} else {
					ArrayList<Point> li = new ArrayList<Point>();
					li.add(each);
					result.get(x).put(y, li);
				}
			} else {
				HashMap<Double, ArrayList<Point>> ma = new HashMap<Double, ArrayList<Point>>();
				ArrayList<Point> li = new ArrayList<Point>();
				li.add(each);
				ma.put(y, li);
				result.put(x, ma);
			}
		}

		int max = 0;
		for (Double x : result.keySet()) {
			for (Double y : result.get(x).keySet()) {
				if (result.get(x).get(y).size() > max) {
					max = result.get(x).get(y).size();
					home.setX(x);
					home.setY(y);
				}
			}
		}

		double sum_x = 0;
		double sum_y = 0;
		if (result.containsKey(home.getX()) && result.get(home.getX()).containsKey(home.getY())) {
			for (Point each : result.get(home.getX()).get(home.getY())) {

				sum_x += each.getX();
				sum_y += each.getY();
			}
		}

		home.setX(sum_x / max);
		home.setY(sum_y / max);

		return home;
	}

	// compute distance based on latitude and longtude
	// kilometers x denotes weidu/latitude; y denotes longtude
	public double getDistance(Point a, Point b) {
		Double R = new Double(6371);
		Double dlat = (b.getX() - a.getX()) * Math.PI / 180;
		Double dlon = (b.getY() - a.getY()) * Math.PI / 180;
		Double aDouble = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(a.getX() * Math.PI / 180)
				* Math.cos(b.getX() * Math.PI / 180) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
		Double cDouble = 2 * Math.atan2(Math.sqrt(aDouble), Math.sqrt(1 - aDouble));
		double d = Math.round((R * cDouble) * 1000) / 1000;
		return d;
	}

	public double getApproximation(double ori, int num) {

		BigDecimal b = new BigDecimal(ori);
		double f1 = b.setScale(num, BigDecimal.ROUND_HALF_UP).doubleValue();

		return f1;
	}

	public HashSet<Integer> getLocationInDistance(Point p) {
		HashSet<Integer> result = new HashSet<>();

		for (int lid : location_map.keySet()) {
			double d = getDistance(location_map.get(lid), p);
			if (d <= distance)
				result.add(lid);
		}

		return result;
	}

	public HashSet<Integer> getHomeLocationInDistance(int uid) {
		HashSet<Integer> result = new HashSet<>();
		Point p = home_location.get(uid);
		for (int lid : location_map.keySet()) {
			double d = getDistance(location_map.get(lid), p);
			if (d <= distanceHome)
				result.add(lid);
		}

		return result;
	}

	public Point getLocaPre(Point p) {
		HashSet<Integer> loca = getLocationInDistance(p);

		double x = 0d;
		double y = 0d;
		for (int i : loca) {
			Point point = location_map.get(i);

			x += point.x;
			y += point.y;
		}

		Point pp = new Point();
		pp.x = x / (double) loca.size();
		pp.y = y / (double) loca.size();

		double[] covariance = new double[2];

		for (int i : loca) {
			covariance[0] += (location_map.get(i).getX() - pp.x) * (location_map.get(i).getX() - pp.x);
			covariance[1] += (location_map.get(i).getY() - pp.y) * (location_map.get(i).getY() - pp.y);
		}

		covariance[0] /= loca.size();
		covariance[1] /= loca.size();

		double pro = 0d;
		Point re = pp;
		for (int i : loca) {
			double v = GaussianProbability(location_map.get(i), covariance, pp);
			if (v > pro) {
				pro = v;
				re = location_map.get(i);
			}
		}

		return re;
	}

	public void Result() throws Exception {
		// BufferedReader reader = new BufferedReader(
		// new InputStreamReader(new
		// FileInputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\JIMTEST5.txt")));
		// BufferedWriter wr0 = new BufferedWriter(new OutputStreamWriter(
		// new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\RESULTJIM.txt"),
		// "utf-8"));
		// BufferedWriter wr1 = new BufferedWriter(
		// new OutputStreamWriter(new
		// FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\theta.txt"), "utf-8"));
		//
		// BufferedWriter wr2 = new BufferedWriter(
		// new OutputStreamWriter(new
		// FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\phi.txt"), "utf-8"));
		// String line = reader.readLine();
		int re_count = 0;
		int sum1 = 0;
		int sum5 = 0;
		int sum10 = 0;
		int sum20 = 0;

		int re_countOut = 0;
		int sum1Out = 0;
		int sum5Out = 0;
		int sum10Out = 0;
		int sum20Out = 0;

		HashMap<Integer, Set<Integer>> timemap = TimeAndLoca.timeandloca();
		BufferedWriter wrm = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\temp.txt"), "utf-8"));
		//
		// TreeMap<Integer, Integer> userSum = new TreeMap<>();// 统计每个用户的记录数
		// TreeMap<Integer, Integer> userHit = new TreeMap<>();

		// while (line != null) {
		for (String line : predictData) {
			String[] info = line.split("--");
			int u_id = Integer.valueOf(info[0]);
			int loca_id = Integer.valueOf(info[3]);
			double lat = Double.valueOf(info[1]);
			double lng = Double.valueOf(info[2]);
			int hour = Integer.parseInt(info[4].split(" ")[1].split(":")[0]);

			// if (!userHit.containsKey(u_id))
			// userHit.put(u_id, 0);

			Point p = new Point(lat, lng, loca_id);
			HashSet<Integer> homeloca = getHomeLocationInDistance(u_id);

			if (homeloca.contains(loca_id)) {
				// 统计每个用户的次数
				// if (userSum.containsKey(u_id)) {
				// userSum.put(u_id, userSum.get(u_id) + 1);
				// } else {
				// userSum.put(u_id, 1);
				// }

				re_count++;// 统计home的数量
				if (re_count % 100 == 0)
					System.out.println("line =" + re_count + "in result");

				// 记录排名
				HashMap<Integer, Double> rank = new HashMap<Integer, Double>();

				HashSet<Integer> locaindis = getLocationInDistance(p);

				wrm.write(loca_id + " " + locaindis.size() + "\n");

				int userindex = userToIndex.get(u_id);

				for (int reg = 0; reg < RegionNumber; reg++) {
					for (int j = 0; j < areaItemCount[reg].length; j++) {
						int lid = indexToLocaMap.get(j);
						if (timemap.get(hour).contains(lid)) {
							if (locaindis.contains(lid)) {
								Point lv = location_map.get(lid);
								double lp = userAreaDistribution[userindex][reg] * areaItemDistribution[reg][j];
								double plp = GaussianProbability(p, reg);
								double plv = GaussianProbability(lv, reg);
								if (location_content.containsKey(lid)) {
									String[] contents = location_content.get(lid).split(",");
									// String contents = location_content.get(lid);
									for (int t = 0; t < TopicNumber; t++) {
										double utc = 1d;
										for (String s : contents) {
											// System.out.println(contents);
											int index = topic_dictionary.get(s);
											int uid = userToIndex.get(u_id);
											utc = userTopicDistribution[uid][hour][t] * topicItemDistribution[t][index];
										}
										// utc = utc / contents.length;
										// tvc = tvc / contents.length;
										utc = Math.pow(utc, 1 / contents.length);

										// double value = utc * lp * tvc;
										// double value = utc * lp;
										// double value = lp * tvc * plv * plp;
										// double value = utc;
										double value = utc * lp * plv * plp;

										if (rank.containsKey(lid)) {
											double v = rank.get(lid);
											if (value > v) {
												rank.put(lid, value);
											}
										} else {
											rank.put(lid, value);
										}
									}
								}
							}
						}
					}
				}

				rank = (HashMap<Integer, Double>) sortByValue(rank);
				Iterator<Integer> iter = rank.keySet().iterator();
				ArrayList<Integer> list = new ArrayList<>();
				int count = 0;
				// wr0.write(u_id + " ");
				while (iter.hasNext()) {
					int key = iter.next();
					list.add(key);
					// Double value = rank.get(key);
					// wr0.write("(" + key + "," + value + ") ");
					count++;
					if (count >= 20)
						break;
				}
				// wr0.write("\n");

				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) == loca_id) {
						if (i < 1) {
							sum1++;
							sum5++;
							sum10++;
							sum20++;
							// userHit.put(u_id, userHit.get(u_id) + 1);
						} else if (i >= 1 && i < 5) {
							sum5++;
							sum10++;
							sum20++;
						} else if (i > 4 && i <= 9) {
							sum10++;
							sum20++;
						} else {
							sum20++;
						}
					}
				}
			} else {
				re_countOut++;// 统计home的数量
				if (re_countOut % 100 == 0)
					System.out.println("Outline =" + re_countOut + "in result");

				// 记录排名
				HashMap<Integer, Double> rankOut = new HashMap<Integer, Double>();

				HashSet<Integer> locaindis = getLocationInDistance(p);
				int userindex = userToIndex.get(u_id);

				for (int reg = 0; reg < RegionNumber; reg++) {
					for (int j = 0; j < areaItemCount[reg].length; j++) {
						int lid = indexToLocaMap.get(j);
						if (timemap.get(hour).contains(lid)) {
							if (locaindis.contains(lid)) {
								Point lv = location_map.get(lid);
								double lp = userAreaDistribution[userindex][reg] * areaItemDistribution[reg][j];
								double plp = GaussianProbability(p, reg);
								double plv = GaussianProbability(lv, reg);
								if (location_content.containsKey(lid)) {
									String[] contents = location_content.get(lid).split(",");
									// String contents = location_content.get(lid);
									for (int t = 0; t < TopicNumber; t++) {
										double utc = 1d;
										for (String s : contents) {
											// System.out.println(contents);
											int index = topic_dictionary.get(s);
											int uid = userToIndex.get(u_id);
											utc = userTopicDistribution[uid][hour][t] * topicItemDistribution[t][index];
										}
										// utc = utc / contents.length;
										// tvc = tvc / contents.length;
										utc = Math.pow(utc, 1 / contents.length);

										// double value = utc * lp * tvc;
										// double value = utc * lp;
										// double value = lp * tvc * plv * plp;
										// double value = utc;
										double value = utc * lp * plv * plp;

										if (rankOut.containsKey(lid)) {
											double v = rankOut.get(lid);
											if (value > v) {
												rankOut.put(lid, value);
											}
										} else {
											rankOut.put(lid, value);
										}
									}
								}
							}
						}
					}
				}

				rankOut = (HashMap<Integer, Double>) sortByValue(rankOut);
				Iterator<Integer> iter = rankOut.keySet().iterator();
				ArrayList<Integer> list = new ArrayList<>();
				int count = 0;
				// wr0.write(u_id + " ");
				while (iter.hasNext()) {
					int key = iter.next();
					list.add(key);
					// Double value = rankOut.get(key);
					// wr0.write("(" + key + "," + value + ") ");
					count++;
					if (count >= 20)
						break;
				}
				// wr0.write("\n");

				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) == loca_id) {
						if (i < 1) {
							sum1Out++;
							sum5Out++;
							sum10Out++;
							sum20Out++;
							// userHit.put(u_id, userHit.get(u_id) + 1);
						} else if (i >= 1 && i < 5) {
							sum5Out++;
							sum10Out++;
							sum20Out++;
						} else if (i > 4 && i <= 9) {
							sum10Out++;
							sum20Out++;
						} else {
							sum20Out++;
						}
					}
				}

			}
			// line = reader.readLine();
		}
		// int indexfor24 = userToIndex.get(24);
		//
		// for (int j = 0; j < userTopicDistribution[indexfor24].length; j++) {
		// wr1.write(userTopicDistribution[indexfor24][j] + " ");
		// }
		// wr1.write("\n");
		//
		// for (int i = 0; i < TopicNumber; i++) {
		// List<Integer> tWordsIndexArray = new ArrayList<Integer>();
		// for (int h = 0; h < topic_dictionary.size(); h++) {
		// tWordsIndexArray.add(new Integer(h));
		// }
		// Collections.sort(tWordsIndexArray, new
		// STC.TwordsComparable(topicItemDistribution[i]));
		// wr2.write("topic " + i + "\t:\n");
		// for (int t = 0; t < 10; t++) {
		// wr2.write(indexToTermMap.get(tWordsIndexArray.get(t)) + " "
		// + topicItemDistribution[i][tWordsIndexArray.get(t)] + "\n");
		// }
		// wr2.write("\n");
		// }
		// wr0.flush();
		// wr0.close();
		// wr1.flush();
		// wr1.close();
		// wr2.flush();
		// wr2.close();
		// reader.close();

		wrm.flush();
		wrm.close();

		// for (Entry<Integer, Integer> entry : userSum.entrySet()) {
		// int id = entry.getKey();
		// System.out.println(id + " " + entry.getValue() + " "
		// + Double.valueOf(userHit.get(id)) / Double.valueOf(entry.getValue()));
		// }
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\RESULTJIMIHome.txt"), "utf-8"));

		wr.write(1 + " " + sum1 + " " + Double.valueOf(sum1) / Double.valueOf(re_count) + "\n");
		wr.write(5 + " " + sum5 + " " + Double.valueOf(sum5) / Double.valueOf(re_count) + "\n");
		wr.write(10 + " " + sum10 + " " + Double.valueOf(sum10) / Double.valueOf(re_count) + "\n");
		wr.write(20 + " " + sum20 + " " + Double.valueOf(sum20) / Double.valueOf(re_count) + "\n");
		wr.write(re_count + "\n");
		wr.flush();
		wr.close();

		BufferedWriter wro = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\RESULTJIMIOut.txt"), "utf-8"));

		wro.write(1 + " " + sum1Out + " " + Double.valueOf(sum1Out) / Double.valueOf(re_countOut) + "\n");
		wro.write(5 + " " + sum5Out + " " + Double.valueOf(sum5Out) / Double.valueOf(re_countOut) + "\n");
		wro.write(10 + " " + sum10Out + " " + Double.valueOf(sum10Out) / Double.valueOf(re_countOut) + "\n");
		wro.write(20 + " " + sum20Out + " " + Double.valueOf(sum20Out) / Double.valueOf(re_countOut) + "\n");
		wro.write(re_countOut + "\n");
		wro.flush();
		wro.close();

		// System.out.println(1 + " " + sum1 + " " + Double.valueOf(sum1) /
		// Double.valueOf(re_count));
		// System.out.println(5 + " " + sum5 + " " + Double.valueOf(sum5) /
		// Double.valueOf(re_count));
		// System.out.println(10 + " " + sum10 + " " + Double.valueOf(sum10) /
		// Double.valueOf(re_count));
		// System.out.println(20 + " " + sum20 + " " + Double.valueOf(sum20) /
		// Double.valueOf(re_count));
		// System.out.println(re_count);

	}

	public static void main(String[] args) throws Exception {
		STCI stci = new STCI();
		// Point a=new Point(40.7505163, 73.9934993, 11);
		// Point b=new Point(34.098, 118.328395, 14);
		// System.out.println(stc.getDistance(a, b));
		stci.readTrain();
		stci.readPredict();
		stci.TopicDictionaryMaker();
		stci.RegionLocationMaker();
		stci.ComputeHomeLocation();
		stci.InitialState();
		// stc.GibbsSampling();
		// stc.Gibbs();
		stci.inferenceModel();
		// stc.CalDistribution();
		stci.Result();
	}
}
