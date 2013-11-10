package com.iha.wcc;

import java.util.ArrayList;
import java.util.List;

import com.iha.wcc.data.Network;
import com.iha.wcc.interfaces.fragment.INetworkFragmentInteractionListener;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;



/**
 * A fragment representing a list of Device.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class NetworkFragment extends ListFragment {
	private WifiManager wifiManager;
	private INetworkFragmentInteractionListener mListener;
	private ArrayAdapter<Network> adapter;
	private List<Network> networks;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public NetworkFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Define the adapter.
		this.adapter = new ArrayAdapter<Network>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
		
		// Refresh the list content, that will define the adapter content.
		this.refreshList();		

		setListAdapter(this.adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (INetworkFragmentInteractionListener) activity;
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
			mListener.onNetworkFragmentInteraction(this.networks.get(position).name, this.networks.get(position).ip);
		}
	}
	
	/**
	 * Refresh the devices variable by loading devices from network and refresh the view.
	 */
	@SuppressWarnings("deprecation")
	public void refreshList(){
		// Force refresh WIFI manager.
		this.refreshWifiManager();
		
		networks = new ArrayList<Network>();
		networks.add(new Network(this.wifiManager.getConnectionInfo().getSSID(), Formatter.formatIpAddress(this.wifiManager.getConnectionInfo().getIpAddress())));

		// Clear, add all and notify the view.
		adapter.clear();
		adapter.addAll(networks);
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * Refresh the Wifi manager. Useful when the connection is lost or shutdown.
	 */
	public void refreshWifiManager(){
		this.wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
	}

}
