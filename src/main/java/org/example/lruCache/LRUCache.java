package org.example.lruCache;

public class LRUCache {
    public static void main(String[] args) {
        LRUCacheService lruCacheService = new LRUCacheService(2);

        lruCacheService.putKey("1", "one");
        lruCacheService.putKey("2", "two");
        lruCacheService.printCacheMap();

        System.out.println(lruCacheService.getKey("1"));
        lruCacheService.delete("1");
        lruCacheService.putKey("3", "three");
        lruCacheService.printCacheMap();
    }
}
