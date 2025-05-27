package Common.serializer.myCode;

import Common.Message.MessageType;
import Common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {
    @Override
    // 它负责传入的字节流解码为业务对象，并将解码后的对象添加到 out 中，供下一个 handler 处理
    // ctx 是 Netty 的 channelHandlerContext 对象，提供对管道、通道和事件的访问
    // in 是 ByteBuf 对象，接收到的字节流，它是 netty 的缓冲区，可以理解为字节数组
    // out.add(obj) 会将解码后的 Java 对象放入队列中
    // Netty 自动将队列中的对象传递到 下一个入站处理器（InboundHandler）
    // Netty 检测到 out 不为空 → 将里面的对象作为参数调用下一个 handler 的 channelRead0() 方法
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 1.读取消息类型
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if(messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂不支持此种数据");
            return;
        }
        // 2.读取序列化的方式&类型
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null)
            throw new RuntimeException("不存在对应的序列化器");
        // 3.读取序列化数组长度
        int length = in.readInt();
        // 4.读取序列化数组
        byte[] bytes=new byte[length];
        in.readBytes(bytes);
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }
}
