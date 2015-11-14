package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.MyFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String[] mFragmentDate = new String[1];
    private ScoresAdapter mAdapter;
    private String currDate;
    private static final int SCORES_LOADER = 0;
    public static final String ACTION_TODAYS_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_TODAYS_DATA_UPDATED";
    private ListView scoreList;

    private final static String LOG_TAG = MainScreenFragment.class.getSimpleName();

    public MainScreenFragment() {
        currDate = MainActivity.getDefaultPageDayInMillis();
    }

    private void updateScores() {
        Intent intent = new Intent(getActivity(), MyFetchService.class);
        getActivity().startService(intent);
    }

    public void setFragmentDate(String date) {
        mFragmentDate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        updateScores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        scoreList = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        scoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.mDetailMatchId = MainActivity.selectedMatchId;
        scoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.mDetailMatchId = selected.matchId;
                MainActivity.selectedMatchId = (int) selected.matchId;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    private static final String ASC = " ASC";
    /*
        start loading data from the DB for the selected date and sort it on the 'time' column
        in ascending order.
    */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader - date: " + mFragmentDate[0]);
        return new CursorLoader(getActivity(), DatabaseContract.scores_table
                .buildScoreWithDate(),
                null, null, mFragmentDate, DatabaseContract.scores_table.TIME_COL + ASC +
                " ," + DatabaseContract.scores_table.HOME_COL + ASC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        Log.v(LOG_TAG, "onLoadFinished - date/count: " + mFragmentDate[0] + "/" + cursor.getCount());
        if (cursor.getCount() > 0 && currDate.equals(mFragmentDate[0])) {
            updateWidgets();
            int selectedRow = ((MainActivity) getActivity()).getWidgetSelectedRowIdx();
            if (selectedRow > -1) {
                scoreList.setSelection(selectedRow);
                ((MainActivity) getActivity()).clearWidgetSelectedRowIdx();

                mAdapter.mDetailMatchId = ((MainActivity) getActivity()).getWidgetSelectedMatchId();
                MainActivity.selectedMatchId = ((MainActivity) getActivity()).getWidgetSelectedMatchId();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void updateWidgets() {
        Context context = getActivity().getApplication();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_TODAYS_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
