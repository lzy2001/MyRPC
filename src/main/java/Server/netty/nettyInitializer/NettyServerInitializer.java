package Server.netty.nettyInitializer;


import Common.serializer.myCode.MyDecoder;
import Common.serializer.myCode.MyEncoder;
import Common.serializer.mySerializer.JsonSerializer;
import Server.netty.handler.NettyRPCServerHandler;
import Server.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 自定义编解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
