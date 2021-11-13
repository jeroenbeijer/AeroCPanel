package subscriber;

import java.util.HashMap;

public class SubscriberFactory {

	private static HashMap<String, SubscriberThread> oList = new HashMap<String, SubscriberThread>();

	// default subs
	int numSubscribers = 4;

	public void setup(int instances) {

		for (int a = 1; a <= instances; a++) {
			
			oList.put("JAERO-" + a, new SubscriberThread());

		}

	}

	public static SubscriberThread getSubscriber(int instance) {

		return oList.get("JAERO-" + instance);

	}
	
	public void startAll(){
		
		
		for(int a = 1; a <= oList.size(); a++){
			
	
			SubscriberThread sub = oList.get("JAERO-" + a);
			sub.setInstance(a);
			Thread t1 = new Thread(sub);
			t1.start();
			
		}
	}

}
