package barqsoft.footballscores.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.database.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.adapters.ScoresAdapter;
import barqsoft.footballscores.adapters.ViewHolder;
import barqsoft.footballscores.activities.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String[] mFragmentDate = new String[1];
    private ScoresAdapter mAdapter;
    private String mCurrDate;
    private static final int SCORES_LOADER = 0;
    public static final String ACTION_TODAYS_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_TODAYS_DATA_UPDATED";
    private ListView mScoreList;
    private static final String ASC = " ASC";

    private final static String LOG_TAG = MainScreenFragment.class.getSimpleName();

    public MainScreenFragment() {
        mCurrDate = MainActivity.getDefaultPageDayInMillis();
    }

    public void setFragmentDate(String date) {
        mFragmentDate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mScoreList = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        mScoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.mDetailMatchId = MainActivity.getSelectedMatchId();
        mScoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.mDetailMatchId = selected.matchId;
                MainActivity.setSelectedMatchId((int) selected.matchId);
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    /*
        start loading data from the DB for the selected date and sort it on the 'time' column
        in ascending order.
    */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        Log.v(LOG_TAG, "onCreateLoader - date: " + mFragmentDate[0]);
        return new CursorLoader(getActivity(), DatabaseContract.scores_table
                .buildScoreWithDate(),
                null, null, mFragmentDate, DatabaseContract.scores_table.TIME_COL + ASC +
                " ," + DatabaseContract.scores_table.HOME_COL + ASC);
    }

    /**
     *
     * When the default 'current' day's data was inserted to DB, calls updateWidgets().
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0 && mCurrDate.equals(mFragmentDate[0])) {
            updateWidgets();
            int selectedRow = ((MainActivity) getActivity()).getWidgetSelectedRowIdx();
            if (selectedRow > -1) {
                mScoreList.setSelection(selectedRow);
                ((MainActivity) getActivity()).clearWidgetSelectedRowIdx();

                mAdapter.mDetailMatchId = ((MainActivity) getActivity()).getWidgetSelectedMatchId();
                MainActivity.setSelectedMatchId(((MainActivity) getActivity()).getWidgetSelectedMatchId());
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Send broadcast message to the widgets. It will force them to reload data from DB
     * and update their UI.
     */
    private void updateWidgets() {
        Context context = getActivity().getApplication();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_TODAYS_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

}
