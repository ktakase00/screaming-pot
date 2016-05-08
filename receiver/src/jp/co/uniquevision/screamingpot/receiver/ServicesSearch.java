package jp.co.uniquevision.screamingpot.receiver;

import java.lang.ref.WeakReference;
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;

public class ServicesSearch {
	
	static final UUID OBEX_SERIAL_PORT = new UUID(0x1101);

	private WeakReference<RemoteDevice> btDeviceRef;
	private UUID serviceUUID;
	private final Vector<String> serviceFound = new Vector<>();
	
	public ServicesSearch(RemoteDevice btDevice, UUID serviceUUID) {
		init(btDevice, serviceUUID);
	}
	
	public ServicesSearch(RemoteDevice btDevice) {
		init(btDevice, OBEX_SERIAL_PORT);
	}
	
	private void init(RemoteDevice btDevice, UUID serviceUUID) {
		this.btDeviceRef = new WeakReference<>(btDevice);
		this.serviceUUID = serviceUUID;
	}
	
	private RemoteDevice getBtDevice() {
		return null == this.btDeviceRef ?
			null :
			this.btDeviceRef.get();
	}

	public void search() throws InterruptedException {
        this.serviceFound.clear();

        final Object serviceSearchCompletedEvent = new Object();

        DiscoveryListener listener = new ServiceDiscoveryListener(this,
        		serviceSearchCompletedEvent);

        UUID[] searchUuidSet = new UUID[] {
        		this.serviceUUID
        };
        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };

        synchronized(serviceSearchCompletedEvent) {
        	try {
        		RemoteDevice btDevice = getBtDevice();
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
                serviceSearchCompletedEvent.wait();
        	}
        	catch (IOException e) {
        		e.printStackTrace();
        	}
        }
	}
	
	public void addServiceUrl(String url) {
		this.serviceFound.add(url);
	}
}
