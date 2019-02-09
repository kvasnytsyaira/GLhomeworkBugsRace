package com.basecamp.service;

import com.basecamp.wire.Bug;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ProductService {

    GetProductInfoResponse getProductInfo(String productId);

    GetHandleProductIdsResponse handleProducts(List<String> productIds);

    void stopProductExecutor();

    List<Bug> homework() throws ExecutionException, InterruptedException;



}
