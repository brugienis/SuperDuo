package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.PagerFragment;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class MyFetchService extends IntentService {
    
    private SimpleDateFormat matchDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
    private SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
    private SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 86400000L;

    private final static String LOG_TAG = MyFetchService.class.getSimpleName();

    public MyFetchService() {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n10");
        getData("p10");
        Intent messageIntent = new Intent(PagerFragment.MESSAGE_EVENT);
//        messageIntent.putExtra(MainActivity.MESSAGE_KEY, MainActivity.DELETE_EVENT);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        String APIKey = getString(R.string.api_key);
        if (APIKey.trim().length() == 0) {
            throw new RuntimeException(LOG_TAG + ".getData - empty 'api_key' in strings.xml");
        }
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetchBuild = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).
                appendQueryParameter("APPID", "a813e2098a3bc05b49ac25ece5e0eaf9").
                build();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String JSONData = null;
        Log.v(LOG_TAG, "getData - fetchBuild: " + fetchBuild);
        //Opening Connection
        try {
            URL fetch = new URL(fetchBuild.toString());
            connection = (HttpURLConnection) fetch.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Auth-Token", APIKey.trim());
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
//                Log.v(LOG_TAG, "getData - line: " + line);
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSONData = buffer.toString();
            // FIXME: 11/11/2015 think how to handle exceptions here
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }
        try {
            if (JSONData != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSONData).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONData(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }
                processJSONData(JSONData, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONData(String JSONData, Context mContext, boolean isReal) {
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
//        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
//        final String Bundesliga3 = "403";
//        final String EREDIVISIE = "404";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league;
        String date;
        String time;
        String home;
        String away;
        String homeGoals;
        String awayGoals;
        String matchId;
        String matchDay;


        try {
            JSONArray matches = new JSONObject(JSONData).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject matchData = matches.getJSONObject(i);
                league = matchData.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (league.equals(PREMIER_LEAGUE) ||
                        league.equals(SERIE_A) ||
                        league.equals(BUNDESLIGA1) ||
                        league.equals(BUNDESLIGA2) ||
                        league.equals(PRIMERA_DIVISION) ||
                        league.equals(SEGUNDA_DIVISION) ||
                        league.equals(PRIMERA_LIGA) ||
                        league.equals(LIGUE1)
                        ) {
                    matchId = matchData.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId = matchId + Integer.toString(i);
                    }
                    // "date":"2015-10-23T16:30:00Z",
                    date = matchData.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    matchDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = matchDate.parse(date + time);
                        new_date.setTimeZone(TimeZone.getDefault());
                        date = new_date.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * TWENTY_FOUR_HOURS_IN_MILLIS));
                            date = mformat.format(fragmentdate);
                        }
                        // FIXME: 11/11/2015 think what to do
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    home = matchData.getString(HOME_TEAM);
                    away = matchData.getString(AWAY_TEAM);
                    homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = matchData.getString(MATCH_DAY);

                    ContentValues matchValues = new ContentValues();
                    matchValues.put(DatabaseContract.scores_table.MATCH_ID, matchId);
                    matchValues.put(DatabaseContract.scores_table.DATE_COL, date);
                    matchValues.put(DatabaseContract.scores_table.TIME_COL, time);
                    matchValues.put(DatabaseContract.scores_table.HOME_COL, home);
                    matchValues.put(DatabaseContract.scores_table.AWAY_COL, away);
                    matchValues.put(DatabaseContract.scores_table.HOME_GOALS_COL, homeGoals);
                    matchValues.put(DatabaseContract.scores_table.AWAY_GOALS_COL, awayGoals);
                    matchValues.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                    matchValues.put(DatabaseContract.scores_table.MATCH_DAY, matchDay);

                    values.add(matchValues);
                }
            }
            ContentValues[] insertData = new ContentValues[values.size()];
            values.toArray(insertData);
            mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insertData);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}

