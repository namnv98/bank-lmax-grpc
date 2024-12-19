package com.namnv;

import com.github.benmanes.caffeine.cache.*;

import java.util.concurrent.*;

public class CacheStampedeExample {

  private static final Cache<String, String> cache = Caffeine.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES)  // Cache expiry
    .maximumSize(100)  // Max cache size
    .build();

  // A map to hold Future objects for loading data for a key
  private static final ConcurrentMap<String, CompletableFuture<String>> inProgress = new ConcurrentHashMap<>();

  // Simulate loading data from an external source (e.g., database)
  private static String loadDataFromDatabase(String key) {
    System.out.println("call");
    // Simulate a delay in loading data from DB or external resource
    try {
      Thread.sleep(2000);  // Simulate 2 seconds delay
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "Data for " + key;  // Simulated data
  }

  // Method to get data with cache stampede prevention
  public static String getData(String key) {
    // First, try to get the value from the cache
    String cachedValue = cache.getIfPresent(key);
    if (cachedValue != null) {
      return cachedValue;  // Return the cached value if present
    }

    // Use computeIfAbsent to prevent stampede (fetch data only once per key)
    CompletableFuture<String> future = inProgress.computeIfAbsent(key, k -> {
      // Create a new CompletableFuture to load the data
      CompletableFuture<String> result = new CompletableFuture<>();
      // Asynchronously load the data
      Executors.newSingleThreadExecutor().submit(() -> {
        try {
          // Load data from the database (simulated)
          String data = loadDataFromDatabase(k);
          // Put the data in the cache
          cache.put(k, data);
          // Complete the Future with the data
          result.complete(data);
        } catch (Exception e) {
          // Complete exceptionally in case of error
          result.completeExceptionally(e);
        } finally {
          // Once done, remove the Future from the inProgress map
          inProgress.remove(k);
        }
      });
      return result;
    });

    // Wait for the result (this will block until the data is loaded)
    try {
      return future.get();  // Block until data is available
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      return null;  // Handle error
    }
  }

  public static void main(String[] args) {
    // Simulate multiple threads trying to access the same key
    ExecutorService executor = Executors.newFixedThreadPool(5);
    String key = "item1";

    for (int i = 0; i < 5; i++) {
      executor.submit(() -> {
        String value = getData(key);
        System.out.println(Thread.currentThread().getName() + ": " + value);
      });
    }

    executor.shutdown();
  }
}
