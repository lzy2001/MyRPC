package Common.serializer.mySerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import Common.Message.RpcRequest;
import Common.Message.RpcResponse;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj;
        // 传输的消息分为request与response
        switch (messageType){
            case 0:
                // 将字节数组转化为 RpcRequest 对象
                RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);
                // 创建一个Object数组，用于存储解析后的请求参数
                Object[] objects = new Object[request.getParams().length];
                // 把json字串转化成对应的对象， fastjson可以读出基本数据类型，不用转化
                // 对转换后的request中的params属性逐个进行类型判断
                for(int i = 0; i < objects.length; i++){
                    // 获取目标参数类型
                    Class<?> paramsType = request.getParamsType()[i];
                    //判断每个对象类型是否和paramsTypes中的一致
                    if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())){
                        //如果不一致，就行进行类型转换
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i],request.getParamsType()[i]);
                    }else{
                        //如果一致就直接赋给objects[i]
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1:
                // 将字节数组转化为 RpcResponse 对象
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                // 根据对象获取对象类型
                Class<?> dataType = response.getDataType();
                // 判断转化后的response对象中的data的类型是否正确
                if (!dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject) response.getData(), dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }

    //1 代表json序列化方式
    @Override
    // 默认使用 JsonSerializer
    public int getType() {
        return 1;
    }
}
