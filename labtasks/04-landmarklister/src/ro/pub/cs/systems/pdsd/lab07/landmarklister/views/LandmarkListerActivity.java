package ro.pub.cs.systems.pdsd.lab07.landmarklister.views;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ro.pub.cs.systems.pdsd.lab07.earthquakelister.R;
import ro.pub.cs.systems.pdsd.lab07.landmarklister.controller.LandmarkInformationAdapter;
import ro.pub.cs.systems.pdsd.lab07.landmarklister.general.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ro.pub.cs.systems.pdsd.lab07.landmarklister.model.LandmarkInformation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class LandmarkListerActivity extends Activity {

    private EditText northEditText, southEditText, eastEditText, westEditText;

    private ListView landmarksListView;

    private ShowResultsButtonClickListener showResultsButtonClickListener = new ShowResultsButtonClickListener();

    private class ShowResultsButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String northString = northEditText.getText().toString();
            if (northString == null || northString.isEmpty()) {
                Toast.makeText(getApplication(), Constants.MISSING_INFORMATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            }
            String southString = southEditText.getText().toString();
            if (southString == null || southString.isEmpty()) {
                Toast.makeText(getApplication(), Constants.MISSING_INFORMATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            }
            String eastString = eastEditText.getText().toString();
            if (eastString == null || eastString.isEmpty()) {
                Toast.makeText(getApplication(), Constants.MISSING_INFORMATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            }
            String westString = westEditText.getText().toString();
            if (westString == null || westString.isEmpty()) {
                Toast.makeText(getApplication(), Constants.MISSING_INFORMATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                return;
            }

            new LandmarkListerAsyncTask().execute(northString, southString, eastString, westString);
        }
    }

    private class LandmarkListerAsyncTask extends AsyncTask<String, Void, List<LandmarkInformation>> {


        @Override
        protected List<LandmarkInformation> doInBackground(String... params) {

            // TODO: exercise 7
            // - create an instance of a HttpClient object
            // - create the URL to the web service, appending the bounding box coordinates and the username to the base Internet address
            // - create an instance of a HttGet object
            // - create an instance of a ReponseHandler object
            // - execute the request, thus obtaining the response
            // - get the JSON object representing the response
            // - get the JSON array (the value corresponding to the "geonames" attribute)
            // - iterate over the results list and create a LandmarkInformation for each element
            // - create a LandmarkInformationAdapter with the array and attach it to the landmarksListView

            String query = Constants.NORTH + params[0] + "&" + Constants.SOUTH + params[1]
                    + "&" + Constants.EAST + params[2] + "&" + Constants.WEST + params[3]
                    + "&username=me";

            String result = "";
            final List<LandmarkInformation> landMarkInformationList = new ArrayList<LandmarkInformation>();

            try {
                URL url = new URL(Constants.LANDMARK_LISTER_WEB_SERVICE_INTERNET_ADDRESS + query);
                URLConnection urlConnection = url.openConnection();

                HttpURLConnection httpURLConnection;
                if (urlConnection instanceof HttpURLConnection) {
                    httpURLConnection = (HttpURLConnection) urlConnection;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                }

                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.GEONAMES);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject landmarkJSON = jsonArray.getJSONObject(i);
                    landMarkInformationList.add(new LandmarkInformation(landmarkJSON.getDouble(Constants.LATITUDE),
                            landmarkJSON.getDouble(Constants.LONGITUDE), landmarkJSON.getString(Constants.TOPONYM_NAME),
                            landmarkJSON.getLong(Constants.POPULATION), landmarkJSON.getString(Constants.FCODE_NAME),
                            landmarkJSON.getString(Constants.NAME), landmarkJSON.getString(Constants.WIKIPEDIA_WEB_PAGE_ADDRESS),
                            landmarkJSON.getString(Constants.COUNTRY_CODE)));

                }
            } catch (MalformedURLException e) {
                Log.d(Constants.TAG, e.getMessage());
            } catch (IOException e) {
                Log.d(Constants.TAG, "url connection FAIL " + e.getMessage());
            } catch (JSONException e) {
                Log.d(Constants.TAG, "JSONObject create FAIL " + e.getMessage());
            }

            return landMarkInformationList;
        }


        @Override
        protected void onPostExecute(List<LandmarkInformation> landmarkInformations) {
            landmarksListView.setAdapter(new LandmarkInformationAdapter(getBaseContext(), new ArrayList<LandmarkInformation>(landmarkInformations)));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_lister);

        northEditText = (EditText) findViewById(R.id.north_edit_text);
        southEditText = (EditText) findViewById(R.id.south_edit_text);
        eastEditText = (EditText) findViewById(R.id.east_edit_text);
        westEditText = (EditText) findViewById(R.id.west_edit_text);

        landmarksListView = (ListView) findViewById(R.id.landmarks_list_view);

        Button showResultsButton = (Button) findViewById(R.id.show_results_button);
        showResultsButton.setOnClickListener(showResultsButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landmark_lister, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
