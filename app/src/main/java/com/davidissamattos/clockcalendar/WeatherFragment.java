package com.davidissamattos.clockcalendar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class WeatherFragment extends Fragment
{

    private final String TAG = "WeatherFragment";
    private final String ApiKey = "b0847e9fd818ae4e2ed86b551eeca88d";
    private String SaoPauloCityID = "3448439";

    //References
    private ImageView iconImageView;
    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView windTextView;
    private TextView temperatureMAXTextView;
    private TextView temperatureMINTextView;
    private TextView pressureTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private TextView rainTextView;
    private TextView humidityTextView;
    private TextView cloudsTextView;

    //Setting up the timer
    private int mInterval = 15*60*1000; // 15 min by default, can be changed later
    private Handler mHandler;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        stopRepeatingTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Inflate a view with the resource, pass the parent view to configure, but do not attache to root (false-> add the view in the activity code)
        View v = inflater.inflate(R.layout.weather_fragment,container,false);

        //getting the references for the views
        iconImageView = (ImageView) v.findViewById(R.id.weatherIcon);
        cityNameTextView = (TextView) v.findViewById(R.id.cityName);
        temperatureTextView = (TextView) v.findViewById(R.id.currentTemp);
        windTextView = (TextView) v.findViewById(R.id.windInfo);
        temperatureMAXTextView = (TextView) v.findViewById(R.id.tempMAX);
        temperatureMINTextView = (TextView) v.findViewById(R.id.tempMIN);
        pressureTextView = (TextView) v.findViewById(R.id.pressureInfo);
        sunriseTextView = (TextView) v.findViewById(R.id.sunriseInfo);
        sunsetTextView = (TextView) v.findViewById(R.id.sunsetInfo);
        rainTextView = (TextView) v.findViewById(R.id.rainInfo);
        humidityTextView = (TextView) v.findViewById(R.id.humidityInfo);
        cloudsTextView = (TextView) v.findViewById(R.id.cloudsInfo);
        //Adding Listeners

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
            new FetchCurrentWeatherData().execute();
            new FetchForecastWeatherData().execute();
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

    public class getWeather
    {
        public String CurrentWeatherJSONString;
        public String ForecastWeatherJSONString;
        private final String openweathermap_FORECAST_WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?id="+SaoPauloCityID+"&units=metric&cnt=1&appid="+ ApiKey;
        private final String openweathermap_CURRENT_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric&id=" + SaoPauloCityID + "&appid=" + ApiKey;
        private final String openweathermapIMAGES_URL = "http://openweathermap.org/img/w/";

        //Important tags to parse the JSON object
        private final String TAG_WEATHER = "weather";
        private final String TAG_MAIN = "main";
        private final String TAG_WIND = "wind";
        private final String TAG_CLOUDS = "clouds";
        private final String TAG_SYS = "sys";
        private final String TAG_ID = "id";
        private final String TAG_NAME = "name";
        private final String TAG_TEMP = "temp";
        private final String TAG_PRESSURE = "pressure";
        private final String TAG_TEMPMIN = "temp_min";
        private final String TAG_TEMPMAX = "temp_max";
        private final String TAG_MAX = "max";
        private final String TAG_MIN = "min";
        private final String TAG_HUMIDITY = "humidity";
        private final String TAG_ICON = "icon";
        private final String TAG_RAIN = "Rain";
        private final String TAG_ALL = "all";
        private final String TAG_3H = "3h";
        private final String TAG_SPEED = "speed";
        private final String TAG_DEG = "deg";
        private final String TAG_COUNTRY = "country";
        private final String TAG_SUNRISE = "sunrise";
        private final String TAG_SUNSET = "sunset";
        private final String TAG_LIST = "list";

        //Variables to represent weather conditions
        //Currently used
        public String tempCurrent;
        public String day_tempMin;
        public String day_tempMax;
        public String CityName;
        public String CountryName;
        public String cloudiness;
        public String humidity;
        public String pressure;
        public String windSpeed;
        public String windDeg;
        public String sunrise;
        public String sunset;
        public String rain3h;
        //Image
        public String iconId;
        public Bitmap icon;



        public void getForecastWeatherJSON() throws IOException
        {
            Log.d(TAG,"Preparing to get data");
            String JSONWeather = "";
            URL url = new URL(openweathermap_FORECAST_WEATHER_URL);
            Log.d(TAG,url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                Log.d(TAG,"Response code: "+ connection.getResponseMessage().toString());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Log.d(TAG,connection.getResponseMessage().toString());
                    throw new IOException(connection.getResponseMessage() + ": with " + url);

                }

                InputStream inputStream = connection.getInputStream();
                Log.d(TAG,inputStream.toString());
                BufferedReader reader;

                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                if (inputStream == null)
                {
                    Log.d(TAG, "input stream null");
                    return;
                }
                else
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        //buffer.append(line+"\n");
                        buffer.append(line);
                    }
                    //If stream is empty
                    if (buffer.length() == 0)
                    {
                        return;
                    }
                    JSONWeather = buffer.toString();
                    ForecastWeatherJSONString = JSONWeather;
                }
            }
            finally
            {
                connection.disconnect();
            }
        }

        public void getCurrentWeatherJSON() throws IOException
        {
            Log.d(TAG,"Preparing to get data");
            String JSONWeather = "";
            URL url = new URL(openweathermap_CURRENT_WEATHER_URL);
            Log.d(TAG,url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                Log.d(TAG,"Response code: "+ connection.getResponseMessage().toString());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Log.d(TAG,connection.getResponseMessage().toString());
                    throw new IOException(connection.getResponseMessage() + ": with " + url);

                }

                InputStream inputStream = connection.getInputStream();
                Log.d(TAG,inputStream.toString());
                BufferedReader reader;

                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                if (inputStream == null)
                {
                    Log.d(TAG, "input stream null");
                    return;
                }
                else
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        //buffer.append(line+"\n");
                        buffer.append(line);
                    }
                    //If stream is empty
                    if (buffer.length() == 0)
                    {
                        return;
                    }
                    JSONWeather = buffer.toString();
                    CurrentWeatherJSONString = JSONWeather;
                }
            }
            finally
            {
                connection.disconnect();
            }
        }

        public void getCurrentWeatherImage() throws IOException
        {
            Log.d(TAG,"Preparing to get data");
            URL url = new URL(openweathermapIMAGES_URL+iconId+".png");
            Log.d(TAG,url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                Log.d(TAG,"Response code: "+ connection.getResponseMessage().toString());
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Log.d(TAG,connection.getResponseMessage().toString());
                    throw new IOException(connection.getResponseMessage() + ": with " + url);

                }
                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                byte[] imagebytes = output.toByteArray();

                icon = BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length);
                Log.d(TAG,"Image downloaded");
            }
            finally
            {
                connection.disconnect();
            }
        }

        public void parseCurrentWeatherJSON()
        {
            try
            {
                Log.d(TAG,"Parsing JSON");
                JSONObject jsonObject = new JSONObject(CurrentWeatherJSONString);

                //NAME
                CityName  = jsonObject.getString(TAG_NAME);

                //JSON MAIN
                JSONObject main = jsonObject.getJSONObject(TAG_MAIN);
                tempCurrent = String.format("%.0f", main.getDouble(TAG_TEMP));
                humidity = main.getString(TAG_HUMIDITY);
                pressure = main.getString(TAG_PRESSURE);

                //JSON Weather - Icon
                JSONObject weather = jsonObject.getJSONArray(TAG_WEATHER).getJSONObject(0);
                iconId = weather.getString(TAG_ICON);
                Log.d(TAG,iconId);


                try
                {
                    //JSON RAIN
                    JSONObject rain = jsonObject.getJSONObject(TAG_RAIN);
                    rain3h = rain.getString(TAG_3H);
                }
                catch (JSONException e)
                {
                    rain3h = getActivity().getString(R.string.no_information);
                }

                //JSON WIND
                JSONObject wind = jsonObject.getJSONObject(TAG_WIND);
                windSpeed = String.format("%.1f", wind.getDouble(TAG_SPEED));
                windDeg = String.format("%.0f", wind.getDouble(TAG_DEG));

                //JSON CLOUDS
                JSONObject clouds = jsonObject.getJSONObject(TAG_CLOUDS);
                cloudiness = clouds.getString(TAG_ALL);

                //JSON SYS
                JSONObject sys = jsonObject.getJSONObject(TAG_SYS);
                CountryName = sys.getString(TAG_COUNTRY);
                //Converting to local time
                Long sunriseUTC = sys.getLong(TAG_SUNRISE);
                Long sunsetUTC = sys.getLong(TAG_SUNSET);
                Date sunriseDate = new Date(sunriseUTC*1000L);
                Date sunsetDate = new Date(sunsetUTC*1000L);
                sunrise = new SimpleDateFormat("kk:mm").format(sunriseDate);
                sunset = new SimpleDateFormat("kk:mm").format(sunsetDate);

                Log.d(TAG,"Parsing complete");

            }
            catch (JSONException e)
            {
                Log.d(TAG, "Error parsing JSON");
            }
        }

        public void parseForecastWeatherJSON()
        {
            try
            {
                Log.d(TAG,"Parsing JSON");
                JSONObject jsonObject = new JSONObject(ForecastWeatherJSONString);
                //JSON LIST
                JSONObject list = jsonObject.getJSONArray(TAG_LIST).getJSONObject(0);
                JSONObject temp = list.getJSONObject(TAG_TEMP);
                day_tempMax = String.format("%.0f", temp.getDouble(TAG_MAX));
                day_tempMin = String.format("%.0f", temp.getDouble(TAG_MIN));
                Log.d(TAG,"Parsing complete");
            }
            catch (JSONException e)
            {
                Log.d(TAG, "Error parsing JSON");
            }

        }

    }

    //Fetch Weather in Background
    private class FetchCurrentWeatherData extends AsyncTask<Void,Void,Void>
    {
        getWeather weather = new getWeather();

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                weather.getCurrentWeatherJSON();
                weather.parseCurrentWeatherJSON();
                weather.getCurrentWeatherImage();
                //Log.d(TAG,weather.CurrentWeatherJSONString);
            }
            catch (Throwable e)
            {
                Log.e(TAG,"Failed to fetch URL");
            }
            return null;
        }

        //Setting the UI
        @Override
        protected void onPostExecute(Void params)
        {
            //Setting the user interface after download
            cityNameTextView.setText(weather.CityName + ", " + weather.CountryName);
            ////////
            temperatureTextView.setText(weather.tempCurrent + "\u2103");
            iconImageView.setImageBitmap(weather.icon);
            //////
            //Row 1
            cloudsTextView.setText(weather.cloudiness + " %");
            //Row 2
            rainTextView.setText(weather.rain3h);
            humidityTextView.setText(weather.humidity + " %");
            //Row 3
            sunriseTextView.setText(weather.sunrise);
            sunsetTextView.setText(weather.sunset);
            //Row 4
            windTextView.setText(weather.windSpeed + " m/s \n" + weather.windDeg + "\u00B0");
            pressureTextView.setText(weather.pressure + " kPa");
        }


    }

    //Fetch Weather in Background
    private class FetchForecastWeatherData extends AsyncTask<Void,Void,Void>
    {
        getWeather weather = new getWeather();

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                weather.getForecastWeatherJSON();
                weather.parseForecastWeatherJSON();
            }
            catch (Throwable e)
            {
                Log.e(TAG,"Failed to fetch URL");
            }
            return null;
        }

        //Setting the UI
        @Override
        protected void onPostExecute(Void params)
        {
            //Only set the max and min for the day
            temperatureMINTextView.setText(weather.day_tempMin + "\u2103");
            temperatureMAXTextView.setText(weather.day_tempMax + "\u2103");
        }


    }

}
