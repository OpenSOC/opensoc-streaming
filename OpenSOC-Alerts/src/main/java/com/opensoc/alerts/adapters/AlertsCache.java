package com.opensoc.alerts.adapters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
 

public class AlertsCache<K, T> implements Serializable{
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 3610009654118587727L;
	private long timeToLive;
    private LRUMap CacheMap;
 
    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
    }
 
    public AlertsCache(long TimeToLive, final long TimerInterval, int maxItems) {
        this.timeToLive = TimeToLive * 1000;
 
        CacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && TimerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(TimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    public void put(K key, T value) {
    	
    	toString();
    	
        synchronized (CacheMap) {
        	System.out.println("CACHE INSERTING KEY: " + key);
            CacheMap.put(key, new CacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (CacheMap) {
            CacheObject c = (CacheObject) CacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                //c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(K key) {
        synchronized (CacheMap) {
            CacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (CacheMap) {
            return CacheMap.size();
        }
    }
    
    public boolean containsKey(String key)
    {
    	toString();
        synchronized (CacheMap) {
            CacheObject c = (CacheObject) CacheMap.get(key);
 
            if (c == null)
                return false;
            else {
                //c.lastAccessed = System.currentTimeMillis();
                return true;
            }
        }
    }
 
    public boolean containsValue(String key)
    {

    		
    		synchronized (CacheMap) {
    			
    			MapIterator itr = CacheMap.mapIterator();
    		
    			while (itr.hasNext()) {

    				itr.next();
                   CacheObject c = (CacheObject) itr.getValue();
                   
                   if(c.value.equals(key))
                	   return true;
    			}
    		}
    		
    		return false;
        
    }
    
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		synchronized (CacheMap) {
			
			MapIterator itr = CacheMap.mapIterator();
			sb.append("\n");
			
			while (itr.hasNext()) {
				itr.next();
               Object key = (K) itr;
               CacheObject c = (CacheObject) itr.getValue();
               
               sb.append("[CACHE] : " + key + " -> " + c.value + " \n");
			}
		}
		return sb.toString();
	}
	

    
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (CacheMap) {
            MapIterator itr = CacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((CacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (CacheMap) {
            	System.out.println("CACHE DELETING KEY: " + key);
                CacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}