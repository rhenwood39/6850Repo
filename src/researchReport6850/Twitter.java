package researchReport6850;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Twitter {


	private static final String CURRENT_FILE_PATH = new File("").getAbsolutePath();

	private static final String FEAT_DIR = CURRENT_FILE_PATH + "/twitter2.0/feat";
	private static final String FEATNAME_DIR = CURRENT_FILE_PATH + "/twitter2.0/featnames";
	private static final String SAME_WORD_EDGES = CURRENT_FILE_PATH + "/twitter2.0/sameWordEdges.txt";
	private static final String LEFT_SEED = "#obama2012";
	private static final String RIGHT_SEED =  "#teaparty";

	// update counts and add new words to cand
	public static int addAll(HashSet<String> seedWords, HashMap<String, Integer> cand, HashSet<String> words) {
		int count = 0;
		for (String word : words) {
			if (seedWords.contains(word)) {
				count++;
			} else {
				if (!cand.containsKey(word))
					cand.put(word, 0);
				cand.put(word, cand.get(word) + 1);
			}
		}
		return count;
	}
	
	public static void main(String[] args) throws Exception {	
		////////////////////////////////////////////////////////////////////////
		// Find intersections of Seed word sets and other word sets
		////////////////////////////////////////////////////////////////////////
		
		// seed words
		HashSet<String> leftWords = new HashSet<>();
		HashSet<String> rightWords = new HashSet<>();
		int lCount = 0;
		int rCount = 0;
		leftWords.add(LEFT_SEED);
		rightWords.add(RIGHT_SEED);
		
		// candidate words
		HashMap<String, Integer> leftCand = new HashMap<>();
		HashMap<String, Integer> rightCand = new HashMap<>();
		
		File dir = new File(FEAT_DIR);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File feat : directoryListing) {
				// get corresponding featname file
				String featnamePath = FEATNAME_DIR + "/" + feat.getName() + "names";
				File featname = new File(featnamePath);
				
				// Construct a hashtagID -> hashtag map
				BufferedReader featnameReader = new BufferedReader(new FileReader(featname));
				String line;
				HashMap<String, String> hashID2hash = new HashMap<>();
				while ((line = featnameReader.readLine()) != null) {
					String array[] = line.split(" ");
					String hashID = array[0];
					String hashtag = array[1];
					if (hashtag.charAt(0) == '#')
						hashID2hash.put(hashID, hashtag.toLowerCase());
				}
				featnameReader.close();
				
				BufferedReader featReader = new BufferedReader(new FileReader(feat));
				while ((line = featReader.readLine()) != null) {
					// get id and feature array
					String array[] = line.split(" ");
					String featArray[] = Arrays.copyOfRange(array, 1, array.length);
					
					HashSet<String> myWords = new HashSet<>();

					// use feature array to find which hastags were used
					for (int i = 0; i < featArray.length; i++) {
						String key = "" + i;
						if (featArray[i].equals("1") && hashID2hash.containsKey(key)) {
							myWords.add(hashID2hash.get(key));
						}
					}
					
					if (myWords.contains(LEFT_SEED))
						lCount += addAll(leftWords, leftCand, myWords);
					if (myWords.contains(RIGHT_SEED))
						rCount += addAll(rightWords, rightCand, myWords);
				}
				featReader.close();
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////
		// Compute occurances of left Words
		/////////////////////////////////////////////////////////////////////////////
		HashMap<String, Integer> lCounts = new HashMap<>();
		HashMap<String, Integer> rCounts = new HashMap<>();
		for (String word : leftCand.keySet())
			lCounts.put(word, 0);
		for (String word : rightCand.keySet())
			rCounts.put(word, 0);
		
		if (directoryListing != null) {
			for (File feat : directoryListing) {
				// get corresponding featname file
				String featnamePath = FEATNAME_DIR + "/" + feat.getName() + "names";
				File featname = new File(featnamePath);
				
				// Construct a hashtagID -> hashtag map
				BufferedReader featnameReader = new BufferedReader(new FileReader(featname));
				String line;
				HashMap<String, String> hashID2hash = new HashMap<>();
				while ((line = featnameReader.readLine()) != null) {
					String array[] = line.split(" ");
					String hashID = array[0];
					String hashtag = array[1];
					if (hashtag.charAt(0) == '#')
						hashID2hash.put(hashID, hashtag.toLowerCase());
				}
				featnameReader.close();
				
				BufferedReader featReader = new BufferedReader(new FileReader(feat));
				while ((line = featReader.readLine()) != null) {
					// get id and feature array
					String array[] = line.split(" ");
					String featArray[] = Arrays.copyOfRange(array, 1, array.length);
					
					HashSet<String> myWords = new HashSet<>();

					// use feature array to find which hastags were used
					for (int i = 0; i < featArray.length; i++) {
						String key = "" + i;
						if (featArray[i].equals("1") && hashID2hash.containsKey(key)) {
							myWords.add(hashID2hash.get(key));
						}
					}
					
					for (String word : myWords) {
						if (lCounts.containsKey(word))
							lCounts.put(word, lCounts.get(word) + 1);
						if (rCounts.containsKey(word))
							rCounts.put(word, rCounts.get(word) + 1);
					}
				}
				featReader.close();
			}
			
			// treat words as left or right words if based on jaccard sim
			Set<String> lKeys = leftCand.keySet();
			for (String key : lKeys) {
				double jaccard = (1.0 * leftCand.get(key)) / (lCount + lCounts.get(key) - leftCand.get(key));
				if (jaccard >= 0.035) {
					leftWords.add(key);
				}
			}			
			
			Set<String> rKeys = rightCand.keySet();
			for (String key : rKeys) {
				double jaccard = (1.0 * rightCand.get(key)) / (rCount + rCounts.get(key) - rightCand.get(key));
				if (jaccard >= 0.035) {
					rightWords.add(key);
				}
			}
			
			System.out.println("Left word: " + leftWords);
			System.out.println("Right words: " + rightWords);
		}
		
		
		/////////////////////////////////////////////////////////////////////////////
		// compute similarity
		/////////////////////////////////////////////////////////////////////////////
		
		HashSet<String> liberals = new HashSet<>();
		HashSet<String> conservatives = new HashSet<>();
		
		// find set who used left words and set who used right words
		if (directoryListing != null) {
			for (File feat : directoryListing) {
				// get corresponding featname file
				String featnamePath = FEATNAME_DIR + "/" + feat.getName() + "names";
				File featname = new File(featnamePath);
				
				// Construct a hashtagID -> hashtag map
				BufferedReader featnameReader = new BufferedReader(new FileReader(featname));
				String line;
				HashMap<String, String> hashID2hash = new HashMap<>();
				while ((line = featnameReader.readLine()) != null) {
					String array[] = line.split(" ");
					String hashID = array[0];
					String hashtag = array[1];
					if (hashtag.charAt(0) == '#')
						hashID2hash.put(hashID, hashtag.toLowerCase());
				}
				featnameReader.close();
				
				BufferedReader featReader = new BufferedReader(new FileReader(feat));
				while ((line = featReader.readLine()) != null) {
					// get id and feature array
					String array[] = line.split(" ");
					String userID = array[0];
					String featArray[] = Arrays.copyOfRange(array, 1, array.length);
					
					HashSet<String> myWords = new HashSet<>();

					// use feature array to find which hastags were used
					for (int i = 0; i < featArray.length; i++) {
						String key = "" + i;
						if (featArray[i].equals("1") && hashID2hash.containsKey(key)) {
							myWords.add(hashID2hash.get(key));
						}
					}
					
					if (!Collections.disjoint(myWords, leftWords))
						liberals.add(userID);
					if (!Collections.disjoint(myWords, rightWords))
						conservatives.add(userID);
				}
				featReader.close();
			}
		}
		
		System.out.println();
		System.out.println("(no dup) Left words: " + LEFT_SEED);
		System.out.println(leftWords);
		System.out.println("(no dup) Right words: " + RIGHT_SEED);
		System.out.println(rightWords);
		System.out.println("num liberals: " + liberals.size());
		System.out.println("num conservatives: " + conservatives.size());
		
		HashSet<String> intersect = (HashSet<String>) liberals.clone();
		intersect.retainAll(conservatives);
		HashSet<String> union = (HashSet<String>) liberals.clone();
		union.addAll(conservatives);
		
		System.out.println();
		System.out.println("Intersection: " + intersect.size());
		System.out.println("Union: " + union.size());
	}
}

