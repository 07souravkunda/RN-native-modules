import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  PermissionsAndroid,, Alert
} from 'react-native';
const GetIMEI = (props) => {
  const [imei,setImei] = useState();
  const get = async () => {
    const status = PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE
    );
    if(status === PermissionsAndroid.RESULTS.GRANTED){
        const num = await IMEI.getImei();
        if(typeof num == 'string'){
            setImei(num);
            console.log(num);
        }else{
            console.log(num);
            setImei(num.join(', '));
        }
    }else{
        setImei('');
        Alert.alert('Oops!','Please give permission!');
    }
  };
  useEffect(() => {});
  return (
    <View style={styles.screen}>
      {imei ? (
        <Text style={styles.text}>{imei}</Text>
      ) : (
        <ActivityIndicator size="large" color="black" />
      )}
    </View>
  );
};
export default GetIMEI;
const styles = StyleSheet.create({
  screen: { alignItems: 'center', justifyContent: 'center', flex: 1 },
  text: { fontSize: 20, fontWeight: 'bold', textAlign: 'center' },
});
