package io.araujo.androidhttp.database;

import android.os.AsyncTask;

import java.sql.SQLException;

public abstract class DatabaseTask<P, T> extends AsyncTask<P, Void, T> {

	@Override
	protected final T doInBackground(P... params) {
		try {
			return dbOperation(params);
		} catch (SQLException e) {
            e.printStackTrace();
			return null;
		}
	}

	protected abstract T dbOperation(P... params) throws SQLException;

	protected void doPostExecute(T result) {
	}

	protected final void onPostExecute(final T result) {
        doPostExecute(result);
        super.onPostExecute(result);
	}

}