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

import java.text.SimpleDateFormat;

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
//    private int lastSelectedItem = -1;

    private final static String LOG_TAG = MainScreenFragment.class.getSimpleName();

    public MainScreenFragment() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        currDate = dayFormat.format(System.currentTimeMillis());
    }

    private void updateScores() {
        Intent service_start = new Intent(getActivity(), MyFetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        mFragmentDate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView - start - mFragmentDate: " + mFragmentDate[0]);
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

    /*
        start loading data from the DB for the selected date and sort it on the 'time' column
        in ascending order.
         */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader - start");
        return new CursorLoader(getActivity(), DatabaseContract.scores_table
                .buildScoreWithDate(),
                null, null, mFragmentDate, DatabaseContract.scores_table.TIME_COL + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

//        int i = 0;
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            i++;
//            cursor.moveToNext();
//        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
//        int colCnt = cursor.getColumnCount();
        Log.v(LOG_TAG, "onLoadFinished - cursor.getCount()/colCnt: " + cursor.getCount() + "/" + mFragmentDate[0]);
        // FIXME: 6/11/2015 test below when there are some data for today's date
        if (cursor.getCount() > 0 && currDate.equals(mFragmentDate[0])) {
            Log.v(LOG_TAG, "onLoadFinished - currDate/mFragmentDate: " + currDate + "/" + mFragmentDate[0]);
            updateWidgets();
            int selectedRow = ((MainActivity) getActivity()).getWidgetSelectedRowIdx();
            if (selectedRow > -1) {
                Log.v(LOG_TAG, "onLoadFinished - scrolling to : " + selectedRow);
//                scoreList.smoothScrollToPosition(selectedRow);
                scoreList.setSelection(selectedRow);
                ((MainActivity) getActivity()).clearWidgetSelectedRowIdx();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void updateWidgets() {
        Log.v(LOG_TAG, "updateWidgets - sending broadcast ACTION_TODAYS_DATA_UPDATED");
        Context context = getActivity().getApplication();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_TODAYS_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    public void scrollToSelectedPosition() {
        // TODO: 9/11/2015 add code - onCreateView() - make listView a field. Here do getListView().setSelection(21); or getListView().smoothScrollToPosition(21); 
    }
}
