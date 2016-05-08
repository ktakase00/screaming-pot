package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;

public class RemoteDeviceDiscovery implements Runnable {
	
	private final Vector<RemoteDevice> devicesDiscovered = new Vector<>();

	@Override
	public void run() {
		try {
			while (true) {
				searchDevice();
				searchService();
				
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void searchDevice() throws InterruptedException {
        this.devicesDiscovered.clear();

        final Object inquiryCompletedEvent = new Object();
        final DiscoveryListener listener = new DeviceDiscoveryListener(this, inquiryCompletedEvent);

        synchronized(inquiryCompletedEvent) {
    		try {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                
                if (started) {
                    System.out.println("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                    System.out.println(devicesDiscovered.size() +  " device(s) found");
                }
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
        }
	}
	
	public void addElement(RemoteDevice btDevice) {
		this.devicesDiscovered.addElement(btDevice);
	}

	private void searchService() throws InterruptedException {
        for(Enumeration<RemoteDevice> en = this.devicesDiscovered.elements(); en.hasMoreElements(); ) {
            RemoteDevice btDevice = (RemoteDevice)en.nextElement();

            ServicesSearch servicesSearch = new ServicesSearch(btDevice);
            servicesSearch.search();
        }
	}
}
