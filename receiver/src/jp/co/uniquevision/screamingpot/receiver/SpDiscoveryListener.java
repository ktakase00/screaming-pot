package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;

import javax.bluetooth.*;

public class SpDiscoveryListener implements DiscoveryListener {

	private RemoteDeviceDiscovery discovery;
	private Object inquiryCompletedEvent;
	
	public SpDiscoveryListener(RemoteDeviceDiscovery discovery, Object inquiryCompletedEvent) {
		this.discovery = discovery;
		this.inquiryCompletedEvent = inquiryCompletedEvent;
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
        this.discovery.addElement(btDevice);
        
        try {
            System.out.println("     name " + btDevice.getFriendlyName(false));
        } catch (IOException cantGetDeviceName) {
        	cantGetDeviceName.printStackTrace();
        }
	}

	@Override
	public void inquiryCompleted(int discType) {
        System.out.println("Device Inquiry completed!");
        synchronized(this.inquiryCompletedEvent){
            this.inquiryCompletedEvent.notifyAll();
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
