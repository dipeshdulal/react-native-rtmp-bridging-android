/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React, { useRef, useState } from 'react';
import {
  Button,
  View,
  SafeAreaView,
  StatusBar,
  useColorScheme,
} from 'react-native';
import { CameraView } from './RCTCameraView';

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';
  const cameraViewRef = useRef<CameraView>(null);

  const [audioMuted, setAudioMuted] = useState<boolean>(false);
  const [videoMuted, setVideoMuted] = useState<boolean>(false);
  const [isFront, setIsFront] = useState<boolean>(true);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <CameraView
        style={{ flex: 1 }}
        streamWidth={720}
        streamHeight={1280}
        ref={cameraViewRef}
        audioMuted={audioMuted}
        videoMuted={videoMuted}
        camera={isFront ? "front" : "back"}
        streamUrl="rtmp://global-live.mux.com:5222/app/" />
      <View style={{
        flexDirection: "row",
        justifyContent: "space-evenly",
        position: "absolute",
        bottom: 0,
        width: "100%",
        marginBottom: 10,
        flexWrap: "wrap"
      }}>
        <Button
          title="Start"
          onPress={() => {
            cameraViewRef.current?.startPublish("95a9ae5a-4270-1bae-f3bb-35b9936f2de3");
          }} />
        <Button
          title="Stop"
          onPress={() => {
            cameraViewRef.current?.stopPublish();
          }} />
        <Button
          title={audioMuted ? "UM A" : "M A"}
          onPress={() => {
            setAudioMuted(m => !m);
          }} />
        <Button
          title={videoMuted ? "UM V" : "M V"}
          onPress={() => {
            setVideoMuted(v => !v);
          }} />

        <Button
          title="SC"
          onPress={() => {
            setIsFront(f => !f)
          }} />
      </View>
    </SafeAreaView>
  );
};

export default App;
