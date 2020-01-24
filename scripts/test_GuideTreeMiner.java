System.out.println();
System.out.println("--------------------------------------------------------");

// https://svn.win.tue.nl/repos/prom/Packages/GuideTreeMiner/Trunk/src/org/processmining/plugins/guidetreeminer/MineGuideTree.java

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.guidetreeminer.algorithm.AgglomerativeHierarchicalClustering;
import org.processmining.plugins.guidetreeminer.distance.EuclideanDistance;
import org.processmining.plugins.guidetreeminer.distance.GenericEditDistance;
import org.processmining.plugins.guidetreeminer.distance.LevenshteinDistance;
import org.processmining.plugins.guidetreeminer.encoding.ActivityOverFlowException;
import org.processmining.plugins.guidetreeminer.encoding.EncodeActivitySet;
import org.processmining.plugins.guidetreeminer.encoding.EncodeTraces;
import org.processmining.plugins.guidetreeminer.encoding.EncodingNotFoundException;
import org.processmining.plugins.guidetreeminer.featureextraction.FeatureExtraction;
import org.processmining.plugins.guidetreeminer.featureextraction.FeatureMatrix;
import org.processmining.plugins.guidetreeminer.featureextraction.FeatureSelection;
import org.processmining.plugins.guidetreeminer.scoringmatrices.IndelSubstitutionMatrix;
import org.processmining.plugins.guidetreeminer.similarity.FScoreSimilarity;
import org.processmining.plugins.guidetreeminer.tree.GuideTree;
import org.processmining.plugins.guidetreeminer.types.DistanceMetricType;
import org.processmining.plugins.guidetreeminer.types.GTMFeature;
import org.processmining.plugins.guidetreeminer.types.GTMFeatureType;
import org.processmining.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.processmining.plugins.guidetreeminer.types.Normalization;
import org.processmining.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.processmining.plugins.guidetreeminer.util.EquivalenceClass;
import org.processmining.plugins.guidetreeminer.util.FileIO;

// CLASS MEMBERS
	// The guide tree to output
	GuideTree guideTree;

	int encodingLength;
    int averageTraceLength;

	//Map<String, String> charActivityMap = new HashMap<String, String>();
	Map charActivityMap;
	//Map<String, String> activityCharMap;
	Map activityCharMap;
	//List<String> encodedTraceList;
	List encodedTraceList;
    //List<String> uniqueEncodedTraceList;
    List uniqueEncodedTraceList;
	//Set<String> duplicateTraceSet;
	Set duplicateTraceSet;
	//Map<String, TreeSet<Integer>> encodedTraceIdenticalIndicesMap;
	Map encodedTraceIdenticalIndicesMap;

	//Map<String, Integer> encodedActivityCountMap,
    //Map<String, Integer> encodedActivityUniqueTraceCountMap;
	Map encodedActivityCountMap;
    Map encodedActivityUniqueTraceCountMap;

	float[][] similarityDistanceMatrix;
	//ClusterLogOutput clusterLogOutput;

	AgglomerativeHierarchicalClustering ahc;