/*
 /////////////////////////////////////////////
		// Use poltical words to construct new graph
		/////////////////////////////////////////////
		HashMap<String, Set<String>> word2users = new HashMap<>();
		for (String word : leftWords.keySet())
			word2users.put(word, new HashSet<String>());
		for (String word : rightWords.keySet())
			word2users.put(word, new HashSet<String>());
		
		if (directoryListing != null) {
			for (File feat : directoryListing) {
				// get corresponding featname file
				String featnamePath = FEATNAME_DIR + "/" + feat.getName() + "names";
				File featname = new File(featnamePath);
				
				// Construct a hashtagID -> hashtag map
				BufferedReader featnameReader = new BufferedReader(new FileReader(featname));
				String line;
				HashMap<String, String> hashID2hash = new HashMap<>();
				while ((line = featnameReader.readLine()) != null) {
					String array[] = line.split(" ");
					String hashID = array[0];
					String hashtag = array[1];
					if (hashtag.charAt(0) == '#')
						hashID2hash.put(hashID, hashtag.toLowerCase());
				}
				featnameReader.close();
				
				BufferedReader featReader = new BufferedReader(new FileReader(feat));
				while ((line = featReader.readLine()) != null) {
					// get id and feature array
					String array[] = line.split(" ");
					String userID = array[0];
					String featArray[] = Arrays.copyOfRange(array, 1, array.length);
					
					// use feature array to find which hashtags were used
					for (int i = 0; i < featArray.length; i++) {
						String key = "" + i;
						if (featArray[i].equals("1") && hashID2hash.containsKey(key)) {
							String hashtag = hashID2hash.get(key);
							if (word2users.containsKey(hashtag))
								word2users.get(hashtag).add(userID);
						}
					}
				}
				featReader.close();
			}
		}	
		
		--------------------------------------------------------------------
HashMap<Integer, HashSet<Integer>> user2users = new HashMap<>();
for (Set<String> set : word2users.values()) {
	for (String user1 : set) {
		for (String user2 : set) {
			if (!user1.equals(user2)) {
				if (!user2users.containsKey(Integer.parseInt(user1))) {
					user2users.put(Integer.parseInt(user1), new HashSet<Integer>());
				}
				user2users.get(Integer.parseInt(user1)).add(Integer.parseInt(user2));
			}
		}
		//break;
	}
	//break;
}
PrintWriter writer = new PrintWriter(SAME_WORD_EDGES);
for (Entry<Integer, HashSet<Integer>> entry : user2users.entrySet()) {
	//System.out.println(entry);
	HashSet<Integer> values = entry.getValue();
	for (Integer user : values)
		writer.write(entry.getKey() + " " + user + "\n");
}
writer.close();
*/









