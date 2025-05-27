package Client.serviceCenter.ZkWatcher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import Client.cache.serviceCache;


public class watchZK {
    // curator 提供的 zookeeper 客户端
    private CuratorFramework client;
    //本地缓存
    serviceCache cache;

    public watchZK(CuratorFramework client, serviceCache cache){
        this.client = client;
        this.cache = cache;
    }

    /**
     * 监听当前节点和子节点的 更新，创建，删除
     */
    public void watchToUpdate(String path) throws InterruptedException {
        // 用于监视指定路径下的节点变化，并在节点变化时更新本地缓存
        // CuratorCache是curator提供的一个用于监听节点变化的API
        // 他会监听指定路径节点变化，这里监听的是根路径
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        // 注册一个监听器，用于处理节点变化事件
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                // type: 事件类型（枚举）
                // childData: 节点更新前的状态、数据
                // childData1: 节点更新后的状态、数据
                // 创建节点时：节点刚被创建，不存在 更新前节点，所以 childData 为 null
                // 删除节点时：节点被删除，不存在 更新后节点，所以 childData1 为 null
                switch (type.name()) {
                    case "NODE_CREATED": // 节点新建 (监听器首次启动且节点存在也会触发次事件)
                        String[] pathList = parsePath(childData1);
                        if (pathList.length <= 2) break;
                        else {
                            String serviceName = pathList[1];
                            String address = pathList[2];
                            // 将新注册的服务加入到本地缓存中
                            cache.addServiceToCache(serviceName, address);
                        }
                        break;
                    case "NODE_CHANGED": // 节点更新
                        if (childData.getData() != null) {
                            System.out.println("修改前的数据: " + new String(childData.getData()));
                        } else {
                            System.out.println("节点第一次赋值!");
                        }
                        String[] oldPathList = parsePath(childData);
                        String[] newPathList = parsePath(childData1);
                        cache.replaceServiceAddress(oldPathList[1], oldPathList[2], newPathList[2]);
                        System.out.println("修改后的数据: " + new String(childData1.getData()));
                        break;
                    case "NODE_DELETED": // 节点删除
                        String[] pathList_d = parsePath(childData);
                        if (pathList_d.length <= 2) break;
                        else {
                            String serviceName = pathList_d[1];
                            String address = pathList_d[2];
                            //将新注册的服务加入到本地缓存中
                            cache.delete(serviceName, address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        //开启监听
        curatorCache.start();
    }
    //解析节点对应地址
    public String[] parsePath(ChildData childData){
        //获取更新的节点的路径
        String path = childData.getPath();
        //按照格式 ，读取
        return path.split("/");
    }
}