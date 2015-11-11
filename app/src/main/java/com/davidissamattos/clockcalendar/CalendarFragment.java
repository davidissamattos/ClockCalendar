package com.davidissamattos.clockcalendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.CalendarContract;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by David on 05/11/15.
 */
public class CalendarFragment extends Fragment {
    //Debug tag
    private final String TAG = "CalendarFragment";

    //Views in fragment
    private TextView mNextEventTextView;
    private TextView mAllDayTextView;


    //Setting up the timer
    private int mInterval = 15*60*1000; // 15 min by default, can be changed later
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load any other stuff

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Inflate a view with the resource, pass the parent view to configure, but do not attache to root (false-> add the view in the activity code)
        View v = inflater.inflate(R.layout.calendar_fragment,container,false);

        //Layout references
        mNextEventTextView = (TextView) v.findViewById(R.id.textview_nextEvent);
        mAllDayTextView = (TextView) v.findViewById(R.id.textview_allDayMessage);

        //Adding Listeners to update
        mNextEventTextView.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new fetchCalendarEvents().execute();
                    }
                }
        );
        mAllDayTextView.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new fetchCalendarEvents().execute();
                    }
                }
        );

        //Setting the timer and the task
        mHandler = new Handler();
        startRepeatingTask();

        return v;
    }

    Runnable mFetchData = new Runnable()
    {
        @Override
        public void run()
        {
            //Function to run
            new fetchCalendarEvents().execute();
            //Re-schedule
            mHandler.postDelayed(mFetchData, mInterval);
        }
    };
    void startRepeatingTask()
    {
        mFetchData.run();
    }
    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mFetchData);
    }


    public class myCalendarEvents
    {

        //Other resources
        //String references
        private String nextEventInitialString = getActivity().getString(R.string.nextEvent_InitalMessage);
        private String noEventString = getActivity().getString(R.string.noEvent_Message);
        private String hourPreposition = getActivity().getString(R.string.hour_preposition);
        private String allDayInitialString = getActivity().getString(R.string.allDay_InitialMessage);
        private String noAllDayString = getActivity().getString(R.string.no_allDay_InitialMessage);

        public int numberOfEvents = 0;
        public int numberOfNextEvents =0;
        public int numberOfAllDayEvents =0;

        public String nextEvent_TITLE;
        public String nextEvent_LOCATION;
        public long nextEvent_START;

        public String allDayEvent_TITLE;
        public String allDayEvent_LOCATION;

        //Database calendar Variables
        //private static final String EVENT_ORDER = CalendarContract.Events.DTSTART + " ASC";
        private static final String INSTANCE_ORDER = CalendarContract.Instances.START_DAY + " ASC, " + CalendarContract.Instances.START_MINUTE+ " ASC";
        String[] PROJECTION = new String[]
                {
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.ALL_DAY
                };

        private static final int PROJECTION_TITLE_INDEX = 0;
        private static final int PROJECTION_EVENT_LOCATION_INDEX = 1;
        private static final int PROJECTION_BEGIN_INDEX = 2;
        private static final int PROJECTION_END_INDEX = 3;
        private static final int PROJECTION_DTSTART_INDEX = 4;
        private static final int PROJECTION_ALL_DAY_INDEX = 5;

        private Cursor mCursor = null;

        //Calendar Function
        //If needed can be used selection an selection args
        private void setupCalendar(String selection, String[] selectionArgs, long startMillis, long endMillis)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                Log.d(TAG, "SDK 6.0");
                if (getActivity().checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    ContentResolver cr = getActivity().getContentResolver();
                    // Construct the query with the desired date range.
                    Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                    ContentUris.appendId(builder, startMillis);
                    ContentUris.appendId(builder, endMillis);
                    // Submit the query
                    mCursor =  cr.query(builder.build(),
                            PROJECTION,
                            selection,
                            selectionArgs,
                            INSTANCE_ORDER);
                }
            }
            else
            {
                ContentResolver cr = getActivity().getContentResolver();
                // Construct the query with the desired date range.
                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, startMillis);
                ContentUris.appendId(builder, endMillis);
                // Submit the query
                mCursor =  cr.query(builder.build(),
                        PROJECTION,
                        selection,
                        selectionArgs,
                        INSTANCE_ORDER);
            }
        }
        public void getLastEvent()
        {
            //Getting now
            Calendar now = Calendar.getInstance();
            long nowMillis = now.getTimeInMillis();

            //Getting Today
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayMillis = today.getTimeInMillis();

            //Date now = new Date(todayMillis);
            //Log.d(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(now));

            //Getting tomorrow
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DATE, 1);
            tomorrow.set(Calendar.HOUR_OF_DAY, 0);
            tomorrow.set(Calendar.MINUTE, 0);
            tomorrow.set(Calendar.SECOND, 0);
            tomorrow.set(Calendar.MILLISECOND, 0);
            long tomorrowMillis = tomorrow.getTimeInMillis();

            //If needed can be used selection an selection args
            setupCalendar(null,null,todayMillis,tomorrowMillis);
            if (mCursor != null)
            {
                //go to the first position
                numberOfEvents = mCursor.getCount();
                Log.d(TAG, "There are: " + numberOfEvents + " events in calendar");
                mCursor.moveToFirst();
                int nextEventIndex =0;
                int allDayEventIndex=0;
                ArrayList<Integer> allDayEvent = new ArrayList<>();
                ArrayList<Integer> nextEvent = new ArrayList<>();
                //Events
                if (numberOfEvents > 0)
                {
                    Log.d(TAG, Long.toString(todayMillis));
                    //See if there is an all day event
                    mCursor.moveToFirst();
                    for (int i = 0; i < numberOfEvents; i++)
                    {
                        Log.d(TAG, "All: " + mCursor.getString(PROJECTION_ALL_DAY_INDEX) + " - " + mCursor.getString(PROJECTION_TITLE_INDEX) + " --> " + mCursor.getString(PROJECTION_BEGIN_INDEX));
                        boolean allDay = mCursor.getString(PROJECTION_ALL_DAY_INDEX).equals("1");
                        if (allDay)
                        {
                            allDayEvent.add(i);
                            //Log.d(TAG,"All day: " + mCursor.getString(PROJECTION_TITLE_INDEX));
                        }
                        else
                        {
                            if ((mCursor.getLong(PROJECTION_BEGIN_INDEX)) > (nowMillis))
                            {
                                nextEvent.add(i);
                            }
                        }
                        mCursor.moveToNext();
                    }

                    //Getting the first all day event
                    if (allDayEvent.size()>0)
                    {
                        //primeiro elemento
                        allDayEventIndex = allDayEvent.get(0);
                        mCursor.moveToPosition(allDayEventIndex);
                        allDayEvent_TITLE = mCursor.getString(allDayEventIndex);
                        allDayEvent_LOCATION = mCursor.getString(allDayEventIndex);
                        numberOfAllDayEvents = allDayEvent.size();
                    }
                    else
                    {
                        numberOfAllDayEvents = 0;
                    }
                    //Getting the first next event
                    if (nextEvent.size()>0)
                    {
                        nextEventIndex = nextEvent.get(0);
                        mCursor.moveToPosition(nextEventIndex);
                        nextEvent_TITLE = mCursor.getString(PROJECTION_TITLE_INDEX);
                        nextEvent_LOCATION = mCursor.getString(PROJECTION_EVENT_LOCATION_INDEX);
                        nextEvent_START = mCursor.getLong(PROJECTION_BEGIN_INDEX);
                        numberOfNextEvents = nextEvent.size();
                    }
                    else
                    {
                        numberOfNextEvents =0;
                    }
                }
            }
            else
            {
                Log.d(TAG,"Cursor is NULL!!!");
            }
        }
    }


    //Fetch Weather in Background
    private class fetchCalendarEvents extends AsyncTask<Void,Void,Void>
    {
        myCalendarEvents mCalendarEvents = new myCalendarEvents();
        @Override
        protected Void doInBackground(Void... params)
        {

            mCalendarEvents.getLastEvent();
            return null;
        }

        //Setting the UI
        @Override
        protected void onPostExecute(Void params)
        {
            //Only set the max and min for the day
            if (mCalendarEvents.numberOfNextEvents > 0)
            {
                Date nextEventDate = new Date(mCalendarEvents.nextEvent_START);
                String hour = new SimpleDateFormat("kk:mm").format(nextEventDate);
                mNextEventTextView.setText(mCalendarEvents.nextEventInitialString+" "
                        +  mCalendarEvents.nextEvent_TITLE
                        + " " +mCalendarEvents.hourPreposition + " "
                        + hour);
            }
            else
            {
                mNextEventTextView.setText(mCalendarEvents.noEventString);
            }

            if (mCalendarEvents.numberOfAllDayEvents > 0)
            {
                mAllDayTextView.setText(mCalendarEvents.allDayInitialString
                        + mCalendarEvents.allDayEvent_TITLE);
            }
            else
            {
                mAllDayTextView.setText(mCalendarEvents.noAllDayString);
            }
        }


    }

}
