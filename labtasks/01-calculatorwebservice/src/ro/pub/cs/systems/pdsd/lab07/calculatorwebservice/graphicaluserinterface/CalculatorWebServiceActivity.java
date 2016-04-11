package ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.graphicaluserinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.R;
import ro.pub.cs.systems.pdsd.lab07.calculatorwebservice.general.Constants;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CalculatorWebServiceActivity extends Activity {

	private EditText operator1EditText, operator2EditText;
	private TextView resultTextView;
	private Spinner operationsSpinner, methodsSpinner;

	private static Context context;

	private class CalculatorWebServiceThread extends Thread {

		@Override
		public void run() {

			// TODO: exercise 4
			// get operators 1 & 2 from corresponding edit texts
			// (operator1EditText, operator2EditText)
			// signal missing values through error messages
			// get operation from operationsSpinner

			String op1 = operator1EditText.getText().toString();
			String op2 = operator2EditText.getText().toString();
			String operation = operationsSpinner.getSelectedItem().toString();
			String method = methodsSpinner.getSelectedItem().toString().toLowerCase();

			String result = null;

			// create an instance of a HttpClient object
			HttpClient httpClient = new DefaultHttpClient();

			// get method used for sending request from methodsSpinner

			// 1. GET
			// a) build the URL into a HttpGet object (append the operators /
			// operations to the Internet address)
			// b) create an instance of a ResultHandler object
			// c) execute the request, thus generating the result

			if (method.equals("get")) {
				HttpGet httpWebPageGet = new HttpGet(
						Constants.GET_WEB_SERVICE_ADDRESS
								+ Constants.OPERATION_ATTRIBUTE + "="
								+ operation + "&"
								+ Constants.OPERATOR1_ATTRIBUTE + "=" + op1
								+ "&" + Constants.OPERATOR2_ATTRIBUTE + "="
								+ op2);

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					result = httpClient
							.execute(httpWebPageGet, responseHandler);
					Log.i("RESPONSE", result);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 2. POST
			// a) build the URL into a HttpPost object
			// b) create a list of NameValuePair objects containing the
			// attributes and their values (operators, operation)
			// c) create an instance of a UrlEncodedFormEntity object using the
			// list and UTF-8 encoding and attach it to the post request
			// d) create an instance of a ResultHandler object
			// e) execute the request, thus generating the result

 			if (method.equals("post")) {

				HttpPost httpPost = new HttpPost(
						Constants.POST_WEB_SERVICE_ADDRESS);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(
						Constants.OPERATION_ATTRIBUTE, operationsSpinner
								.getSelectedItem().toString()));
				params.add(new BasicNameValuePair(
						Constants.OPERATOR1_ATTRIBUTE, op1));
				params.add(new BasicNameValuePair(
						Constants.OPERATOR2_ATTRIBUTE, op2));
				try {
					UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
							params, HTTP.UTF_8);
					httpPost.setEntity(urlEncodedFormEntity);
				} catch (UnsupportedEncodingException unsupportedEncodingException) {
					Log.e(Constants.TAG,
							unsupportedEncodingException.getMessage());
					if (Constants.DEBUG) {
						unsupportedEncodingException.printStackTrace();
					}
				}

				// display the result in resultTextView
				HttpEntity httpPostEntity = httpPost.getEntity();

				BufferedReader bufferedReader = null;
				final StringBuilder stringBuilder = new StringBuilder();
				try {
					// ...
					bufferedReader = new BufferedReader(new InputStreamReader(
							httpPostEntity.getContent()));
					int currentLineNumber = 0;
					String currentLineContent;
					while ((currentLineContent = bufferedReader.readLine()) != null) {
						currentLineNumber++;
						stringBuilder.append(currentLineNumber + ": "
								+ currentLineContent + "\n");
					}
					
					result = stringBuilder.toString();
					Log.i(Constants.TAG, stringBuilder.toString());
				} catch (Exception exception) {
					Log.e(Constants.TAG, exception.getMessage());
					if (Constants.DEBUG) {
						exception.printStackTrace();
					}
				} finally {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (IOException ioException) {
							Log.e(Constants.TAG, ioException.getMessage());
							if (Constants.DEBUG) {
								ioException.printStackTrace();
							}
						}
					}
				}
			}

			Handler mainHandler = new Handler(context.getMainLooper());
			final String endResult = result;
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					resultTextView.setText(endResult);

				}
			};
			mainHandler.post(myRunnable);

		}
	}

	private DisplayResultButtonClickListener displayResultButtonClickListener = new DisplayResultButtonClickListener();

	private class DisplayResultButtonClickListener implements
			Button.OnClickListener {

		@Override
		public void onClick(View view) {
			new CalculatorWebServiceThread().start();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculator_web_service);

		operator1EditText = (EditText) findViewById(R.id.operator1_edit_text);
		operator2EditText = (EditText) findViewById(R.id.operator2_edit_text);

		resultTextView = (TextView) findViewById(R.id.result_text_view);

		operationsSpinner = (Spinner) findViewById(R.id.operations_spinner);
		methodsSpinner = (Spinner) findViewById(R.id.methods_spinner);

		context = this;

		Button displayResultButton = (Button) findViewById(R.id.display_result_button);
		displayResultButton
				.setOnClickListener(displayResultButtonClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calculator_web, menu);
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
