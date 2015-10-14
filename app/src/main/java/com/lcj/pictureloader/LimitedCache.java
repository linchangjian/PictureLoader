package com.lcj.pictureloader;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by aniu520 on 9/23/2015.
 */
public abstract class LimitedCache<K, V> {

    private final LinkedList<V> hardCache = new LinkedList<V>();

    private final Map<K, Reference<V>> softMap = new HashMap<K, Reference<V>>();


    public V get(K key){
        if(containsKey(key)){
            Reference<V> reference = softMap.get(key);
            return reference.get();
        }else{
            return null;
        }
    }


    public boolean containsKey(K key){
        return softMap.containsKey(key);
    }

    public void put(K key, V value){
        int valueSize = getSize(value);
        int sizeLimit = getSizeLimit();
        if(valueSize < sizeLimit){
            while(getMapSize() + valueSize > sizeLimit){
                hardCache.removeLast();
            }
            hardCache.addFirst(value);
        }
        softMap.put(key, createReference(value));


    }

    private int getMapSize() {
        int size = 0;
        for(V v : hardCache){
            size += getSize(v);
        }
        return size;
    }


    protected abstract Reference<V> createReference(V value);

    protected abstract int getSizeLimit();


    protected abstract int getSize(V value);


}
