package Common.serializer.myCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import Common.Message.MessageType;
import Common.Message.RpcRequest;
import Common.Message.RpcResponse;
import Common.serializer.mySerializer.Serializer;

@AllArgsConstructor
// MessageToByteEncoder是 netty 专门设计用来实现编码器的抽象类，可以帮助开发者将Java对象编码成字节数据
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;
    @Override
    // ctx : netty 提供的上下文对象，代表管道上下文，包含通道和处理器相关信息
    // msg : 要被编码的消息对象
    // out : netty 提供的字符缓冲区，编码后的字节数据写入其中
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        // 1.写入消息类型
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        else if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        // 2.写入序列化方式
        out.writeShort(serializer.getType());
        //得到序列化数组
        byte[] serializeBytes = serializer.serialize(msg);
        // 3.写入长度
        out.writeInt(serializeBytes.length);
        // 4.写入序列化数组
        out.writeBytes(serializeBytes);
    }
}
