package com.seeedstudio.ble.node;


import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class MapActivity extends DeviceBaseActivity {
	private static final String TAG = "Node Mapping";
	
	private Spinner mEventSpinner;
	private Spinner mActionSpinner;
	private ListView mListView;
	
	ArrayAdapter<String> mEventAdapter;
	ArrayAdapter<String> mActionAdapter;
	ArrayAdapter<String> mListAdapter;
	DataCenter mDataCenter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		mDataCenter = DataCenter.getInstance();
		
		ArrayList<SensorEvent> eventList = mDataCenter.sensorEventList;
		ArrayList<String> eventNameList = new ArrayList<String>();
		for (SensorEvent event : eventList) {
			eventNameList.add(event.toString());
		}
		mEventAdapter = new ArrayAdapter<String>(this, R.layout.device_row, eventNameList);
		
		ArrayList<String> actionList = mDataCenter.getActionNameList();
		mActionAdapter = new ArrayAdapter<String>(this, R.layout.device_row, actionList);
		
		mEventSpinner = (Spinner) findViewById(R.id.event_spinner);
		mActionSpinner = (Spinner) findViewById(R.id.action_spinner);
		
		mEventSpinner.setAdapter(mEventAdapter);
		mActionSpinner.setAdapter(mActionAdapter);
		
		ArrayList<String> list = new ArrayList<String>();
		mListAdapter = new ArrayAdapter<String>(this, R.layout.device_row, list);
		
		mListView = (ListView) findViewById(R.id.ifttt_list_view);
		mListView.setAdapter(mListAdapter);
		
		// Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                		mListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	mListAdapter.remove(mListAdapter.getItem(position));
                                }
                                mListAdapter.notifyDataSetChanged();
                            }
                        });
        mListView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mListView.setOnScrollListener(touchListener.makeScrollListener());
	}
	
	public void onAddButtonClick(View v) {
		if (mEventAdapter.isEmpty()) {
			showMessage("Please selet a sensor and add some events first");
			return;
		}
		
		if (mActionAdapter.isEmpty()) {
			showMessage("Please selet a actuator and add some actions first");
			return;
		}
		
		String eventName = mEventSpinner.getSelectedItem().toString();
		String actionName = mActionSpinner.getSelectedItem().toString();
		String ifttt = "if " + eventName + " then " + actionName;
		if (0 > mListAdapter.getPosition(ifttt)) {
			mListAdapter.add(ifttt);
		}
		
		int eventPosition = mEventSpinner.getSelectedItemPosition();
		int actionIndex = mDataCenter.getActionIndex(actionName);
		
		String command = "m " + eventPosition + " " + actionIndex;
		configureDevice(command.getBytes());
		
		Log.v(TAG, "Mapping: " + command);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
