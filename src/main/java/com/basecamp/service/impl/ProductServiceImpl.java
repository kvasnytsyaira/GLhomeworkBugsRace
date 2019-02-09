package com.basecamp.service.impl;

import com.basecamp.exception.InternalException;
import com.basecamp.exception.InvalidDataException;
import com.basecamp.service.ProductService;
import com.basecamp.wire.Bug;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ConcurrentTaskService taskService;

    public GetProductInfoResponse getProductInfo(String productId) {

        validateId(productId);

        log.info("Product id {} was successfully validated.", productId);

        return callToDbAnotherServiceETC(productId);
    }


    public GetHandleProductIdsResponse handleProducts(List<String> productIds) {
        Map<String, Future<String>> handledTasks = new HashMap<>();
        productIds.forEach(productId ->
                handledTasks.put(
                        productId,
                        taskService.handleProductIdByExecutor(productId)));

        List<String> handledIds = handledTasks.entrySet().stream().map(stringFutureEntry -> {
            try {
                return stringFutureEntry.getValue().get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(stringFutureEntry.getKey() + " execution error!");
            }

            return stringFutureEntry.getKey() + " is not handled!";
        }).collect(Collectors.toList());

        return GetHandleProductIdsResponse.builder()
                .productIds(handledIds)
                .build();
    }

    public void stopProductExecutor() {
        log.warn("Calling to stop product executor...");

        taskService.stopExecutorService();

        log.info("Product executor stopped.");
    }

    @Override
    public List<Bug> homework() throws ExecutionException, InterruptedException {

        Scanner in = new Scanner(System.in);
        System.out.print("Input a number of bugs that will participate in race: ");
        int num_of_threads = in.nextInt();
        while (num_of_threads == 0 || num_of_threads <0) {
            System.out.print("Input a NOTNULL/POSITIVE number of bugs that will participate in race: ");
            num_of_threads = in.nextInt();
        }
        System.out.println("There are " + num_of_threads + "participants");

        ExecutorService executorService = Executors.newFixedThreadPool(num_of_threads);

        Callable<Bug> callable = () -> {
            return startRace(10, Thread.currentThread().getName()); };

        ArrayList<Callable<Bug>> callableArrayList = new ArrayList<>();
        for (int i = 0; i < num_of_threads; i++) {
            callableArrayList.add(callable);
        }
        List<Future<Bug>> futures = executorService.invokeAll(callableArrayList);

        List<Bug> bugs = new ArrayList<>();
        for (Future<Bug> future : futures) {
            bugs.add(future.get());
        }
        bugs.sort((o1, o2) -> o1.getTimemoment() - o2.getTimemoment());
        System.out.println(bugs);
        executorService.shutdown();
        return bugs;
    }

    private static Bug startRace(int distance, String nameOfBug) throws InterruptedException {
        int step = 0;
        int timemoment = 0;
        while (step < distance) {
            ++timemoment;
            Thread.sleep(1000);
            step += new Random().nextInt(2);
            System.out.println
                    ("At " + timemoment + " time moment '" + Thread.currentThread().getName() + "'  is on the step " + step);
        }
        System.out.println("At " + timemoment + " time moment Bug '" + Thread.currentThread().getName() + "' passed distance");
        return new Bug(timemoment, Thread.currentThread().getName(), step);
    }

    ;

    private void validateId(String id) {

        if (StringUtils.isEmpty(id)) {
            // all messages could be moved to messages properties file (resources)
            String msg = "ProductId is not set.";
            log.error(msg);
            throw new InvalidDataException(msg);
        }

        try {
            Integer.valueOf(id);
        } catch (NumberFormatException e) {
            String msg = String.format("ProductId %s is not a number.", id);
            log.error(msg);
            throw new InvalidDataException(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalException(e.getMessage());
        }
    }

    private GetProductInfoResponse callToDbAnotherServiceETC(String productId) {
        return GetProductInfoResponse.builder()
                .id(productId)
                .name("ProductName")
                .status("ProductStatus")
                .build();
    }

}