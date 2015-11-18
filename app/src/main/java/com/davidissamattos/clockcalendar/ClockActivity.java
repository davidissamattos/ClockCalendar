package com.davidissamattos.clockcalendar;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockActivity extends FragmentActivity {

    private final String TAG = "ClockActivity";

    private Fragment mClockFragment;
    private Fragment mWeatherFragment;

    private TextClock mClock;
    private TextView mDate;

    //Setting up the timer
    private int mInterval = 2*60*1000; // 2 min by default, can be changed later
    private Handler mHandler;

    Runnable mFetchData = new Runnable()
    {
        @Override
        public void run()
        {
            //Function to run
            updateDateTextView();
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


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock_activity);
        configureUIFlags();

        //getting the references for the views
        mClock = (TextClock) findViewById(R.id.digital_clock);
        mDate = (TextView) findViewById(R.id.date_clock);
        updateDateTextView();

        //

        //setting listeners
        mClock.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        configureUIFlags();
                    }
                }
        );

        //Setting up two fragments
        //If needed to change screen sizes etc...
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        mClockFragment = fragmentManager.findFragmentById(R.id.calendarFragmentID);
        mWeatherFragment = fragmentManager.findFragmentById(R.id.weatherFragmentID);
        if(mClockFragment==null)
        {
            mClockFragment = new CalendarFragment();
        }

        if (mWeatherFragment == null)
        {
            mWeatherFragment = new WeatherFragment();
        }

        transaction.add(R.id.calendarFragmentID, mClockFragment);
        transaction.add(R.id.weatherFragmentID, mWeatherFragment);
        transaction.commit();
    }



    private void updateDateTextView()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd, MMMM yyyy ");
        mDate.setText(sdf.format(new Date()));
    }

    //FULLSCREEN
    private void configureUIFlags()
    {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | // hide nav bar
                        View.SYSTEM_UI_FLAG_FULLSCREEN |   // hide status bar
                        View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }
}
