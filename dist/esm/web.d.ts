import { WebPlugin } from '@capacitor/core';
import { CapacitorBluetoothSerialPlugin } from './definitions';
export declare class CapacitorBluetoothSerialWeb extends WebPlugin implements CapacitorBluetoothSerialPlugin {
    constructor();
    clear(): Promise<any>;
    connect(address: string): Promise<any>;
    connectInsecure(address: string): Promise<any>;
    disconnect(): Promise<any>;
    enable(): Promise<any>;
    isConnected(): Promise<any>;
    isEnabled(): Promise<any>;
    subscribeRawData(): Promise<any>;
    write(buffer: Uint8Array | string): Promise<any>;
}
declare const CapacitorBluetoothSerial: CapacitorBluetoothSerialWeb;
export { CapacitorBluetoothSerial };
