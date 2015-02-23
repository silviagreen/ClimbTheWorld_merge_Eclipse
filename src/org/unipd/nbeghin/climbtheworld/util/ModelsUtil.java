package org.unipd.nbeghin.climbtheworld.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.ChartMember;
import org.unipd.nbeghin.climbtheworld.models.MicrogoalText;
import org.unipd.nbeghin.climbtheworld.models.TourText;

public class ModelsUtil {

	public static int getIndexByProperty(int id, List<Building> buildings) {
        for (int i = 0; i < buildings.size(); i++) {
            if (buildings.get(i).get_id() == id) {
                return i;
            }
        }
        return -1;// not there is list
    }
	
	public static List<ChartMember> fromJsonToChart(JSONObject json){
		Iterator keys = json.keys();
		List<ChartMember> chart = new ArrayList<ChartMember>();
		while(keys.hasNext()){
			String key;
			try {
				key = (String) keys.next();
				ChartMember m = new ChartMember(key, json.getInt(key));
				chart.add(m);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Collections.sort(chart, new Comparator<ChartMember>(){
			public int compare(ChartMember m1, ChartMember m2){
				if(m1.getScore() == m2.getScore())
					return 0;
				else
					return m1.getScore() > m2.getScore() ? -1 : 1;
			}
		});
		return chart;
	}
	
	public static int chartPosition(String id, List<ChartMember> chart){
		for(int i = 0; i < chart.size(); i++){
			if(chart.get(i).getId().equalsIgnoreCase(id))
				return i;
		}
		return -1;
	}
	
	public static SortedMap<Integer, String> fromJsonToSortedMap(JSONObject json){
		Iterator keys = json.keys();
		SortedMap<Integer, String> map = new TreeMap<Integer, String>();
		while(keys.hasNext()){
			String key = (String) keys.next();
			try {
				map.put(json.getInt(key), key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public static int getMyPosition(String me, SortedMap<Integer, String> map){


		for (Entry<Integer, String> entry : map.entrySet())
		{
			if(entry.getValue().equalsIgnoreCase(me)){
				return map.headMap(entry.getKey()).entrySet().size() + 1;
			}
		}
		return 0;
	}
	
	public static int sum(JSONObject json){
		Iterator<String> keys = json.keys();
		int sum = 0;
		while(keys.hasNext()){
			try {
				sum += json.getInt(keys.next());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum;
	}
	
	public static JSONArray removeFromJSONArray(JSONArray jarray, int pos){
		JSONArray Njarray=new JSONArray();
		try{
			for(int i=0;i<jarray.length();i++){     
				if(i!=pos)
					Njarray.put(jarray.get(i));     
			}
		}catch (Exception e){e.printStackTrace();}
	return Njarray;
	}
	
	public static boolean contains(ArrayList<JSONObject> list, JSONObject obj){
		for(JSONObject o : list){
			if(o.optString("id").equals(obj.optString("id")))
				return true;
		}
		return false;
	}
	
	public static MicrogoalText getMicrogoalTextByStory(int story_id){
		for(MicrogoalText text : ClimbApplication.microgoalTexts){
			if(text.getStory_id() == story_id)
				return text;
		}
		return null;
	}
	
	public static BuildingText getBuildingTextFromBuildingId(int id){
		for(BuildingText text : ClimbApplication.buildingTexts){
			if(text.getBuilding().get_id() == id)
				return text;
		}
		return null;
	}
	
	public static TourText getTourTextFromBuildingId(int id){
		for(TourText text : ClimbApplication.tourTexts){
			if(text.getTour().get_id() == id)
				return text;
		}
		return null;
	}
	
	public static boolean hasSomeoneWon(List<ChartMember> chart, int total_steps){
		for(ChartMember member: chart){
			if(member.getScore() == total_steps)
				return true;
		}
		return false;
	}
	
	public static boolean hasSomeoneWon(int team1, int team2, int total_steps){
		if(team1 == total_steps || team2 == total_steps)
				return true;
		else
			return false;
	}
}
