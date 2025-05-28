package org.example.client.rpcClient;


import common.message.RpcRequest;
import common.message.RpcResponse;


public interface RpcClient {
    RpcResponse sendRequest(RpcRequest request);
    void close();
}
