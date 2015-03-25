package org.unipd.nbeghin.climbtheworld.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.exceptions.NoFBSession;
import org.unipd.nbeghin.climbtheworld.models.ChartMember;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Group;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class FacebookUtils {
	private Context	context;

	public FacebookUtils(Context context) {
		this.context = context;
	}

//	private void showPublishResult(GraphObject result, FacebookRequestError error) {
//		if (error == null) {
//			Log.i(MainActivity.AppName, "Posted on Facebook wall");
//			Toast.makeText(context, "Posted on your wall", Toast.LENGTH_SHORT).show();
//		} else {
//			Log.e(MainActivity.AppName, "Unable to post on Facebook wall: " + error.getErrorMessage());
//			Toast.makeText(context, "Unable to post on your wall: " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
//		}
//	}   

	public static boolean isOnline(Activity activity) {
	    ConnectivityManager cm =
	        (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isOnline(Context ctx) {
	    ConnectivityManager cm =
	        (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isLoggedIn() {
	    Session session = Session.getActiveSession();
	    return (session != null && session.isOpened());
	}
	
	protected boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	private void checkFBSession() throws NoFBSession {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) throw new NoFBSession();
	}

	public void postToWall(Climbing climbing, String building_name) throws NoFBSession {
		Log.i(MainActivity.AppName, "Posting on FB wall");
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		params.putString("description", climbing.getFBStatusMessage(building_name));
		params.putString("link", "https://developers.facebook.com/android");
		switch(climbing.getGame_mode()){
		case 0:
			params.putString("picture", "http://climbtheworld.parseapp.com/img/win_solo_climb.jpeg");
			break;
		case 1:
			params.putString("picture", "http://climbtheworld.parseapp.com/img/win_social_climb.jpeg");
			break;
		case 2:
			params.putString("picture", "http://climbtheworld.parseapp.com/img/win_social_challenge.jpeg");	
			break;
		case 3:
			params.putString("picture", "http://climbtheworld.parseapp.com/img/win_team_vs_team.jpeg");
			break;
	}
		//params.putString("picture", /*climbing.getBuilding().getPhoto()*/ "http://2.bp.blogspot.com/-aO8ILLDFKv4/UQb08_I2JkI/AAAAAAAAPEU/RvEo5lNHDvs/s1600/Victory.jpg");
		publishFeedDialog(params);
	}
	
	public void postUpdateToWall(Climbing climbing, int newSteps, String building_name, String fb_id) throws NoFBSession{
		Log.i(MainActivity.AppName, "Posting on FB wall - update");
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		
		switch(climbing.getGame_mode()){
			case 0:
				params.putString("picture", "http://climbtheworld.parseapp.com/img/improve_solo_climb.png");
				break;
			case 1:
				params.putString("picture", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");
				break;
			case 2:
				params.putString("picture", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");	
				break;
			case 3:
				params.putString("picture", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");
				break;
		}
		
		params.putString("description", climbing.getUpdateMessage(newSteps, building_name));
		params.putString("link", "https://developers.facebook.com/android");
		//params.putString("picture", /*climbing.getBuilding().getPhoto()*/ "http://images.nationalgeographic.com/wpf/media-live/photos/000/234/cache/gunks-new-york-climb_23497_600x450.jpg");
		//params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		String id = "{'tag_uid':'"+ "1382835532010134" +"'} ,";
		params.putString("tags","["+id+"]");
		publishFeedDialog(params);
	}
	
	public void inviteFriends(Collection<GraphUser> friends) throws NoFBSession {
		for(GraphUser friend: friends) {
			Bundle params = new Bundle();
			params.putString("name", "ClimbTheWorld");
			params.putString("caption", "Climb the world: a serious game to promote physical activity");
			params.putString("description", "Download ClimbTheWorld from Google Play (FREE)");
			params.putString("link", "https://developers.facebook.com/android");
			params.putString("to", friend.getId());
			params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
			publishFeedDialog(params);
		}
	}

	public void publishFeedDialog() throws NoFBSession {
		Bundle params = new Bundle();
		params.putString("name", "ClimbTheWorld");
		params.putString("caption", "Climb the world: a serious game to promote physical activity");
		params.putString("description", "Download ClimbTheWorld from Google Play (FREE)");
		params.putString("link", "https://developers.facebook.com/android");
		params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		publishFeedDialog(params);
		
	}

	public void publishFeedDialog(Bundle params) throws NoFBSession {
		this.checkFBSession();
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					final String postId = values.getString("post_id");
					if (postId != null) {
						Toast.makeText(context, context.getString(R.string.post_published), Toast.LENGTH_SHORT).show();
					} else {
						// User clicked the Cancel button
						Toast.makeText(context.getApplicationContext(), context.getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
					}
				} else if (error instanceof FacebookOperationCanceledException) {
					// User clicked the "x" button
					Toast.makeText(context.getApplicationContext(), context.getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
				} else {
					// Generic, ex: network error
					Toast.makeText(context.getApplicationContext(), context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				}
			}
		}).build();
		feedDialog.show();
	}
	
	public static FacebookDialog publishOpenGraphStory_SoloClimb(Activity activity, boolean win, int steps, String building_name, int tot_steps){
		OpenGraphObject setObj = OpenGraphObject.Factory.createForPost("unipdclimb:building");
		OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
    	setObj.setProperty("url", "http://climbtheworld.parseapp.com/building.com");
    	
    	
        
        if(win){
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.solo_climb_win_opengraph_title, building_name));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.solo_climb_win_opengraph_descr, tot_steps));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/win_solo_climb.jpeg");
        	action.setType("unipdclimb:climb");
        }else{
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.solo_climb_improve_opengraph_title));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.solo_climb_improve_opengraph_descr, steps, building_name));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/improve_solo_climb.png");
            action.setType("unipdclimb:keep_climbing");

        }
        
        action.setProperty("building", setObj);
        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "building").build();
        return shareDialog;
	}
	
	public static FacebookDialog publishOpenGraphStory_SocialClimb(Activity activity, JSONObject collaborators, boolean win, int steps, String building_name){
		OpenGraphObject setObj = OpenGraphObject.Factory.createForPost("unipdclimb:building");
		OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
    	List<GraphUser> tags = new ArrayList<GraphUser>();

    	setObj.setProperty("url", "http://climbtheworld.parseapp.com/building.com");
        
        if(win){
            action.setType("unipdclimb:climb");
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.social_climb_win_opengraph_title));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.social_climb_win_opengraph_descr, building_name));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/win_social_climb.jpeg");
        }else{
        	action.setType("unipdclimb:help_to_climb");
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.social_climb_improve_opengraph_title));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.social_climb_improve_opengraph_descr, steps, building_name));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");

        }
        
        
        action.setProperty("building", setObj);
        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "building").build();
        return shareDialog;
	}
	
	public static FacebookDialog publishOpenGraphStory_SocialChallenge(Activity activity, List<ChartMember> chart, boolean win, int steps, String building_name, int old_position, boolean checked){
		SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		
		OpenGraphObject setObj = null;//OpenGraphObject.Factory.createForPost("unipdclimb:building");
		OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
    	List<GraphUser> tags = new ArrayList<GraphUser>();
        FacebookDialog shareDialog = null;//new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "building").build();


    	String my_fb_id = pref.getString("FBid", "");
    	int current_position = ModelsUtil.chartPosition(my_fb_id, chart);
        
        if(win && checked){
            action.setType("unipdclimb:win");
            setObj = OpenGraphObject.Factory.createForPost("unipdclimb:challenge");
        	setObj.setProperty("url", "http://climbtheworld.parseapp.com/challenge.com");
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.social_challenge_win_opengraph_title));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.social_challenge_win_opengraph_descr, building_name));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/win_social_challenge.jpeg");	
        	
            action.setProperty("challenge", setObj);
        	shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "challenge").build();
        }else{
        	if(current_position < old_position){
        		action.setType("unipdclimb:is_making");
                setObj = OpenGraphObject.Factory.createForPost("unipdclimb:overtake");
            	setObj.setProperty("url", "http://climbtheworld.parseapp.com/overtake.com");
        		setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.social_challenge_overtake_opengraph_title));
        		setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.social_challenge_overtake_opengraph_descr, steps, building_name));
            	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/overtake.png");	

        	
                action.setProperty("overtake", setObj);
            	shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "overtake").build();
        	}else{
        		action.setType("unipdclimb:is_closing");
                setObj = OpenGraphObject.Factory.createForPost("unipdclimb:the_gap");
            	setObj.setProperty("url", "http://climbtheworld.parseapp.com/gap.com");	
        		setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.social_challenge_improve_opengraph_title));
        		setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.social_challenge_improve_opengraph_descr, steps, building_name));
            	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");	

        		
                action.setProperty("the_gap", setObj);
            	shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "the_gap").build();
        	}
        }
        
      
        
        
    	
        return shareDialog;
	}
	
	
	public static FacebookDialog publishOpenGraphStory_TeamVsTeam(Activity activity, Group myTeam, JSONObject creators, JSONObject challengers, boolean win, int steps, String building_name, int old_position, int new_position, boolean checked){
		SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		OpenGraphObject setObj = null;// OpenGraphObject.Factory.createForPost("unipdclimb:building");
		OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
    	List<GraphUser> tags = new ArrayList<GraphUser>();
        FacebookDialog shareDialog = null;//new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "building").build();

      	Iterator<String> it = null;

        if(win && checked){
        	setObj =  OpenGraphObject.Factory.createForPost("unipdclimb:team_challenge");
        	action.setType("unipdclimb:win");
        	setObj.setProperty("url", "http://climbtheworld.parseapp.com/team_challenge.com");
        	setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.team_vs_team_win_opengraph_title));
        	setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.team_vs_team_win_opengraph_descr, building_name));
        	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/win_team_vs_team.png");	
        	
            action.setProperty("team_challenge", setObj);
            shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "team_challenge").build();

        	
        }else{
        	if(new_position < old_position){
        		setObj =  OpenGraphObject.Factory.createForPost("unipdclimb:pole_position");
            	action.setType("unipdclimb:help_to_gain");
            	setObj.setProperty("url", "http://climbtheworld.parseapp.com/pole.com");
        		setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.team_vs_team_overtake_opengraph_title));
        		setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.team_vs_team_overtake_opengraph_descr, steps, building_name));
            	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/overtake.png");	

        		
                action.setProperty("pole_position", setObj);
                shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "pole_position").build();
            	
        	}else{
        		setObj =  OpenGraphObject.Factory.createForPost("unipdclimb:the_gap");
            	action.setType("unipdclimb:help_to_close");
            	setObj.setProperty("url", "http://climbtheworld.parseapp.com/gap.com");
        		setObj.setProperty("title", ClimbApplication.getContext().getString(R.string.team_vs_team_improve_opengraph_title));
        		setObj.setProperty("description", ClimbApplication.getContext().getString(R.string.team_vs_team_improve_opengraph_descr, steps, building_name));
            	setObj.setProperty("image", "http://climbtheworld.parseapp.com/img/social_climb_improve.png");	

        		
        		
        		
                action.setProperty("the_gap", setObj);
                shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "the_gap").build();
        	}
        }
        
      
       
        return shareDialog;
	}
	
	 public static Bitmap getRoundedBitmap(Bitmap bitmap) {
	        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
	                .getHeight(), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);

	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawOval(rectF, paint);

	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);

	        return output;
	    }
}
