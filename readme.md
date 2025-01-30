I implemented a concurrent map for a statistics endpoint at work over the last few months and regrettably I was punching outside my weight class. It did work but I could not confidently tell you what the code did altogether, future problems might be hard for me to solve. I made it my mission to start learning about Java concurrency to it’s fullest, completing courses and practise problems to improve my technical skill in this area. Already, three days in, I have learned more than I did over the last three years as a developer.

The first task I decided to accomplish was a quick 5 hour Java concurrency class provided through my work’s training portal. The work was pretty basic but an hour in we got an assignment: create a concurrent program to sum all the elements of an array. I added the extra parameter that this must be done inline without modifying the original array or creating a new one. Naturally I first wrote a recursive function which received the array, a start, and an end index, and conducted a merge-sort-like fork through the array and joining as I stepped back up the recursion tree.

```
getSum(int[] input, int start, int end) {
    if (end - start == 0) {
        return input[start];
    }
    if (end - start == 1) {
        return input[start] + input[end];
    }
    
    int half = (end + start) / 2;
    return getSum(input, start, half) + getSum(input, half + 1, end);
}
```

This is easy and should be expected from anyone who knows anything about java. It is more performant than a for-loop with incremental addition, but you do run the risk of exceeding recursion depth if done incorrectly. Switching this to a concurrent model seemed easy enough, I would just create an ExecutorService in my class and execute each getSum as a new thread.

```
ExecutorService e = 
	Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
...
    int half = (end + start) / 2;
    Future<Integer> left = e.submit(() -> getSumFast(input, start, half));
    Future<Integer> right = e.submit(() -> getSumFast(input, half + 1, end));
    return left.get() + right.get();
}
```

But this is where I hit a snag, the method was stalling infinitely. I assumed correctly that I was running out of threads as each recursive call spawns two new threads and blocks until they are done. I did something similar in my team product, but if I had implemented this incorrectly and stalled the app every time my endpoint was called I would be in big trouble without the skill to fix it.
  
After some research I figured our that I just needed to change my thread pool type to Executors.newWorkStealingPool(). This will turn my executor service into a fork-join pool, which is optimized for concurrent recursion. The idea is that if a thread is idle or stopped it will start tasks from another thread’s work pool. With this, once the thread was idle waiting for the left.get() and right.get() it would take on the left.get() or right.get() of it’s sibling thread removing the deadlock completely. This also reduces the amount of cores used to the amount available in your system (I could have stuck with the newFixedThreadPool pool and just set to 1000 threads but that is not dynamic nor efficient). Reading into it further I can see that the only time it is not a good idea to use the fork-join pool is when you are in an app server, since the fork-join pool uses a common pool of threads and could steal CPU time from incoming requests. Overall this was a good learning experience.