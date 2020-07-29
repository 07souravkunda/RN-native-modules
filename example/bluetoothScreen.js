import React, { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  NativeModules,
  PermissionsAndroid,
  TouchableNativeFeedback,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';

const Bluetooth = NativeModules.Bluetooth;

const ButtonComp = (props) => {
  return (
    <TouchableNativeFeedback onPress={props.onPress}>
      <View style={styles.button}>
        <Text style={styles.buttonText}>{props.children}</Text>
      </View>
    </TouchableNativeFeedback>
  );
};

const DeviceComp = (props) => {
  return (
    <TouchableNativeFeedback
      background={TouchableNativeFeedback.Ripple('white')}
    >
      <View
        style={{
          width: '80%',
          backgroundColor: 'black',
          paddingVertical: 10,
          paddingHorizontal: 15,
          borderRadius: 5,
          marginTop: 10,
        }}
      >
        <Text
          style={{ color: 'white', fontSize: 18, fontWeight: 'bold' }}
          key={props.id}
        >
          {props.name}
        </Text>
      </View>
    </TouchableNativeFeedback>
  );
};

const BluetoothScreen = () => {
  const [devices, setDevices] = useState([]);
  const [pairedDevices, setPairedDevices] = useState([]);
  const [enabled, setEnabled] = useState();
  const [loading, setLoading] = useState(false);

  const accessPermissions = useCallback(async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        {
          title: 'Location permission for bluetooth scanning',
          message: 'wahtever',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        }
      );
      console.log(granted);
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        return true;
      }
    } catch (er) {
      console.log(er);
    }
  }, []);
  const getBluetooth = useCallback(async () => {
    try {
      setLoading(true);
      const resp = await Bluetooth.discoverDevices();
      setDevices(resp);
    } catch (er) {
      console.log(er);
    }
    setLoading(false);
  }, []);
  const getPairedDevices = useCallback(async () => {
    try {
      const res = await Bluetooth.getPairedDevices();
      console.log(res, 'paired');
      setPairedDevices(res);
    } catch (er) {
      console.log(er);
    }
  }, []);
  const enableBluetooth = useCallback(async () => {
    try {
      console.log('hello');
      await Bluetooth.requestEnable();
      setEnabled(true);
    } catch (er) {
      console.log(er);
    }
  }, []);
  const checkBluetooth = useCallback(async () => {
    try {
      const bt = await Bluetooth.isEnabled();
      console.log(bt, 'bt');
      setEnabled(bt);
    } catch (er) {
      console.log(er);
    }
  }, []);
  useEffect(() => {
    accessPermissions();
    checkBluetooth();
  }, [accessPermissions, checkBluetooth]);
  return (
    <View style={{ justifyContent: 'center', alignItems: 'center' }}>
      {enabled ? (
        <ButtonComp
          onPress={async () => {
            await Bluetooth.turnOff();
            setEnabled(false);
          }}
        >
          Turn off
        </ButtonComp>
      ) : (
        <ButtonComp onPress={enableBluetooth}>Turn On</ButtonComp>
      )}
      {loading ? (
        <ActivityIndicator size="large" color="green" />
      ) : (
        <ButtonComp onPress={getBluetooth}>Scan</ButtonComp>
      )}
      <ButtonComp onPress={getPairedDevices}>Show paired devices</ButtonComp>
      <Text>Availaible devices</Text>
      {devices.map((el, ind) => (
        <DeviceComp key={el.address} id={el.address} name={el.name} />
      ))}
      <Text>Paired devices</Text>
      {pairedDevices.map((el) => (
        <DeviceComp key={el.address} name={el.name} />
      ))}
    </View>
  );
};

export default BluetoothScreen;

const styles = StyleSheet.create({
  button: {
    width: '90%',
    backgroundColor: 'green',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 10,
    marginVertical: 10,
    borderRadius: 10,
  },
  buttonText: {
    fontSize: 15,
    fontWeight: 'bold',
    color: 'white',
  },
});
