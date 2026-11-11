# Single Flight Design Pattern

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange" alt="Java 17+" />
  <img src="https://img.shields.io/badge/Pattern-SingleFlight-blue" alt="Single Flight Pattern" />
  <img src="https://img.shields.io/badge/Concurrency-ThreadSafe-green" alt="Thread Safe" />
</p>

A clean Java implementation of the Single Flight pattern to prevent duplicate expensive operations from running concurrently for the same key.

## Why this pattern matters

Imagine multiple users request the same data at the same time:

- 50 threads ask for the same user profile
- 50 separate database calls are triggered
- the system wastes CPU, memory, and network resources
- the database can become a bottleneck

The Single Flight pattern fixes this by ensuring that only one request executes the expensive work for a given key, while all other concurrent callers wait and reuse the same result.

## Concept

Single Flight is often used when:

- a cache miss occurs for the same key
- multiple requests fetch the same data from a database
- multiple API calls target the same remote service
- expensive computations should be shared among concurrent callers

In short:

> For the same key, only one execution is allowed at a time. Everyone else waits for that result instead of doing the same expensive work again.

## How it works

This repository demonstrates the pattern using Java's `ConcurrentHashMap` and `CompletableFuture`.

- A map tracks operations currently in flight
- Each key is associated with a single `CompletableFuture`
- If another thread requests the same key while the operation is still running, it reuses the same future
- Once the result is ready, all waiting threads receive the same value

### Flow

1. Thread A requests key `userId:101`
2. No in-flight operation exists, so it starts loading data
3. Thread B and Thread C request the same key at the same time
4. They see the in-flight future and wait on it
5. Thread A completes the operation
6. Threads B and C receive the same result without repeating the work

## Project structure

```text
SingleFlight-Design-Pattern/
├── src/
│   ├── Main.java
│   ├── SingleFlight.java
│   └── SingleFlightTimeout.java
├── .gitignore
├── untitled.iml
└── README.md
```

## Files explained

### `SingleFlight.java`

This class implements the core pattern without timeout support.

It does the following:

- stores active computations in a thread-safe map
- uses `computeIfAbsent` to ensure atomic creation of the future
- runs the expensive operation asynchronously
- reuses the same `CompletableFuture` for all concurrent requests
- joins on the shared future to wait for the result

### `SingleFlightTimeout.java`

This variation adds timeout handling.

It allows callers to specify:

- a max waiting duration
- a time unit such as `SECONDS`, `MILLISECONDS`, or `MINUTES`

This is useful when you want to avoid callers waiting forever on a slow or hung dependency.

### `Main.java`

This file demonstrates the pattern in action with multiple threads requesting the same operation concurrently.

It simulates:

- concurrent requests for the same user
- a slow database-like operation
- reuse of the same computed result across threads
- timeout-based failure handling

## Example

```java
SingleFlight<String, String> singleFlight = new SingleFlight<>();

String result = singleFlight.execute("userId: 101", () -> {
    // Expensive operation: database call, remote API, cache load, etc.
    return "Dipankar";
});

System.out.println(result);
```

This ensures that if five threads invoke the same key at once, only one executes the supplier, and the others get the same value.

## Benefits

- reduces duplicate work
- lowers database and API pressure
- improves latency for repeated requests
- prevents thundering herd issues
- keeps concurrent access safe and scalable

## Use cases

This pattern is commonly used in:

- cache population
- distributed service coordination
- database read deduplication
- API rate-limit protection
- expensive business logic execution

## Why this is useful in real systems

In production systems, the same data often gets requested multiple times before the cache is populated. Without Single Flight, each request may trigger identical expensive work. With Single Flight, the first request performs the work and the rest wait for the shared result.

This simple optimization can significantly improve:

- throughput
- stability under load
- response consistency
- resource efficiency

## Getting started

### Prerequisites

- Java 17 or later
- IntelliJ IDEA / VS Code / any Java-capable IDE

### Run the project

```bash
javac src/*.java
java -cp src Main
```

## Conclusion

The Single Flight pattern is a powerful concurrency optimization that prevents redundant duplicate work when multiple requests hit the same key simultaneously. This repository shows a practical Java implementation using `CompletableFuture` and `ConcurrentHashMap`, making it easy to understand and extend.

Whether you are building REST APIs, microservices, or caching layers, Single Flight is a valuable pattern to keep in your design toolkit.

---

<p align="center">
  <b>Built for efficient concurrency and smarter systems.</b>
</p>
