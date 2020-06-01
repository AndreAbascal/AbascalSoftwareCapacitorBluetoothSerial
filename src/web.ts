import { WebPlugin,registerWebPlugin } from '@capacitor/core';
import { CapacitorBluetoothSerialPlugin } from './definitions';
// import { Observable, from } from 'rxjs';

export class CapacitorBluetoothSerialWeb extends WebPlugin implements CapacitorBluetoothSerialPlugin {
	constructor() {
		super({
			name: 'CapacitorBluetoothSerial',
			platforms: ['web']
		});
	}

	clear(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	connect(address: string): Promise<any> {
		// return from(() => {
		// 	return new Promise((resolve) => {
		// 		return resolve("Connected to "+address);
		// 	});
		// });
		return new Promise((resolve) => {
			return resolve("Connected to "+address);
		});
	}
	connectInsecure(address: string): Promise<any> {
		// return from(() => {
		// 	return new Promise((resolve) => {
		// 		return resolve("Connected insecurely to "+address);
		// 	});
		// });
		return new Promise((resolve) => {
			return resolve("Connected insecurely to "+address);
		});
	}
	disconnect(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	enable(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	isConnected(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	isEnabled(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	subscribeRawData(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
		});
	}
	write(buffer: Uint8Array|string): Promise<any> {
		return new Promise((resolve) => {
			return resolve("OK: "+buffer.toString());
		});
	}
}

const CapacitorBluetoothSerial = new CapacitorBluetoothSerialWeb();

export { CapacitorBluetoothSerial };

registerWebPlugin(CapacitorBluetoothSerial);
