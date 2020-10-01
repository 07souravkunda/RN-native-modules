import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  Button,
  PermissionsAndroid,
  NativeModules,, Alert
} from 'react-native';
const SendSMS = NativeModules.SendSMS;

const SendSMS = (props) => {
  const [text, setText] = useState('');
  const [number, setNumber] = useState('');
  const sendSmsHandler = async () => {
    const grant = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.SEND_SMS
    );
    if (grant !== PermissionsAndroid.RESULTS.GRANTED) {
      return;
    }
    const resp = await SendSMS.sendSms(number, text);
    Alert.alert('success!','message sent');
  };
  return (
    <View style={styles.screen}>
      <TextInput
        style={styles.input}
        placeholder="Enter message content"
        value={text}
        onChangeText={(str) => setText(str)}
      />
      <TextInput
        style={styles.input}
        placeholder="Enter message content"
        value={number}
        keyboardType="phone-pad"
        onChangeText={(str) => setNumber(str)}
      />
      <Button title="send" color="green" />
    </View>
  );
};
export default SendSMS;
const styles = StyleSheet.create({
  screen: { alignItems: 'center', justifyContent: 'center' },
  input: {
    width: '80%',
    borderBottomColor: '#9f86c0',
    borderBottomWidth: 2,
    marginVertical: 10,
    padding: 5,
  },
});
