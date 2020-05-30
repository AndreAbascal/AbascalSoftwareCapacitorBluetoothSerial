package br.com.abascalsoftware.capacitor.bluetoothserial;

import android.Manifest;
import android.content.pm.PackageManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

//import org.apache.cordova.CordovaArgs;
//import org.apache.cordova.CordovaPlugin;
//import org.apache.cordova.CallbackContext;
//import org.apache.cordova.PluginResult;
//import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * PhoneGap Plugin for Serial Communication over Bluetooth
 */
@NativePlugin()
public class CapacitorBluetoothSerial extends Plugin {

	// actions
	private static final String LIST = "list";
	private static final String CONNECT = "connect";
	private static final String CONNECT_INSECURE = "connectInsecure";
	private static final String DISCONNECT = "disconnect";
	private static final String WRITE = "write";
	private static final String AVAILABLE = "available";
	private static final String READ = "read";
	private static final String READ_UNTIL = "readUntil";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String SUBSCRIBE_RAW = "subscribeRaw";
	private static final String UNSUBSCRIBE_RAW = "unsubscribeRaw";
	private static final String IS_ENABLED = "isEnabled";
	private static final String IS_CONNECTED = "isConnected";
	private static final String CLEAR = "clear";
	private static final String SETTINGS = "showBluetoothSettings";
	private static final String ENABLE = "enable";
	private static final String DISCOVER_UNPAIRED = "discoverUnpaired";
	private static final String SET_DEVICE_DISCOVERED_LISTENER = "setDeviceDiscoveredListener";
	private static final String CLEAR_DEVICE_DISCOVERED_LISTENER = "clearDeviceDiscoveredListener";
	private static final String SET_NAME = "setName";
	private static final String SET_DISCOVERABLE = "setDiscoverable";

	// callbacks
	private PluginCall connectCallback;
	private PluginCall dataAvailableCallback;
	private PluginCall rawDataAvailableCallback;
	private PluginCall enableBluetoothCallback;
	private PluginCall deviceDiscoveredCallback;

	private BluetoothAdapter bluetoothAdapter;
	private CapacitorBluetoothSerialService capacitorBluetoothSerialService;

	// Debugging
	private static final String TAG = "BluetoothSerial";
	private static final boolean D = true;

	// Message types sent from the CapacitorBluetoothSerialService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_READ_RAW = 6;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	StringBuffer buffer = new StringBuffer();
	private String delimiter;
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;

	// Android 23 requires user to explicitly grant permission for location to
	// discover unpaired
	private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
	private static final int CHECK_PERMISSIONS_REQ_CODE = 2;
	private PluginCall permissionCallback;

	@PluginMethod()
	public void clear(PluginCall call) {
		buffer.setLength(0);
		call.resolve();
	}

	@PluginMethod()
	public void connect(PluginCall call) {
		String macAddress = call.getString("address");
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
		if (device != null) {
			// connectCallback = callbackContext;
			connectCallback = call;
			capacitorBluetoothSerialService.connect(device, true);
			buffer.setLength(0);
			// saveCall(call);
			// PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			// result.setKeepCallback(true);
			// callbackContext.sendPluginResult(result);
		} else {
			call.reject("Could not connect to " + macAddress);
		}
	}

	@PluginMethod()
	public void connectInsecure(PluginCall call) {
		String macAddress = call.getString("address");
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
		if (device != null) {
			// connectCallback = callbackContext;
			connectCallback = call;
			capacitorBluetoothSerialService.connect(device, false);
			buffer.setLength(0);
			// saveCall(call);
			// PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			// result.setKeepCallback(true);
			// callbackContext.sendPluginResult(result);
		} else {
			call.reject("Could not connect to " + macAddress);
		}
	}

	@PluginMethod()
	public void disconnect(PluginCall call) {
		connectCallback = null;
		capacitorBluetoothSerialService.stop();
		call.resolve();
	}

	@PluginMethod()
	public void enable(PluginCall call) {
		enableBluetoothCallback = call;
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(call, intent, REQUEST_ENABLE_BLUETOOTH);
	}

	@PluginMethod()
	public void isConnected(PluginCall call) {
		if (capacitorBluetoothSerialService.getState() == CapacitorBluetoothSerialService.STATE_CONNECTED) {
			call.resolve();
		} else {
			call.reject("Not connected.");
		}
	}

	@PluginMethod()
	public void isEnabled(PluginCall call) {
		if (bluetoothAdapter.isEnabled()) {
			call.resolve();
		} else {
			call.reject("Bluetooth is disabled.");
		}
	}

	@PluginMethod()
	public void subscribeRawData(PluginCall call) {
		rawDataAvailableCallback = call;
		// PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		// result.setKeepCallback(true);
		// callbackContext.sendPluginResult(result);
	}

	@PluginMethod()
	public void write(PluginCall call) {
		String _data = call.getString("data");
		byte[] data = _data.getBytes();
		capacitorBluetoothSerialService.write(data);
		call.resolve();
	}

