package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;

import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class BadgeCard extends Card{
	
	ImageView cup;
	TextView description;
	TextView name;
	ProgressBar progressBar;
	UserBadge userBadge;
	TextView textPercentage;
	
	public BadgeCard(UserBadge userBadge) {
		super(String.valueOf(userBadge.get_id()));
		this.userBadge = userBadge;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.badge_card, null);
		cup = (ImageView) view.findViewById(R.id.imageCup);
		description = (TextView) view.findViewById(R.id.textDescription);
		name = (TextView) view.findViewById(R.id.textName);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		name.setText(userBadge.getDescription());
		description.setText(userBadge.getName());
		if(userBadge.getPercentage() < 1.00){
			cup.setImageResource(R.drawable.unlock_win);
		}else
			cup.setImageResource(R.drawable.lock_win);
		progressBar.setIndeterminate(false);
		progressBar.setProgress((int)(userBadge.getPercentage() * 100));
		textPercentage = (TextView) view.findViewById(R.id.textPercentage);
		textPercentage.setText(new DecimalFormat("#").format(userBadge.getPercentage() * 100) + "%");
		return view;
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}

}
