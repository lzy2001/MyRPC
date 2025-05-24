package Client.rpcClient;

import Common.Message.RpcRequest;
import Common.Message.RpcResponse;


public interface  RpcClient {

    //定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request);
}
