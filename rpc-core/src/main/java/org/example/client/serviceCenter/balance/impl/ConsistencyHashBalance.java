package org.example.client.serviceCenter.balance.impl;

import lombok.Getter;
import org.example.client.serviceCenter.balance.LoadBalance;
import lombok.extern.slf4j.Slf4j;


import java.util.*;

@Getter
@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    // 虚拟节点的个数
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
    private SortedMap<Integer, String> shards = new TreeMap<>();

    // 真实节点列表
    private List<String> realNodes = new LinkedList<>();

    // 获取虚拟节点的个数
    public static int getVirtualNum() {
        return VIRTUAL_NUM;
    }

    // 该方法初始化负载均衡器，将真实的服务节点和对应的虚拟节点添加到哈希环中
    public void init(List<String> serviceList) {
        for (String server : serviceList) {
            realNodes.add(server);
            log.info("真实节点[{}] 被添加", server);
            // 遍历 serviceList(真实节点列表)，每个真实节点都会生成 VIRTUAL_NUM 个虚拟节点，并计算它们的哈希值
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                // 虚拟节点的命名规则是server&&VN<i>，其中 <i> 是虚拟节点的编号。
                String virtualNode = server + "&&VN" + i;
                int hash = getHash(virtualNode);
                // shards 是一个SortedMap，会根据 hash 对虚拟节点进行排序
                shards.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        }
    }

    /**
     * 获取被分配的节点名
     *
     * @param node 请求的节点（通常是请求的唯一标识符）
     * @return 负责该请求的真实节点名称
     */
    // 根据请求的node（比如某个请求的标识符），选择一个服务器节点。
    public String getServer(String node, List<String> serviceList) {
        if (shards.isEmpty()) {
            init(serviceList);  // 如果shards为空，调用init(serviceList) 初始化哈希环
        }

        int hash = getHash(node);
        Integer key;
        // 使用shards.tailMap(hash)获取hash值大于等于请求哈希值的所有虚拟节点。
        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        if (subMap.isEmpty()) {
            key = shards.firstKey();  // 如果没有大于该hash的节点，则返回最小的hash值
        } else {
            key = subMap.firstKey();  // 否则 选择 tailMap 的虚拟节点
        }
        // 返回真实节点：从选中的虚拟节点virtualNode中提取出真实节点的名称（即虚拟节点名称去掉 &&VN<i> 部分。
        String virtualNode = shards.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * 添加节点
     *
     * @param node 新加入的节点
     */
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            log.info("真实节点[{}] 上线添加", node);
            //
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        }
    }

    /**
     * 删除节点
     *
     * @param node 被移除的节点
     */
    public void delNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            log.info("真实节点[{}] 下线移除", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                log.info("虚拟节点[{}] hash:{}，被移除", virtualNode, hash);
            }
        }
    }

    /**
     * FNV1_32_HASH算法
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    @Override
    // 模拟负载均衡，通过生成一个随机字符串来模拟请求，最终通过一致性哈希选择一个服务器
    public String balance(List<String> addressList) {
        // 如果 addressList 为空或 null，抛出 IllegalArgumentException
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }

        // 使用UUID作为请求的唯一标识符来进行一致性哈希
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }

    @Override
    public String toString() {
        return "ConsistencyHash";
    }
}

