package Packet;

import java.util.concurrent.ConcurrentMap;
import framework.Guid;
import java.util.concurrent.ConcurrentHashMap;

public class GuidMapping
{
	private ConcurrentMap<Integer,Guid>  map = new ConcurrentHashMap<Integer,Guid>();
	public static final GuidMapping Default = new GuidMapping();
	private GuidMapping(){

	}

	public void add(Integer key,Guid value){
		if(this.map.containsKey(key)){
			this.map.remove(key);
		}
		this.map.put(key,value);
	}

	public void remove(Integer key){
		if(this.map.containsKey(key)){
			this.map.remove(key);
		}
	}

	public Guid get(Integer key){
		if(this.map.containsKey(key)){
			return this.map.get(key);
		}
		return null;
	}

	public void clear(){
		this.map.clear();
	}

}
