package org.unipd.nbeghin.climbtheworld;

import android.app.ProgressDialog;

import com.facebook.Session;

public interface NetworkRequests {
	void makeRequest(Session session, ProgressDialog PD);
}
