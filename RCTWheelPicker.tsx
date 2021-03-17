import {requireNativeComponent, ViewProps} from "react-native";

export default requireNativeComponent<ViewProps & {data: string[], onChange: (e: any) => void}>('RCTWheelPicker');