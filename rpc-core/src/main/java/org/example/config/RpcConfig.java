package org.example.config;

import org.example.client.serviceCenter.balance.impl.ConsistencyHashBalance;
import org.example.server.serviceRegister.impl.ZKServiceRegister;
import common.serializer.mySerializer.Serializer;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcConfig {
    //名称
    private String name = "rpc";
    //端口
    private Integer port = 9999;
    //主机名
    private String host = "localhost";
    //版本号
    private String version = "1.0.0";
    //注册中心
    private String registry = new ZKServiceRegister().toString();
    //序列化器
    private String serializer = Serializer.getSerializerByCode(3).toString();
    //负载均衡
    private String loadBalance = new ConsistencyHashBalance().toString();

}
