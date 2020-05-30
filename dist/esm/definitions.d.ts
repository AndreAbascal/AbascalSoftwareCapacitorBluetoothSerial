declare module "@capacitor/core" {
    interface PluginRegistry {
        CapacitorBluetoothSerial: CapacitorBluetoothSerialPlugin;
    }
}
export interface CapacitorBluetoothSerialPlugin {
    clear(): Promise<any>;
    connect(address: string): Promise<any>;
    connectInsecure(address: string): Promise<any>;
    disconnect(): Promise<any>;
    enable(): Promise<any>;
    isConnected(): Promise<any>;
    isEnabled(): Promise<any>;
    subscribeRawData(): Promise<any>;
    write(data: any): Promise<any>;
}
