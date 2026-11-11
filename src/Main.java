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



        // Create SingleFlightTimeout instance
        SingleFlightTimeout<String, String> singleFlight1 =
                new SingleFlightTimeout<>();

        // Create a pool of 5 worker threads
        ExecutorService executorService1 =
                Executors.newFixedThreadPool(5);

        // Submit 5 concurrent requests for the same user
        for (int i = 1; i <= 5; i++) {

            // Separate variable for each iteration
            final int thread = i;

            executorService1.submit(() -> {

                try {

                    System.out.println(
                            "Thread " + thread + " started: " +
                                    Thread.currentThread().getName()
                    );

                    String result = singleFlight1.execute(
                            "userId:101",

                            // Actual database operation
                            () -> {

                                System.out.println(
                                        "Fetching user from database..."
                                );

                                try {
                                    // Simulate a slow database query
                                    Thread.sleep(5000);

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new RuntimeException(e);
                                }

                                return "Dipankar";
                            },

                            // Maximum waiting time
                            3,
                            TimeUnit.SECONDS
                    );

                    System.out.println(
                            "Thread " + thread +
                                    " received result: " + result
                    );

                } catch (RuntimeException e) {

                    System.out.println(
                            "Thread " + thread +
                                    " failed: " + e.getMessage()
                    );
                }
            });
        }

        // Stop accepting new tasks
        executorService.shutdown();

        // Wait for submitted tasks to finish
        boolean completed = executorService.awaitTermination(
                10,
                TimeUnit.SECONDS
        );

        if (completed) {
            System.out.println("All Requests Completed");
        } else {
            System.out.println("Some Requests Are Still Running");
        }
    }
}