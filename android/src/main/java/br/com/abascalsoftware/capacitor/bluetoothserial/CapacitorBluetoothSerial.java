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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * PhoneGap Plugin for Serial Communication over Bluetooth
 */
@NativePlugin(
	permissions = {
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.BLUETOOTH,
		Manifest.permission.BLUETOOTH_ADMIN
	},
	requestCodes = {
		CapacitorBluetoothSerial.REQUEST_ENABLE_BLUETOOTH
	}
)
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
	private static final String PERMISSION_BT_DENIED = "É necessário permitir o uso do Bluetooth para prosseguir.";

	// callbacks
	private PluginCall connectCallback;
	private PluginCall dataAvailableCallback;
	private PluginCall rawDataAvailableCallback;
	private PluginCall enableBluetoothCallback;
	private PluginCall deviceDiscoveredCallback;

	private BluetoothAdapter bluetoothAdapter;
	private CapacitorBluetoothSerialService capacitorBluetoothSerialService;

	// Debugging
	private static final String TAG = "CapBluSer";
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
	public static final int REQUEST_ENABLE_BLUETOOTH = 43937;

	// Android 23 requires user to explicitly grant permission for location to
	// discover unpaired
	private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
	private static final int CHECK_PERMISSIONS_REQ_CODE = 2;
	private PluginCall permissionCallback;

	private void checkEntities(){
		Log.d(TAG,"checkEntities 01");
		if (this.bluetoothAdapter == null) {
			Log.d(TAG,"checkEntities 01.1");
			this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			Log.d(TAG,"checkEntities 01.2");
		}
		Log.d(TAG,"checkEntities 02");
		if (this.capacitorBluetoothSerialService == null) {
			Log.d(TAG,"checkEntities 02.1");
			this.capacitorBluetoothSerialService = new CapacitorBluetoothSerialService(mHandler);
			Log.d(TAG,"checkEntities 02.2");
		}
		Log.d(TAG,"checkEntities 03");
	}

	@PluginMethod()
	public void clear(PluginCall call) {
		saveCall(call);
		Log.d(TAG,"clear 01");
		this.checkEntities();
		Log.d(TAG,"clear 02");
		buffer.setLength(0);
		Log.d(TAG,"clear 03");
		call.resolve();
	}

	@PluginMethod()
	public void connect(PluginCall call) {
		Log.d(TAG,"connect 01");
		saveCall(call);
		Log.d(TAG,"connect 02");
		this.checkEntities();
		Log.d(TAG,"connect 03");
		JSObject data = call.getData();
		Log.d(TAG,"connect call data: "+data.toString());
		String macAddress = call.getString("address");
		Log.d(TAG,"connect 04");
		BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(macAddress);
		Log.d(TAG,"connect 05");
		if (device != null) {
			Log.d(TAG,"connect 05.1");
			connectCallback = call;
			Log.d(TAG,"connect 05.2");
			this.capacitorBluetoothSerialService.connect(device, true);
			Log.d(TAG,"connect 05.3");
			buffer.setLength(0);
			Log.d(TAG,"connect 05.4");
		} else {
			Log.d(TAG,"connect 06");
			call.reject("Could not connect to " + macAddress);
		}
	}

	@PluginMethod()
	public void connectInsecure(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		String macAddress = call.getString("address");
		BluetoothDevice device = this.bluetoothAdapter.getRemoteDevice(macAddress);
		if (device != null) {
			connectCallback = call;
			this.capacitorBluetoothSerialService.connect(device, false);
			buffer.setLength(0);
		} else {
			call.reject("Could not connect to " + macAddress);
		}
	}

	@PluginMethod()
	public void disconnect(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		connectCallback = null;
		this.capacitorBluetoothSerialService.stop();
		call.resolve();
	}

	@PluginMethod()
	public void enable(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		enableBluetoothCallback = call;
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(call, intent, REQUEST_ENABLE_BLUETOOTH);
	}

	@PluginMethod()
	public void isConnected(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		if (this.capacitorBluetoothSerialService.getState() == CapacitorBluetoothSerialService.STATE_CONNECTED) {
			call.resolve();
		} else {
			call.reject("Not connected.");
		}
	}

	@PluginMethod()
	public void isEnabled(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		if (this.bluetoothAdapter.isEnabled()) {
			call.resolve();
		} else {
			call.reject("Bluetooth is disabled.");
		}
	}

	@PluginMethod()
	public void subscribeRawData(PluginCall call) {
		saveCall(call);
		this.checkEntities();
		rawDataAvailableCallback = call;
		// PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		// result.setKeepCallback(true);
		// callbackContext.sendPluginResult(result);
	}

	@PluginMethod()
	public void write(PluginCall call) throws JSONException {
		saveCall(call);
		this.checkEntities();
		Log.d(TAG,"write call data: "+call.getData().toString());
		JSONArray data = call.getArray("data");
		int[] arr = new int[data.length()];
		for(int i=0;i<data.length();i++){
			arr[i] = data.getInt(i);
			Log.d(TAG,String.valueOf(i)+": ");
			Log.d(TAG,String.valueOf(arr[i]));
		}
		byte[] buffer = writeInts(arr);
		this.capacitorBluetoothSerialService.write(buffer);
		call.resolve();
	}

	private static byte[] writeInts(int[] array) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(array.length * 4);
            DataOutputStream dos = new DataOutputStream(bos);
            for (int i = 0; i < array.length; i++) {
                dos.writeInt(array[i]);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
		if (this.capacitorBluetoothSerialService != null) {
			this.capacitorBluetoothSerialService.stop();
		}
	}

	@Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result:grantResults) {
            if(result == PackageManager.PERMISSION_DENIED) {
				Log.d(TAG, "User *rejected* location permission");
				PluginCall savedCall = getSavedCall();
        		if (savedCall == null) {
					Log.d(TAG, "No stored plugin call for permissions request result");
					return;
				}
				savedCall.reject(PERMISSION_BT_DENIED);
                return;
            }
        }
    }

	// The Handler that gets information back from the CapacitorBluetoothSerialService
	// Original code used handler for the because it was talking to the UI.
	// Consider replacing with normal callbacks
	private final Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
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
					byte[] writeBuf = (byte[]) msg.obj;
					String writeMessage = new String(writeBuf);
					Log.i(TAG, "Wrote: " + writeMessage);
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
