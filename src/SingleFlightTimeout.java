import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class SingleFlightTimeout<K, V> {
    private final ConcurrentHashMap<K, CompletableFuture<V>> inFlight = new ConcurrentHashMap<>();

    public V execute(K key, Supplier<V> loader, long timeout, TimeUnit timeUnit) {
        CompletableFuture<V> future = inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<V> newFuture = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                try {
                    newFuture.complete(loader.get());
                } catch (Exception e) {
                    newFuture.completeExceptionally(e);
                } finally {
                    inFlight.remove(k);
                }
            });

            return newFuture;
        });

        //wait with a timeout
        try{
            return future.get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            inFlight.remove(key);
            e.printStackTrace();
            throw new RuntimeException("SingleFlight timeout exception " + e);
        }
    }
}
