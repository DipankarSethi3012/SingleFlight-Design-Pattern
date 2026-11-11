import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * SingleFlight Pattern Ensures that multiple concurrent requests for the same key execute the expensive operation only once
 *
 * If multiple threads requests the same key simultaneously:
 * - Only one thread can execute the loader
 * Other thread resues the same CompletableFuture
 * All threads receive the result of the same computation
 *
 * This helps to prevent duplicate database queries, API calls, cache misses etc.
**/
public class SingleFlight<K, V>{
    /**
     * Stores currently executing Computation
     * Key -> Identifier of the operation
     * Value -> CompletableFuture representing the ongoing computation
     * ConcurrentHashMap makes access thread-safe
     */
    private final ConcurrentHashMap<K, CompletableFuture<V>> inFlight= new ConcurrentHashMap<>();

    /**
     * Executes the given loader for the particular key
     *
     * If an operation for this key is already running, the existing CompletableFuture is reused.
     * Otherwise, a new computation is started
     * @param key Unique identifier for computation
     * @param loader Expensive Operation that produces the result
     * @return Result of computation
     */
    public V execute(K key, Supplier<V> loader){
        /**
         * ComputeIfAbsent is atomic for given key
         *
         * Case 1: Key already exists: Return the existing CompletableFuture
         *
         * Case 2: Key does not exist: Create a new CompletableFuture
         *
         * This insures that only one Future is created per key
         */
        CompletableFuture<V> future = inFlight.computeIfAbsent(key, k -> {
            //Create a future that will eventually hold the result
            CompletableFuture<V> newFuture = new CompletableFuture<>();
            /*
             * Execute the expensive operation asynchronously
             *
             * Other threads can immediately receive newFuture and wait
             * for its completion instead of executing the same operation themselves
             */
            CompletableFuture.runAsync(() -> {
                try {
                    //execute the actual expensive operation
                    V result = loader.get();
                    //Publish the result to all threads waiting for this future
                    newFuture.complete(result);
                } catch (Exception e) {
                    newFuture.completeExceptionally(e);
                } finally {
                    inFlight.remove(k);
                }
            });
            return newFuture;
        });
        //check whether the CompletableFuture has completed. If it hasn't completed the calling thread waits
        //Once completed, it retrieves and returns the result.
        //If the computation failed, it throws CompletionException
        return future.join();
    }
}

//Supplier is a functional Interface in Java that represents an operation which supplies a value
//without taking any input.
//Simple Words: Take nothing, returns something
//Syntax

// @FunctionalInterface
//public interface Supplier <T> {
// T get();
//}


//CompletableFuture<T> is a java class that represents a future result  of Asynchronous Communication
//In simple words:
//Imagine you send a request to database. The database takes 3 seconds to return to result
//Instead of blocking your current thread while the query executes, you can start the operation asynchronously
//and get a completableFuture representing its future result