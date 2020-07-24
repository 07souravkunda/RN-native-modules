package com.yourpackage;

import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.app.Activity;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

public class BluetoothModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static ReactApplicationContext reactContext;
    private ReactApplicationContext ReactContext;
    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    private BroadcastReceiver mBluetoothDiscoveryReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String, String> unpairedDevices;
    private Set<BluetoothDevice> pairedDevices;
    Promise promise = null;
    Promise discoverPromise = null;

    BluetoothModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        this.ReactContext = context;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "Toast";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                if (promise != null) {
                    promise.resolve(true);
                }
            } else {
                if (promise != null) {
                    Toast.makeText(getReactApplicationContext(), "user denied", Toast.LENGTH_SHORT).show();
                    promise.reject(new Exception("User did not enable Bluetooth"));
                }
            }
            promise = null;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    @ReactMethod
    public void get(int val, Promise p) {
        p.resolve(2);
    }

    @ReactMethod
    public void getPairedDevices(Promise prom) {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        WritableArray deviceArray = Arguments.createArray();
        for (BluetoothDevice device : pairedDevices) {
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            WritableMap map = Arguments.createMap();
            map.putString("name", deviceName);
            map.putString("address", deviceHardwareAddress);
            deviceArray.pushMap(map);
        }
        prom.resolve(deviceArray);
    }

    @ReactMethod
    public void turnOff(Promise prom) {
        mBluetoothAdapter.disable();
        prom.resolve(true);
        Toast.makeText(getReactApplicationContext(), "turned off bluetooth", Toast.LENGTH_SHORT).show();
    }

    @ReactMethod
    public void discoverDevices(Promise p) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoverPromise = p;
        unpairedDevices = new HashMap<>();
        mBluetoothDiscoveryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if (!unpairedDevices.containsKey(device.getAddress())) {
                        unpairedDevices.put(device.getAddress(), deviceName);
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    WritableArray deviceArray = Arguments.createArray();
                    for (Map.Entry<String, String> entry : unpairedDevices.entrySet()) {
                        WritableMap map = Arguments.createMap();
                        map.putString("name", entry.getValue());
                        map.putString("address", entry.getKey());
                        deviceArray.pushMap(map);
                    }
                    discoverPromise.resolve(deviceArray);
                    discoverPromise = null;
                    ReactContext.unregisterReceiver(mBluetoothDiscoveryReceiver);
                }
            }
        };
        ReactContext.registerReceiver(mBluetoothDiscoveryReceiver, intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    @ReactMethod
    public void bluetoothCheck(Promise p) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = bluetoothAdapter;
        if (bluetoothAdapter == null) {
            Toast.makeText(getReactApplicationContext(), "bluetooth no suported", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Activity activity = getCurrentActivity();
                promise = p;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 0);
                Toast.makeText(getReactApplicationContext(), "bluetooth is turned on", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getReactApplicationContext(), "bluetooth is on", Toast.LENGTH_SHORT).show();
                p.resolve(true);
            }
        }
    }

}