package Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalContainer {
  private Map<String, SignalObject> map=new ConcurrentHashMap<String,SignalObject>();

  private SignalContainer(){}

  public final static SignalContainer Default=new SignalContainer();

  public SignalObject get(String key){
	  if(this.map.containsKey(key))
		  return this.map.get(key);
	  else {
		return null;
	}
  }

  public void add(String key,SignalObject value){
	  if(this.map.containsKey(key)){
		 removeCommon(key);
	  }
	  this.map.put(key, value);
  }

  private void removeCommon(String key){
	  SignalObject valueObject=this.map.get(key);
	  valueObject=null;
	  this.map.remove(key);
  }


  public void remove(String key){
	  if(!this.map.containsKey(key)){
		  return;
	  }
	  removeCommon(key);

  }

}
