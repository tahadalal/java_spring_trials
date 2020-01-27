package com.example.demo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;


public class CheatSheetCompletableFutures {

	
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		
		Future<String> future = calculateAsync();
		System.out.println("calling get method");
		String result = future.get();
		System.out.println(result);
		Assert.assertEquals("Hello", result);
		
		// For returning a completed future.
		CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("Hi");
		Assert.assertEquals("Hi", completableFuture.get());
		
		// Async with cancellation
//		Future<String> cancelledFuture = calculateAsyncWithCancellation();
//		cancelledFuture.get();
		
		CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
		Assert.assertEquals("Hello",future1.get());
		
		// Apply
		CompletableFuture<String> future2 = 
				future1
					.thenApply( s -> s + "world" );
		Assert.assertEquals("Helloworld", future2.get());
		
		// Accept
		future1.thenAccept(System.out::println);
		future2.thenAccept(s -> System.out.println("future2 is "+s));
		
		// Run
		future1.thenRun(()->System.out.println("Finished."));
		
		// Combining futures
		CompletableFuture<String> future3 = 
				CompletableFuture
					.supplyAsync(()->"Hello")
					.thenCompose(s -> CompletableFuture.supplyAsync(() -> s+"world"));
		assertEquals("Helloworld",future3.get());
		
		CompletableFuture<String> future4 =
				CompletableFuture.supplyAsync(()->"Hello")
				// combines and returns a CompletableFuture
				.thenCombine(CompletableFuture.supplyAsync(()-> "world"), 
						(s1, s2) -> s1+s2);
		future4.thenAccept(s->System.out.println("future4 "+s));
		assertEquals("Helloworld",future4.get());
		
		
		CompletableFuture<Void> future5 = 
				CompletableFuture
					.supplyAsync(()->"future5 Hello")
					.thenAcceptBoth(
							CompletableFuture.supplyAsync(()->"world")
							,(s1,s2)->System.out.println(s1+s2));
		future5.get();
		
		CompletableFuture.allOf(future1, future2, future3);
		
		
		
		String combined = Stream
			.of(CompletableFuture.supplyAsync(()->"Hello"), CompletableFuture.supplyAsync(()->"World"))
			.map(CompletableFuture::join)
			.peek(System.out::println)
			.collect(Collectors.joining(" "));
		assertEquals("Hello World", combined);
		
		// Handling Errors
		String name = null;
		CompletableFuture<String> throwingFuture = CompletableFuture
			.supplyAsync(() -> {
				if(name == null) {
					throw new RuntimeException("Nameless!!");
				}
				return "Hello, "+name;
			})
			// Gets either of the 2 : the value and the exception
			.handle((s,t) -> s != null ? s : "Hello, Stranger");;
		
		System.out.println("calling throwing Future");
		System.out.println(throwingFuture.get());
		
		CompletableFuture
			.supplyAsync(() -> {
				if (name == null) throw new RuntimeException("Nameless!!");
				return "Hello, "+name;
			})
			// Gets only the exception (if any) 
			.exceptionally(error -> {
				
				return null;
			});
		
		
		// Throw Exception
		CompletableFuture<String> throwingFuture1 = new CompletableFuture<String>();
		throwingFuture1.completeExceptionally(new RuntimeException("Complete With Exception!"));
		
//		throwingFuture1.get();
		
		
		// Async
		
		CompletableFuture
			.supplyAsync(() -> "Hello")
			.thenApplyAsync(s->s+"World")
			.thenAccept(System.out::println);
		
		// Listeners
		
//		 thread pool executor vs executor service
		Executors.newFixedThreadPool(100);
		ThreadPoolExecutor poolExecutor;
//		poolExecutor
		
		
		System.out.println("++++++++++++++++++");
		Stream<CompletableFuture<String>> futureStream = Stream
			.of(future2, throwingFuture1, future3 );
		CompletableFuture.allOf(future2, throwingFuture1, future3);
		List<CompletableFuture<String>> list = new ArrayList<CompletableFuture<String>>();
		list.add(future2);
		list.add(throwingFuture1);
		list.add(future3);
		
		list.toArray();
//		CompletableFuture<String>[] cfs = (CompletableFuture[]) list.toArray() ;
		CompletableFuture<String>[] cfs1 = list.toArray(new CompletableFuture[list.size()]);
		
		CompletableFuture<Void> cf = CompletableFuture.allOf(cfs1);
		
		list
			.stream()
			.forEach(cf1 -> {
				String res = "";
				if (cf1.isCompletedExceptionally()) {
					res = "Exception was thrown" ;
				} 
				else {
					try {
						res = cf1.get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println(res);
				//				System.out.println(cf1.isDone());
				//				System.out.println(cf1.isCompletedExceptionally());
			});
		
		System.out.println("+++++++++++++++++");
		
		
	}
	
	
	public static Future<String> calculateAsync() {

		CompletableFuture<String> completableFuture = new CompletableFuture<String>();
		Executors
			.newCachedThreadPool()
			.submit(()->{
				System.out.println("Thread will go to sleep");
				Thread.sleep(1000);
				System.out.println("Thread now awake");
				completableFuture.complete("Hello");
				System.out.println("done completing");
				
				//returning a null lets the executor know that this is a callable and not a runnable.
				return null;
			});
		
		return completableFuture;
	}
	
	public static Future<String> calculateAsyncWithCancellation(){
		CompletableFuture<String> future = new CompletableFuture<String>();
		
		Executors
			.newSingleThreadExecutor()
			.submit(() -> {
				Thread.sleep(1000);
				// use for cancelling a future
				System.out.println("Cancelling the future");
				future.cancel(true);
				return null;
			});
		return future;
	}

}