// CLASS METHODS
	void encodeLog() {
		/*
			* traceIndex is used to keep track of the number of traces;
			* totalNoEvents counts the number of events/activities in the entire
			* event log. activitySet accumulates the set of distinct
			* activities/events in the event log; it doesn't store the trace
			* identifier for encoding; Encoding trace identifier is required only
			* when any of the maximal repeat (alphabet) features is selected
			*/

		int traceIndex = 0;
		int totalNoEvents = 0;
		//Set<String> activitySet = new HashSet<String>();
		Set activitySet = new HashSet();
		XAttributeMap attributeMap;
		//Set<String> eventTypeSet = new HashSet<String>();
		Set eventTypeSet = new HashSet();

		for (XTrace trace : log) {
			totalNoEvents += trace.size();
			for (XEvent event : trace) {
				attributeMap = event.getAttributes();
				if(!attributeMap.containsKey("concept:name"))
					continue;
				activitySet.add(attributeMap.get("concept:name").toString()
						+ "-"
						+ attributeMap.get("lifecycle:transition").toString());
				eventTypeSet.add(attributeMap.get("lifecycle:transition")
						.toString());
			}
		}

		averageTraceLength = totalNoEvents / log.size();
		//encodedTraceIdenticalIndicesMap = new HashMap<String, TreeSet<Integer>>();
		encodedTraceIdenticalIndicesMap = new HashMap();
		try {
			EncodeActivitySet encodeActivitySet = new EncodeActivitySet(
					activitySet);
			encodingLength = encodeActivitySet.getEncodingLength();

			activityCharMap = encodeActivitySet.getActivityCharMap();
			charActivityMap = encodeActivitySet.getCharActivityMap();
			/*
				* Encode each trace to a charStream
				*/
			EncodeTraces encodeTraces = new EncodeTraces(activityCharMap, log);
			encodedTraceList = encodeTraces.getCharStreamList();

			//uniqueEncodedTraceList = new ArrayList<String>();
			uniqueEncodedTraceList = new ArrayList();

			traceIndex = 0;
			//TreeSet<Integer> encodedTraceIdenticalIndicesSet;
			TreeSet encodedTraceIdenticalIndicesSet;
			for (String encodedTrace : encodedTraceList) {
				if (encodedTraceIdenticalIndicesMap.containsKey(encodedTrace)) {
					encodedTraceIdenticalIndicesSet = encodedTraceIdenticalIndicesMap
							.get(encodedTrace);
				} else {
					//encodedTraceIdenticalIndicesSet = new TreeSet<Integer>();
					encodedTraceIdenticalIndicesSet = new TreeSet();
					uniqueEncodedTraceList.add(encodedTrace);
				}
				encodedTraceIdenticalIndicesSet.add(traceIndex);
				encodedTraceIdenticalIndicesMap.put(encodedTrace,
						encodedTraceIdenticalIndicesSet);

				traceIndex++;
			}
			//duplicateTraceSet = new HashSet<String>();
			duplicateTraceSet = new HashSet();
			for (String encodedTrace : encodedTraceIdenticalIndicesMap.keySet()) {
				if (encodedTraceIdenticalIndicesMap.get(encodedTrace).size() > 1)
					duplicateTraceSet.add(encodedTrace);
			}

			//encodedActivityCountMap = new HashMap<String, Integer>();
			encodedActivityCountMap = new HashMap();
			//encodedActivityUniqueTraceCountMap = new HashMap<String, Integer>();
			encodedActivityUniqueTraceCountMap = new HashMap();
			int traceLength, noIdenticalTraces, count;
			String encodedActivity;

			for (String encodedTrace : uniqueEncodedTraceList) {
				noIdenticalTraces = encodedTraceIdenticalIndicesMap.get(
						encodedTrace).size();
				traceLength = encodedTrace.length() / encodingLength;

				for (int i = 0; i < traceLength; i++) {
					encodedActivity = encodedTrace.substring(
							i * encodingLength, (i + 1) * encodingLength);
					count = noIdenticalTraces;
					if (encodedActivityCountMap.containsKey(encodedActivity)) {
						count += encodedActivityCountMap.get(encodedActivity);
					}
					encodedActivityCountMap.put(encodedActivity, count);

					count = 1;
					if (encodedActivityUniqueTraceCountMap
							.containsKey(encodedActivity)) {
						count += encodedActivityUniqueTraceCountMap
								.get(encodedActivity);
					}
					encodedActivityUniqueTraceCountMap.put(encodedActivity,
							count);
				}
			}
		} catch (ActivityOverFlowException e) {
			e.printStackTrace();
		} catch (EncodingNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	public GuideTree getGuideTree() {
		return guideTree;
	}
	*/

// Main function : MineGuideTree.mine

// @param PluginContext         context
// Omitted, no need for GUI

// @param GuideTreeMinerInput   input
// Omitted, hard-coded
import org.processmining.plugins.guidetreeminer.types.AHCJoinType;
AHCJoinType ahcJoinType = AHCJoinType.MinVariance;

int noClusters = 5;

// @param XLog                  log
log = open_xes_log_file("../../data/wabo.xes");

encodeLog();

System.out.println("*** break point L213 ***");
exit();

IndelSubstitutionMatrix indelSubstitutionMatrix = new IndelSubstitutionMatrix(
	encodingLength, encodedTraceList);
//Map<String, Integer> substitutionScoreMap = indelSubstitutionMatrix
//	.getSubstitutionScoreMap();
Map substitutionScoreMap = indelSubstitutionMatrix
    .getSubstitutionScoreMap();
//Map<String, Integer> indelRightGivenLeftScoreMap = indelSubstitutionMatrix
//	.getIndelRightGivenLeftScoreMap();
Map indelRightGivenLeftScoreMap = indelSubstitutionMatrix
    .getIndelRightGivenLeftScoreMap();
	
FileIO io = new FileIO();
String tempDir = System.getProperty("java.io.tmpdir");

io.writeToFile(tempDir, "substitutionScoreMap.txt", substitutionScoreMap, "\\^");
io.writeToFile(tempDir, "indelScoreMap.txt", indelRightGivenLeftScoreMap, "\\^");
GenericEditDistance ged = new GenericEditDistance(
	encodingLength, uniqueEncodedTraceList,
	substitutionScoreMap, indelRightGivenLeftScoreMap, 4, 1);
similarityDistanceMatrix = ged.getSimilarityMatrix();

// Perform the agglomerative hierarchical clustering
ahc = new AgglomerativeHierarchicalClustering(
	uniqueEncodedTraceList, similarityDistanceMatrix,
	SimilarityDistanceMetricType.Similarity, ahcJoinType);

guideTree = ahc.getGuideTree();
guideTree.setEncodedTraceIdenticalIndicesSetMap(encodedTraceIdenticalIndicesMap);
guideTree.setEncodingLength(encodingLength);
guideTree.setEncodedTraceList(encodedTraceList);
guideTree.setLog(log);
guideTree.setActivityCharMap(activityCharMap);
guideTree.setCharActivityMap(charActivityMap);


//List<XLog> clusterLogList = new ArrayList<XLog>();
List clusterLogList = new ArrayList();

//List<List<String>> clusterEncodedTraceList = guideTree.getClusters(noClusters);
List clusterEncodedTraceList = guideTree.getClusters(noClusters);
//Set<Integer> identicalTraceSet;
Set identicalTraceSet;
//List<String> encodedTraceList;
List encodedTraceList;
XLog currentClusterLog;

for (int i = 0; i < noClusters; i++){
	currentClusterLog = new XLogImpl(log.getAttributes());
	encodedTraceList = clusterEncodedTraceList.get(i);

	for (String encodedTrace : encodedTraceList) {
		if (encodedTraceIdenticalIndicesMap.containsKey(encodedTrace)) {
			identicalTraceSet = encodedTraceIdenticalIndicesMap
				.get(encodedTrace);
			
			for (Integer traceIndex : identicalTraceSet) {
				currentClusterLog.add(log.get(traceIndex));
			}
		} else {
			context.log("EncodedTrace Not Found");
		}
	}
	clusterLogList.add(currentClusterLog);
}
//clusterLogOutput = new ClusterLogOutput(noClusters, clusterLogList);

System.out.println("--------------------------------------------------------");
exit()

