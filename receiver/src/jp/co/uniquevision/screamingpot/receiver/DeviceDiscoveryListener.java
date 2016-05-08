package jp.co.uniquevision.screamingpot.receiver;

import java.lang.ref.WeakReference;

import java.io.IOException;

import javax.bluetooth.*;

public class DeviceDiscoveryListener implements DiscoveryListener {

	private WeakReference<RemoteDeviceDiscovery> discoveryRef;
	private WeakReference<Object> inquiryCompletedEventRef;
	
	public DeviceDiscoveryListener(RemoteDeviceDiscovery discovery, Object inquiryCompletedEvent) {
		this.discoveryRef = new WeakReference<>(discovery);
		this.inquiryCompletedEventRef = new WeakReference<>(inquiryCompletedEvent);
	}
	
	private RemoteDeviceDiscovery getDiscovery() {
		return null == this.discoveryRef ?
			null :
			this.discoveryRef.get();
	}
	
	private Object getInquiryCompletedEvent() {
		return null == this.inquiryCompletedEventRef ?
			null :
			this.inquiryCompletedEventRef.get();
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        RemoteDeviceDiscovery discovery = getDiscovery();
        if (null == discovery) {
        	return;
        }
        
        System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
        discovery.addElement(btDevice);
        
        try {
            System.out.println("     name " + btDevice.getFriendlyName(false));
        } catch (IOException cantGetDeviceName) {
        	cantGetDeviceName.printStackTrace();
        }
	}

	@Override
	public void inquiryCompleted(int discType) {
		Object inquiryCompletedEvent = getInquiryCompletedEvent();
		if (null == inquiryCompletedEvent) {
			return;
		}
		
        System.out.println("Device Inquiry completed!");
        synchronized(inquiryCompletedEvent){
            inquiryCompletedEvent.notifyAll();
        }
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// TODO Auto-generated method stub
		
	}

}