	/*
	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

		LOG.d(TAG, "action = " + action);

		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		if (capacitorBluetoothSerialService == null) {
			capacitorBluetoothSerialService = new CapacitorBluetoothSerialService(mHandler);
		}

		boolean validAction = true;

		if (action.equals(LIST)) {

			listBondedDevices(callbackContext);

		} else if (action.equals(CONNECT)) {

			boolean secure = true;
			connect(args, secure, callbackContext);

		} else if (action.equals(CONNECT_INSECURE)) {

			// see Android docs about Insecure RFCOMM http://goo.gl/1mFjZY
			boolean secure = false;
			connect(args, secure, callbackContext);

		} else if (action.equals(DISCONNECT)) {

			connectCallback = null;
			capacitorBluetoothSerialService.stop();
			callbackContext.success();

		} else if (action.equals(WRITE)) {

			byte[] data = args.getArrayBuffer(0);
			capacitorBluetoothSerialService.write(data);
			callbackContext.success();

		} else if (action.equals(AVAILABLE)) {

			callbackContext.success(available());

		} else if (action.equals(READ)) {

			callbackContext.success(read());

		} else if (action.equals(READ_UNTIL)) {

			String interesting = args.getString(0);
			callbackContext.success(readUntil(interesting));

		} else if (action.equals(SUBSCRIBE)) {

			delimiter = args.getString(0);
			dataAvailableCallback = callbackContext;

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else if (action.equals(UNSUBSCRIBE)) {

			delimiter = null;

			// send no result, so Cordova won't hold onto the data available callback
			// anymore
			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			dataAvailableCallback.sendPluginResult(result);
			dataAvailableCallback = null;

			callbackContext.success();

		} else if (action.equals(SUBSCRIBE_RAW)) {

			rawDataAvailableCallback = callbackContext;

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else if (action.equals(UNSUBSCRIBE_RAW)) {

			rawDataAvailableCallback = null;

			callbackContext.success();

		} else if (action.equals(IS_ENABLED)) {

			if (bluetoothAdapter.isEnabled()) {
				callbackContext.success();
			} else {
				callbackContext.error("Bluetooth is disabled.");
			}

		} else if (action.equals(IS_CONNECTED)) {

			if (capacitorBluetoothSerialService.getState() == CapacitorBluetoothSerialService.STATE_CONNECTED) {
				callbackContext.success();
			} else {
				callbackContext.error("Not connected.");
			}

		} else if (action.equals(CLEAR)) {

			buffer.setLength(0);
			callbackContext.success();

		} else if (action.equals(SETTINGS)) {

			Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
			cordova.getActivity().startActivity(intent);
			callbackContext.success();

		} else if (action.equals(ENABLE)) {

			enableBluetoothCallback = callbackContext;
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			cordova.startActivityForResult(this, intent, REQUEST_ENABLE_BLUETOOTH);

		} else if (action.equals(DISCOVER_UNPAIRED)) {

			if (cordova.hasPermission(ACCESS_COARSE_LOCATION)) {
				discoverUnpairedDevices(callbackContext);
			} else {
				permissionCallback = callbackContext;
				cordova.requestPermission(this, CHECK_PERMISSIONS_REQ_CODE, ACCESS_COARSE_LOCATION);
			}

		} else if (action.equals(SET_DEVICE_DISCOVERED_LISTENER)) {

			this.deviceDiscoveredCallback = callbackContext;

		} else if (action.equals(CLEAR_DEVICE_DISCOVERED_LISTENER)) {

			this.deviceDiscoveredCallback = null;

		} else if (action.equals(SET_NAME)) {

			String newName = args.getString(0);
			bluetoothAdapter.setName(newName);
			callbackContext.success();

		} else if (action.equals(SET_DISCOVERABLE)) {

			int discoverableDuration = args.getInt(0);
			Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoverableDuration);
			cordova.getActivity().startActivity(discoverIntent);

		} else {
			validAction = false;

		}

		return validAction;
	}
	*/

	@Override
  	protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
		super.handleOnActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

			if (resultCode == Activity.RESULT_OK) {
				Log.d(TAG, "User enabled Bluetooth");
				if (enableBluetoothCallback != null) {
					enableBluetoothCallback.resolve();
				}
			} else {
				Log.d(TAG, "User did *NOT* enable Bluetooth");
				if (enableBluetoothCallback != null) {
					enableBluetoothCallback.error("User did not enable Bluetooth");
				}
			}

