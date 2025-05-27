package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;

import java.util.*;

public class ConsistencyHashBalance implements LoadBalance {
    // 虚拟节点的个数
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
    private SortedMap<Integer, String> shards = new TreeMap<>();

    // 真实节点列表
    private List<String> realNodes = new LinkedList<>();

    //模拟初始服务器
    private String[] servers = null;

    // 该方法初始化负载均衡器，将真实的服务节点和对应的虚拟节点添加到哈希环中
    private  void init(List<String> serviceList) {
        for (String server :serviceList) {
            realNodes.add(server);
            System.out.println("真实节点[" + server + "] 被添加");
            createVirtualNode(server);
        }
    }
    // 遍历 serviceList(真实节点列表)，每个真实节点都会生成 VIRTUAL_NUM 个虚拟节点，并计算它们的哈希值
    private void createVirtualNode(String server) {
        for (int i = 0; i < VIRTUAL_NUM; i++) {
            // 虚拟节点的命名规则是server&&VN<i>，其中 <i> 是虚拟节点的编号。
            String virtualNode = server + "&&VN" + i;
            int hash = getHash(virtualNode);
            // shards 是一个SortedMap，会根据 hash 对虚拟节点进行排序
            shards.put(hash, virtualNode);
            System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被添加");
        }
    }

    /**
     * 获取被分配的节点名
     *
     */
    // 根据请求的node（比如某个请求的标识符），选择一个服务器节点。
    public  String getServer(String node,List<String> serviceList) {
        // 首先调用init(serviceList) 初始化哈希环（真实节点和虚拟节点）
        init(serviceList);
        int hash = getHash(node);
        Integer key;
        // 使用shards.tailMap(hash)获取hash值大于等于请求哈希值的所有虚拟节点。
        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        // 如果没有找到，意味着请求的哈希值大于所有虚拟节点的哈希值，选择哈希值最大的虚拟节点。
        if (subMap.isEmpty()) {
            key = shards.lastKey();
        } else {  // 否则，选择tailMap中第一个虚拟节点。
            key = subMap.firstKey();
        }
        // 返回真实节点：从选中的虚拟节点virtualNode中提取出真实节点的名称（即虚拟节点名称去掉 &&VN<i> 部分。
        String virtualNode = shards.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * 添加节点
     */
    public  void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            System.out.println("真实节点[" + node + "] 上线添加");
            createVirtualNode(node);
        }
    }

    /**
     * 删除节点
     */
    public  void delNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            System.out.println("真实节点[" + node + "] 下线移除");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被移除");
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
        // 生成一个随机字符串(UUID)，然后将其作为 node 参数传递给 getServer 方法，来获取相应的服务器地址
        String random= UUID.randomUUID().toString();
        return getServer(random,addressList);
    }

}
