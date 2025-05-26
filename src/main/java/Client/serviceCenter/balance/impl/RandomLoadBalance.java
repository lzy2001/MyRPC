package Client.serviceCenter.balance.impl;

import Client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;


public class RandomLoadBalance implements LoadBalance {
    @Override
    public String balance(List<String> addressList) {
        Random random=new Random();
        int choose = random.nextInt(addressList.size());
        System.out.println("负载均衡选择了"+choose+"服务器");
        return addressList.get(choose);
    }
    public void addNode(String node){} ;
    public void delNode(String node){};
}
