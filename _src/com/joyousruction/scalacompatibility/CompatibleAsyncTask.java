package com.joyousruction.scalacompatibility;

import android.os.AsyncTask;

public abstract class CompatibleAsyncTask extends AsyncTask<Void,Integer,Long> {

	protected abstract Long backgroundTask();
	
	@Override
	protected Long doInBackground(Void... arg0) {
		return backgroundTask();
	}

}
