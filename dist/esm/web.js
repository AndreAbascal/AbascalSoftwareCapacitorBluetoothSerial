import { WebPlugin, registerWebPlugin } from '@capacitor/core';
// import { Observable, from } from 'rxjs';
export class CapacitorBluetoothSerialWeb extends WebPlugin {
    constructor() {
        super({
            name: 'CapacitorBluetoothSerial',
            platforms: ['web']
        });
    }
    clear() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    connect(address) {
        // return from(() => {
        // 	return new Promise((resolve) => {
        // 		return resolve("Connected to "+address);
        // 	});
        // });
        return new Promise((resolve) => {
            return resolve("Connected to " + address);
        });
    }
    connectInsecure(address) {
        // return from(() => {
        // 	return new Promise((resolve) => {
        // 		return resolve("Connected insecurely to "+address);
        // 	});
        // });
        return new Promise((resolve) => {
            return resolve("Connected insecurely to " + address);
        });
    }
    disconnect() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    enable() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    isConnected() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    isEnabled() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    subscribeRawData() {
        return new Promise((resolve) => {
            return resolve();
        });
    }
    write(buffer) {
        return new Promise((resolve) => {
            return resolve("OK: " + buffer.toString());
        });
    }
}
const CapacitorBluetoothSerial = new CapacitorBluetoothSerialWeb();
export { CapacitorBluetoothSerial };
registerWebPlugin(CapacitorBluetoothSerial);
//# sourceMappingURL=web.js.map