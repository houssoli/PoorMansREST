package pl.poznachowski.poormanrest;

import java.util.Map;

import org.mule.api.MuleEvent;
import org.mule.api.transport.PropertyScope;

import com.google.common.collect.ImmutableMap;

public class MuleEventEnricher {
	
	private final MuleEvent muleEvent;
	
	public static MuleEventEnricher enrich(MuleEvent muleEvent) {
		return new MuleEventEnricher(muleEvent);
	}
	
	private MuleEventEnricher(MuleEvent muleEvent){
		this.muleEvent = muleEvent;
	}
	
	public MuleEventEnricher withInboundProperties(Map<String, Object> properties){
		return withProperties(properties, PropertyScope.INBOUND);
	}
	
	public MuleEventEnricher withInboundProperty(String name, Object value){
		return withProperties(ImmutableMap.of(name, value), PropertyScope.INBOUND);
	}
	
	public MuleEventEnricher withInvocationProperties(Map<String, Object> properties){
		return withProperties(properties, PropertyScope.INVOCATION);
	}
	
	public MuleEventEnricher withInvocationProperty(String name, Object value){
		return withProperties(ImmutableMap.of(name, value), PropertyScope.INVOCATION);
	}
	
	public MuleEventEnricher withSessionProperties(Map<String, Object> properties){
		return withProperties(properties, PropertyScope.SESSION);
	}
	
	public MuleEventEnricher withSessionProperty(String name, Object value){
		return withProperties(ImmutableMap.of(name, value), PropertyScope.SESSION);
	}
	
	public MuleEventEnricher withProperties(Map<String, Object> properties, PropertyScope scope){
		for (String propertyKey : properties.keySet()) {
			muleEvent.getMessage().setProperty(propertyKey, properties.get(propertyKey), scope);
        }
		return this;
	}
	
	public MuleEvent get() {
		return muleEvent;
	}
}