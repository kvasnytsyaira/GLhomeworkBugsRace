package com.basecamp.service.impl;

import com.basecamp.exception.InternalException;
import com.basecamp.exception.InvalidDataException;
import com.basecamp.service.ProductService;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
