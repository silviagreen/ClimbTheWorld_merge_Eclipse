package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.ui.card.NotificationCard;
import org.unipd.nbeghin.climbtheworld.ui.card.Updater;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.fima.cardsui.views.CardUI;

public class NotificationFragment extends Fragment implements Updater{
	static public CardUI	notificationCards;
	static Button btnUpdate;
	
	private class LoadNotificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}
	
	@Override
	public void refresh() {
		if(ClimbApplication.notifications.isEmpty()){
			btnUpdate.setVisibility(View.VISIBLE);
			notificationCards.setVisibility(View.GONE);
		}else{
			btnUpdate.setVisibility(View.GONE);
			notificationCards.setVisibility(View.VISIBLE);
		
		notificationCards.clearCards();
		for (final Notification notification : ClimbApplication.notifications) {
			if(!notification.isRead()){
				NotificationCard notificationCard;
				if(ClimbApplication.notifications.indexOf(notification) == 0)
					notificationCard = new NotificationCard(notification, true);
				else
					notificationCard = new NotificationCard(notification, false);

					
//			notificationCard.setOnCardSwipedListener(new OnCardSwiped() {
//				
//				@Override
//				public void onCardSwiped(Card card, View layout) {
//					System.out.println("swiiiiipe");					
//				}
//			});
			
			notificationCards.addCard(notificationCard);
			}else{
				ClimbApplication.notifications.remove(notification);
			}
		}
		notificationCards.refresh();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_notification, container, false);
		btnUpdate = (Button) result.findViewById(R.id.emptyBtn);
		btnUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).onUpdateNotifications();
				
			}
		});
//		empty = (TextView) result.findViewById(R.id.empty);
//		int i = (String.valueOf(empty.getText())).indexOf("b1");
//		SpannableString ss = new SpannableString(empty.getText()); 
//        Drawable d = getResources().getDrawable(R.drawable.overflow); 
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//        ss.setSpan(span, i, i+"b1".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//        empty.setText(ss); 
//        
//        i = (String.valueOf(empty.getText())).indexOf("b0");
//		ss = new SpannableString(empty.getText()); 
//        d = getResources().getDrawable(R.drawable.profile); 
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//        span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//        ss.setSpan(span, i, i+"b0".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//        empty.setText(ss);
//        
//        i = (String.valueOf(empty.getText())).indexOf("b2");
//		ss = new SpannableString(empty.getText()); 
//        d = getResources().getDrawable(R.drawable.menu); 
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
//        span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
//        ss.setSpan(span, i, i+"b2".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
//        empty.setText(ss);
        
		
//		SpannableStringBuilder ssb = new SpannableStringBuilder(empty.getText());
//	    int i = (String.valueOf(empty.getText())).indexOf("b1");
//	    ssb.setSpan(new ImageSpan(getActivity().getResources().getDrawable(R.drawable.overflow)), i, i+"b1".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//	    i = (String.valueOf(empty.getText())).indexOf("b2");
//	    //ssb.setSpan(new ImageSpan(getActivity().getResources().getDrawable(R.drawable.menu)), i, i+"b2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		empty.setText( ssb, BufferType.SPANNABLE );
		
		notificationCards = (CardUI) result.findViewById(R.id.cardsNotification);
		notificationCards.setSwipeable(true);
		
		refresh();
		return (result);
	}
	
	

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
}
