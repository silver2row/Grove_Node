package com.seeedstudio.ble.node;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SensorActivity extends DeviceBaseActivity {
	static final String TAG = "Node Sensor";

	private SensorData[] mSensorData;

	private DataCenter mDataCenter;
	private ListView mDataListView;
	private SensorDataArrayAdapter mDataListAdapter;

	private ListView mEventListView;
	private ArrayAdapter<String> mEventListAdapter;
	private ArrayList<String> mEventNameList;

	private ImageView mTypeImageView;
	private TextView mOperatorTextView;
	private EditText mValueEditText;
	
	private int mTypeImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int id = getIntent().getExtras().getInt("sensor");

		mDataCenter = DataCenter.getInstance();
		Grove sensor = mDataCenter.getSensors()[id];

		setTitle(sensor.name);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.sensor);

		mSensorData = (SensorData[]) sensor.data;
		mDataListAdapter = new SensorDataArrayAdapter(this, mSensorData);
		mDataListView = (ListView) findViewById(R.id.data_list_view);
		mDataListView.setAdapter(mDataListAdapter);

		mEventNameList = (ArrayList<String>) mDataCenter.getEventNameList()
				.clone();
		mEventListAdapter = new ArrayAdapter<String>(this, R.layout.device_row,
				mEventNameList);
		mEventListView = (ListView) findViewById(R.id.event_list_view);
		mEventListView.setAdapter(mEventListAdapter);

		View header = getLayoutInflater().inflate(R.layout.list_header, null);
		TextView text = (TextView) header.findViewById(R.id.header_text_view);
		text.setText("Event List");

		mEventListView.addHeaderView(header);

		// Create a ListView-specific touch listener. ListViews are given
		// special treatment because
		// by default they handle touches for their list items... i.e. they're
		// in charge of drawing
		// the pressed state (the list selector), handling list item clicks,
		// etc.
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				mEventListView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							String item = mEventListAdapter
									.getItem(position - 1);
							mEventListAdapter.remove(item);
							mDataCenter.removeEvent(item);
						}
						mEventListAdapter.notifyDataSetChanged();
					}
				});
		mEventListView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		mEventListView.setOnScrollListener(touchListener.makeScrollListener());

		mTypeImageView = (ImageView) findViewById(R.id.type_image_view);
		mOperatorTextView = (TextView) findViewById(R.id.operator_text_view);
		mValueEditText = (EditText) findViewById(R.id.value_edit_text);
		
		mTypeImage = 0;
		mTypeImageView.setImageResource(mSensorData[mTypeImage].image);
	}

	public void changeType(View v) {
		mTypeImage++;
		if (mTypeImage >= mSensorData.length) {
			mTypeImage = 0;
		}
		
		mTypeImageView.setImageResource(mSensorData[mTypeImage].image);
	}

	public void changeOperator(View v) {
		String operator = mOperatorTextView.getText().toString();
		if (operator.equals(">")) {
			operator = "<";
		} else if (operator.equals("<")) {
			operator = "=";
		} else {
			operator = ">";
		}

		mOperatorTextView.setText(operator);
	}

	public void addEvent(View v) {
		String operator = mOperatorTextView.getText().toString();
		String value = mValueEditText.getText().toString();
		String equation = mSensorData[mTypeImage].name + " " + operator + " " + value;
		SensorEvent event = new SensorEvent();
		event.type = 0;
		event.operator = operator.charAt(0);
		try {
			event.value = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			Log.d(TAG, "Invalid Input");
			return;
		}

		int n = mDataCenter.getEventNumber();
		mDataCenter.addEvent(equation, event);

		mEventListAdapter.insert(equation, 0);
	}
	
	@Override
	protected void onDeviceDataReceived(byte[] data) {
		String rxString = null;
		try {
			rxString = new String(data, "UTF-8");
			Log.d(TAG, "RX: " + rxString);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString());
			return;
		}
		
		String[] slices = rxString.split(" ");
		if (slices[0].equals("i") && (slices.length == 3)) {
			int dimention = Integer.parseInt(slices[1]);
			float value = Float.parseFloat(slices[2]);
			float ten = 10;
			value = ((int) (value * 10)) / ten;
			if (dimention < mSensorData.length && mSensorData[dimention].data != value) {
				mSensorData[dimention].data = value;
				mDataListAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.action_done) {
			Intent intent = new Intent(this, NodeActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
