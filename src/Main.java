//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.concurrent.*;
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        SingleFlight<String, String> singleFlight = new SingleFlight<>();
        //ExecutorService is an interface that manages the execution of tasks using threads.
        //we want 5 requests to execute concurrently.
        //A thread pool does not mean all the tasks execute simultaneously in exact same nano second. It means multiple tasks can execute concurrently, subject to scheduling and available resources.
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for(int i = 1; i<= 5;i++) {
            //Separable variable is created for each iteration.
            //This is required because Java Lambda expressions can capture local variables only when they are final or effectively final
            final int thread = i;
            //.submit() -> submits the task to thread pool for execution and ExecutorService assigns an available worker thread to execute it
            executorService.submit(() -> { // here the task logic should be written
                System.out.println("Thread " + thread + " " + Thread.currentThread().getName());
                String result = singleFlight.execute("userId: 101", () -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    return "Dipankar";
                });

                System.out.println("Thread: " + thread + " result: " + result);

            });

        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("All Requests Completed");
    }
}