			enableBluetoothCallback = null;
		}
    }

	@Override
	public void handleOnStop() {
		super.handleOnStop();
		if (capacitorBluetoothSerialService != null) {
			capacitorBluetoothSerialService.stop();
		}
	}

	/*
	private void listBondedDevices(CallbackContext callbackContext) throws JSONException {
		JSONArray deviceList = new JSONArray();
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

		for (BluetoothDevice device : bondedDevices) {
			deviceList.put(deviceToJSON(device));
		}
		callbackContext.success(deviceList);
	}

	private void discoverUnpairedDevices(final CallbackContext callbackContext) throws JSONException {

		final CallbackContext ddc = deviceDiscoveredCallback;

		final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {

			private JSONArray unpairedDevices = new JSONArray();

			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					try {
						JSONObject o = deviceToJSON(device);
						unpairedDevices.put(o);
						if (ddc != null) {
							PluginResult res = new PluginResult(PluginResult.Status.OK, o);
							res.setKeepCallback(true);
							ddc.sendPluginResult(res);
						}
					} catch (JSONException e) {
						// This shouldn't happen, log and ignore
						Log.e(TAG, "Problem converting device to JSON", e);
					}
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					callbackContext.success(unpairedDevices);
					cordova.getActivity().unregisterReceiver(this);
				}
			}
		};

		Activity activity = cordova.getActivity();
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		bluetoothAdapter.startDiscovery();
	}
	

	private JSONObject deviceToJSON(BluetoothDevice device) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", device.getName());
		json.put("address", device.getAddress());
		json.put("id", device.getAddress());
		if (device.getBluetoothClass() != null) {
			json.put("class", device.getBluetoothClass().getDeviceClass());
		}
		return json;
	}

	private void connect(CordovaArgs args, boolean secure, CallbackContext callbackContext) throws JSONException {
		String macAddress = args.getString(0);
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

		if (device != null) {
			connectCallback = callbackContext;
			capacitorBluetoothSerialService.connect(device, secure);
			buffer.setLength(0);

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else {
			callbackContext.error("Could not connect to " + macAddress);
		}
	}
	*/

	// The Handler that gets information back from the CapacitorBluetoothSerialService
	// Original code used handler for the because it was talking to the UI.
	// Consider replacing with normal callbacks
	private final Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_READ:
					buffer.append((String) msg.obj);

					if (dataAvailableCallback != null) {
						// sendDataToSubscriber();
					}

					break;
				case MESSAGE_READ_RAW:
					if (rawDataAvailableCallback != null) {
						byte[] bytes = (byte[]) msg.obj;
						sendRawDataToSubscriber(bytes);
					}
					break;
				case MESSAGE_STATE_CHANGE:

					if (D)
						Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
						case CapacitorBluetoothSerialService.STATE_CONNECTED:
							Log.i(TAG, "CapacitorBluetoothSerialService.STATE_CONNECTED");
							notifyConnectionSuccess();
							break;
						case CapacitorBluetoothSerialService.STATE_CONNECTING:
							Log.i(TAG, "CapacitorBluetoothSerialService.STATE_CONNECTING");
							break;
						case CapacitorBluetoothSerialService.STATE_LISTEN:
							Log.i(TAG, "CapacitorBluetoothSerialService.STATE_LISTEN");
							break;
						case CapacitorBluetoothSerialService.STATE_NONE:
							Log.i(TAG, "CapacitorBluetoothSerialService.STATE_NONE");
							break;
					}
					break;
				case MESSAGE_WRITE:
					// byte[] writeBuf = (byte[]) msg.obj;
					// String writeMessage = new String(writeBuf);
					// Log.i(TAG, "Wrote: " + writeMessage);
					break;
				case MESSAGE_DEVICE_NAME:
					Log.i(TAG, msg.getData().getString(DEVICE_NAME));
					break;
				case MESSAGE_TOAST:
					String message = msg.getData().getString(TOAST);
					notifyConnectionLost(message);
					break;
			}
		}
	};

	private void notifyConnectionLost(String error) {
		if (connectCallback != null) {
			connectCallback.reject(error);
			connectCallback = null;
		}
	}

	private void notifyConnectionSuccess() {
		if (connectCallback != null) {
			connectCallback.resolve();
			connectCallback = null;
			// PluginResult result = new PluginResult(PluginResult.Status.OK);
			// result.setKeepCallback(true);
			// connectCallback.sendPluginResult(result);
		}
	}

	private void sendRawDataToSubscriber(byte[] data) {
		if (data != null && data.length > 0) {
			if (rawDataAvailableCallback == null) {
				return;
			}
			JSObject obj = new JSObject();
			obj.put("data", data);
			rawDataAvailableCallback.resolve(obj);
			// PluginResult result = new PluginResult(PluginResult.Status.OK, data);
			// result.setKeepCallback(true);
			// rawDataAvailableCallback.sendPluginResult(result);
		}
	}

	private void sendDataToSubscriber() {
		String data = readUntil(delimiter);
		if (data != null && data.length() > 0) {
			if(dataAvailableCallback == null){
				return;
			}
			JSObject obj = new JSObject();
			obj.put("data", data);
			rawDataAvailableCallback.resolve(obj);
			sendDataToSubscriber();
		}
	}

	private int available() {
		return buffer.length();
	}

	private String read() {
		int length = buffer.length();
		String data = buffer.substring(0, length);
		buffer.delete(0, length);
		return data;
	}

	private String readUntil(String c) {
		String data = "";
		int index = buffer.indexOf(c, 0);
		if (index > -1) {
			data = buffer.substring(0, index + c.length());
			buffer.delete(0, index + c.length());
		}
		return data;
	}
}
