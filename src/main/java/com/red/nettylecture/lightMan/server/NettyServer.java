package com.red.nettylecture.lightMan.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Netty learn
 *
 * @date: 2020-02-01 下午 03:48
 * @author: zhaobo0119@outlook.com
 * @Descriptopn: 原文地址 [https://juejin.cn/post/6844903624372387853]
 */
public class NettyServer {

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
                    // handler()用于指定在服务端启动过程中的一些逻辑，通常情况下呢，我们用不着这个方法。
                    .handler(new ChannelInitializer<NioServerSocketChannel>() {
                        protected void initChannel(NioServerSocketChannel ch) {
                            System.out.println("服务端启动中");
                            // 获取 attr 参数
                            System.out.println(ch.attr(AttributeKey.newInstance("serverName")).get());
                        }
                    })
                    // attr()方法可以给服务端的channel，也就是NioServerSocketChannel指定一些自定义属性，
                    // 我们可以通过channel.attr()取出这个属性，
                    // 比如，上面的代码我们指定我们服务端channel的一个serverName属性，属性值为nettyServer，
                    // 其实说白了就是给NioServerSocketChannel维护一个map而已，通常情况下，我们也用不上这个方法。
                    .attr(AttributeKey.newInstance("serverName"), "nettyServer")
                    // childAttr可以给每一条连接指定自定义属性，然后后续我们可以通过channel.attr()取出该属性，详情请看视频演示
                    .childAttr(AttributeKey.newInstance("clientId"), "Secret")
                    // 除了给每个连接设置这一系列属性之外，我们还可以给服务端channel设置一些属性，最常见的就是so_backlog，如下设置
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // childOption()可以给每条连接设置一些TCP底层相关的属性，比如上面，我们设置了三种TCP属性，其中
                    // ChannelOption.SO_KEEPALIVE表示是否开启TCP底层心跳机制，true为开启
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // ChannelOption.SO_REUSEADDR表示端口释放后立即就可以被再次使用，因为一般来说，一个端口释放后会等待两分钟之后才能再被使用。
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    // ChannelOption.TCP_NODELAY表示是否开始Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 我们调用childHandler()方法，给这个引导类创建一个ChannelInitializer，
                    // 这里主要就是定义后续每条连接的数据读写，业务处理逻辑，不理解没关系，在后面我们会详细分析。
                    // ChannelInitializer这个类中，我们注意到有一个泛型参数NioSocketChannel，这个类呢，就是Netty对NIO类型的连接的抽象，
                    // 而我们前面NioServerSocketChannel也是对NIO类型的连接的抽象，
                    // NioServerSocketChannel和NioSocketChannel的概念可以和BIO编程模型中的ServerSocket以及Socket两个概念对应上
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            // 读取childAttr 存入参数
                            System.out.println(nioSocketChannel.attr(AttributeKey.newInstance("clientKey")).get());
                        }
                    });

            // 我们的最小化参数配置到这里就完成了，我们总结一下就是，要启动一个Netty服务端，
            // 必须要指定三类属性，分别是线程模型、IO模型、连接读写处理逻辑，有了这三者，之后在调用bind(9999)，我们就可以在本地绑定一个9999端口启动起来
            bind(serverBootstrap, 1000);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap
                // serverBootstrap.bind()这个方法呢，它是一个异步的方法，调用之后是立即返回的，他的返回值是一个ChannelFuture，
                // 我们可以给这个ChannelFuture添加一个监听器GenericFutureListener，
                // 然后我们在GenericFutureListener的operationComplete方法里面，我们可以监听端口是否绑定成功，接下来是监测端口是否绑定成功的代码片段
                .bind(port)
                // 我们接下来从1000端口号，开始往上找端口号，直到端口绑定成功，
                // 我们要做的就是在 if (future.isSuccess())的else逻辑里面重新绑定一个递增的端口号
                .addListener(new GenericFutureListener<Future<? super Void>>() {
                    public void operationComplete(Future<? super Void> future) {
                        if (future.isSuccess()) {
                            System.out.println("端口[" + port + "]绑定成功!");
                        } else {
                            System.err.println("端口[" + port + "]绑定失败!");
                            bind(serverBootstrap, port + 1);
                        }
                    }
                });
    }
}
