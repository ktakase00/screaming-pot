package jp.co.uniquevision.screamingpot.receiver;

import java.lang.ref.WeakReference;

import javax.bluetooth.*;

public class ServiceDiscoveryListener implements DiscoveryListener {
	
	private WeakReference<ServicesSearch> servicesSearchRef;
	private WeakReference<Object> serviceSearchCompletedEventRef;
	
	public ServiceDiscoveryListener(ServicesSearch servicesSearch, Object serviceSearchCompletedEvent) {
		this.servicesSearchRef = new WeakReference<>(servicesSearch);
		this.serviceSearchCompletedEventRef = new WeakReference<>(serviceSearchCompletedEvent); 
	}
	
	private ServicesSearch getServicesSearch() {
		return null == this.servicesSearchRef ?
			null :
			this.servicesSearchRef.get();
	}
	
	private Object getserviceSearchCompletedEvent() {
		return null == this.serviceSearchCompletedEventRef ?
			null :
			this.serviceSearchCompletedEventRef.get();
	}

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		// TODO Auto-generated method stub

	}

	@Override
	public void inquiryCompleted(int discType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
        Object serviceSearchCompletedEvent = getserviceSearchCompletedEvent();
        if (null == serviceSearchCompletedEvent) {
        	return;
        }
        System.out.println("service search completed!");
        
        synchronized(serviceSearchCompletedEvent){
            serviceSearchCompletedEvent.notifyAll();
        }
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        ServicesSearch servicesSearch = getServicesSearch();
        if (null == servicesSearch) {
        	return;
        }
        
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (url == null) {
                continue;
            }
            
            servicesSearch.addServiceUrl(url);
            DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
            if (serviceName != null) {
                System.out.println("service " + serviceName.getValue() + " found " + url);
            } else {
                System.out.println("service found " + url);
            }
        }
	}

}
