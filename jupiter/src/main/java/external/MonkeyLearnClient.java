package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnResponse;
import com.monkeylearn.MonkeyLearnException;

public class MonkeyLearnClient {
	
	private static final String API_KEY = "717d426a82ac830573508c6517464b764632e470";
	
	
	public static void main( String[] args ) throws MonkeyLearnException {
 
        String[] data = {
        		"Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look."};
        
        List<List<String>> words = extractKeywords(data);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
        
    }
	
	public static List<List<String>> extractKeywords(String[] text){
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}
		// use the API key from your account
		MonkeyLearn ml = new MonkeyLearn(API_KEY);
        
        String modelId = "ex_YCya9nrn";
        
        ExtraParam[] extraParams = {new ExtraParam("max_keywords","3")};
        
        MonkeyLearnResponse res;
		try {
			res = ml.extractors.extract(modelId, text, extraParams);
			JSONArray resultArray = res.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return null;
	}
	
	private static List<List<String>> getKeywords(JSONArray resultArray){
		List<List<String>> topKeywords = new ArrayList<>();
		// i represent 20 job descriptions
		for (int i = 0; i < resultArray.size();i++) {
			List<String> keywords = new ArrayList<>();
			
			JSONArray keywordsArray = (JSONArray) resultArray.get(i);
			// every job description has j keywords
			for (int j = 0; j < keywordsArray.size(); j++ ) {
				JSONObject object = (JSONObject) keywordsArray.get(j);
				String keyword = (String) object.get("keyword");
				keywords.add(keyword);
			}
			topKeywords.add(keywords);
		}
		return topKeywords;
	}
}

