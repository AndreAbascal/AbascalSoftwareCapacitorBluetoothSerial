import { WebPlugin,registerWebPlugin } from '@capacitor/core';
import { Plugins } from '@capacitor/core';
import { CapacitorBluetoothSerialProtocol } from './definitions';

const { CapacitorBluetoothSerialPlugin } = Plugins;

var stringToArrayBuffer = (str: any) => {
    var ret = new Uint8Array(str.length);
    for (var i = 0; i < str.length; i++) {
        ret[i] = str.charCodeAt(i);
    }
    return ret.buffer;
};

export class CapacitorBluetoothSerial implements CapacitorBluetoothSerialProtocol {
	clear(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.clear();
	}
	connect(address: string): Promise<any> {
		return CapacitorBluetoothSerialPlugin.connect(address);
	}
	connectInsecure(address: string): Promise<any> {
		return CapacitorBluetoothSerialPlugin.connectInsecure(address);
	}
	disconnect(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.disconnect();
	}
	enable(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.enable();
	}
	isConnected(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.isConnected();
	}
	isEnabled(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.isEnabled();
	}
	subscribeRawData(): Promise<any> {
		return CapacitorBluetoothSerialPlugin.subscribeRawData();
	}
	write(data: any): Promise<any> {
		if (typeof data === 'string') {
            data = stringToArrayBuffer(data);
        } else if (data instanceof Array) {
            // assuming array of interger
            data = new Uint8Array(data).buffer;
        } else if (data instanceof Uint8Array) {
            data = data.buffer;
        }
		return CapacitorBluetoothSerialPlugin.write(data);
	}
}

export class CapacitorBluetoothSerialWeb extends WebPlugin implements CapacitorBluetoothSerialProtocol {
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
		return new Promise((resolve) => {
			return resolve("Connected to "+address);
		});
	}
	connectInsecure(): Promise<any> {
		return new Promise((resolve) => {
			return resolve();
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
	write(buffer: any): Promise<any> {
		return new Promise((resolve) => {
			return resolve("OK: "+buffer.toString());
		});
	}
}

registerWebPlugin(new CapacitorBluetoothSerialWeb());