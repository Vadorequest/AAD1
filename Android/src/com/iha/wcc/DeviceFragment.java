package com.iha.wcc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iha.wcc.data.Device;


/**
 * A fragment representing a list of Device.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class DeviceFragment extends ListFragment {

	private OnFragmentInteractionListener mListener;
	private ArrayAdapter<Device> adapter;
	private List<Device> devices;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public DeviceFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Define the adapter.
		this.adapter = new ArrayAdapter<Device>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
		
		// Refresh the list content, that will define the adapter content.
		this.refreshList();		

		setListAdapter(this.adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mListener.onFragmentInteraction(this.devices.get(position).id);
		}
	}
	
	/**
	 * Refresh the devices variable by loading devices from network and refresh the view.
	 */
	public void refreshList(){
		devices = new ArrayList<Device>();
		// TODO Load these data by the network.
		devices.add(new Device((devices.size()+1)+"", "Car #123"));
		devices.add(new Device((devices.size()+1)+"", "Phone xxx"));
		
		// Clear, add all and notify the view.
		adapter.clear();
		adapter.addAll(devices);
		adapter.notifyDataSetChanged();
	}

	/**
	 * This interface must be implemented by activities that contain this fragment to allow an interaction in this fragment to be communicated to the activity
	 * and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with Other
	 * Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(String id);
	}

}
