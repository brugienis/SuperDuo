package barqsoft.footballscores.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import barqsoft.footballscores.R;
import barqsoft.footballscores.util.Utilies;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public double mDetailMatchId = 0;

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    private static final String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
//    private boolean isRightToLeft;

    private final static String LOG_TAG = ScoresAdapter.class.getSimpleName();

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /**
     *
     * Returns new list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    /**
     *
     * Populate one list item with the data contained in a cursor.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        String homeName = cursor.getString(COL_HOME);
        String awayName = cursor.getString(COL_AWAY);
        String timeStr = cursor.getString(COL_MATCHTIME);
        int homeScore = cursor.getInt(COL_HOME_GOALS);
        int awayScore = cursor.getInt(COL_AWAY_GOALS);
        String scores = Utilies.getScores(homeScore, awayScore);

        mHolder.homeName.setText(homeName);
        mHolder.awayName.setText(awayName);
        mHolder.date.setText(timeStr);
        mHolder.score.setText(scores);
        mHolder.matchId = cursor.getDouble(COL_ID);
        mHolder.homeCrest.setImageResource(Utilies.getTeamCrestByTeamName(
                cursor.getString(COL_HOME)));
        mHolder.awayCrest.setImageResource(Utilies.getTeamCrestByTeamName(
                cursor.getString(COL_AWAY)
        ));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mHolder.homeName.setContentDescription(context.getString(R.string.home_team_cont_desc, homeName));
            mHolder.awayName.setContentDescription(context.getString(R.string.away_team_cont_desc, awayName));
            mHolder.date.setContentDescription(context.getString(R.string.time_cont_desc, timeStr));
            mHolder.score.setContentDescription(context.getString(R.string.scores_cont_desc, scores));
        }

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if (mHolder.matchId == mDetailMatchId) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");
            int matchDay = cursor.getInt(COL_MATCHDAY);
            int league = cursor.getInt(COL_LEAGUE);
            String matchDayKind = Utilies.getMatchDay(matchDay, league);

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView matchDayTv = (TextView) v.findViewById(R.id.matchday_textview);
            matchDayTv.setText(matchDayKind);

            TextView leagueTv = (TextView) v.findViewById(R.id.league_textview);
            leagueTv.setText(Utilies.getLeague(league));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                matchDayTv.setContentDescription(context.getString(R.string.match_day_cont_desc, matchDayKind));
                leagueTv.setContentDescription(context.getString(R.string.league_cont_desc, league));
            }
            Button shareButton = (Button) v.findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.homeName.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.awayName.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    /**
     *
     * Returns 'share' Intent.
     */
    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
