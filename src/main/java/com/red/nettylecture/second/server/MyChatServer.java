package com.red.nettylecture.second.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @date: 2020-02-01 下午 03:48
 * @author: zhaobo0119@outlook.com
 * @Descriptopn:
 */
public class MyChatServer {

    public static void main(String[] args) throws Exception {
        // 首先看到，我们创建了两个NioEventLoopGroup，这两个对象可以看做是传统IO编程模型的两大线程组，
        // boosGroup表示监听端口，创建新连接的线程组，workerGroup表示处理每一条连接的数据读写的线程组，
        // 用生活中的例子来讲就是，一个工厂要运作，必然要有一个老板负责从外面接活，然后有很多员工，负责具体干活，
        // 老板就是boosGroup，员工们就是workerGroup，boosGroup接收完连接，扔给workerGroup去处理。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建了一个引导类 ServerBootstrap，这个类将引导我们进行服务端的启动工作，直接new出来开搞。
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.
                    // 我们通过.group(boosGroup, workerGroup)给引导类配置两大线程，这个引导类的线程模型也就定型了
                    group(bossGroup, workerGroup)
                    // 我们指定我们服务端的IO模型为NIO，我们通过.channel(NioServerSocketChannel.class)来指定IO模型，
                    // 当然，这里也有其他的选择，如果你想指定IO模型为BIO，那么这里配置上OioServerSocketChannel.class类型即可，
                    // 当然通常我们也不会这么做，因为Netty的优势就在于NIO。
                    .channel(NioServerSocketChannel.class)
                    // 我们调用childHandler()方法，给这个引导类创建一个ChannelInitializer，
                    // 这里主要就是定义后续每条连接的数据读写，业务处理逻辑，不理解没关系，在后面我们会详细分析。
                    // ChannelInitializer这个类中，我们注意到有一个泛型参数NioSocketChannel，这个类呢，就是Netty对NIO类型的连接的抽象，
                    // 而我们前面NioServerSocketChannel也是对NIO类型的连接的抽象，
                    // NioServerSocketChannel和NioSocketChannel的概念可以和BIO编程模型中的ServerSocket以及Socket两个概念对应上
                    .childHandler(new MyChatServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
