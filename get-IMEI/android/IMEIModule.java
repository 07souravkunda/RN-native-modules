package com.yourpackage;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.provider.Settings.Secure;

import java.util.Map;
import java.util.HashMap;
import android.widget.Toast;

public class IMEIModule extends ReactContextBaseJavaModule {
  private static ReactApplicationContext reactContext;
  private TelephonyManager tm;

  IMEIModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    tm = (TelephonyManager) reactContext.getSystemService(Context.TELEPHONY_SERVICE);
  }

  @Override
  public String getName() {
    return "IMEI";
  }

  @SuppressLint({ "MissingPermission", "HardwareIds" })
  @ReactMethod
  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return reactContext
          .checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    } else
      return true;
  }

  @ReactMethod
  public void getImei(Promise promise) {
    if (!hasPermission()) {
      promise.reject(new RuntimeException("Missing permission " + Manifest.permission.READ_PHONE_STATE));
    } else {
      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        String deviceId = Secure.getString(reactContext.getContentResolver(), Secure.ANDROID_ID);
        promise.resolve(deviceId);
      } else if (Build.VERSION.SDK_INT >= 23) {
        int count = tm.getPhoneCount();
        String[] imei = new String[count];
        for (int i = 0; i < count; i++) {
          if (Build.VERSION.SDK_INT >= 26) {
            imei[i] = tm.getImei(i);
          } else {
            imei[i] = tm.getDeviceId(i);
          }
        }
        promise.resolve(Arguments.fromJavaArgs(imei));
      } else {
        promise.resolve(Arguments.fromJavaArgs(new String[] { tm.getDeviceId() }));
      }
    }
  }
}