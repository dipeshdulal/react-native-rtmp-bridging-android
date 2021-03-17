import React, { Component } from "react";
import { findNodeHandle, requireNativeComponent, UIManager, ViewProps } from "react-native";

const RCTCameraView = requireNativeComponent<ViewProps>('RCTCameraView');

interface CameraViewProps extends ViewProps {
    streamWidth?: number;
    streamHeight?: number;
    streamUrl?: string;
    audioMuted?: boolean;
    videoMuted?: boolean;
    camera?: "front" | "back";
}

export class CameraView extends Component<CameraViewProps, unknown>{

    static defaultProps: Partial<CameraViewProps> = {
        camera: "front",
        audioMuted: false,
        videoMuted: false,
        streamWidth: 720,
        streamHeight: 1280,
    }

    nativeViewCommand = (cmdName: string, cmdArgs: string[] = []) => {
        try {
            UIManager.dispatchViewManagerCommand(
                findNodeHandle(this),
                cmdName,
                cmdArgs
            )
        } catch (e) {
            console.warn(e);
        }
    }

    startPublish = (key: string) => this.nativeViewCommand("startPublish", [key]);
    stopPublish = () => this.nativeViewCommand("stopPublish", []);

    componentDidMount() {
        this.nativeViewCommand("startPublish");
    }

    render() {
        return <RCTCameraView {...this.props} />
    }

}