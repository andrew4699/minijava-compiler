package Semantics;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class InsertionOrderedHashMap<K, V> extends HashMap<K, V> {
  List<K> insertionOrder;

  public InsertionOrderedHashMap() {
    insertionOrder = new ArrayList<>();
  }

  public V put(K key, V value) {
    if (!containsKey(key)) {
      insertionOrder.add(key);
    }

    return super.put(key, value);
  }

  public List<Map.Entry<K, V>> entryList() {
    List<Map.Entry<K, V>> entries = new ArrayList<>();

    for (K key : insertionOrder) {
      if (!containsKey(key)) {
        continue;
      }

      Map.Entry<K, V> entry = Map.entry(key, get(key));
      entries.add(entry);
    }

    return entries;
  }

  public int getInsertionOrder(K key) {
    return insertionOrder.indexOf(key);
  }
}
