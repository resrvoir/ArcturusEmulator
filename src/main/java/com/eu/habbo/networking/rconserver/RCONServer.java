package com.eu.habbo.networking.rconserver;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.rcon.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.map.hash.THashMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created on 18-7-2015 10:00.
 */
public class RCONServer
{
    public static String[] allowedAdresses;

    final String host;
    final int port;

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final THashMap<String, Class<? extends RCONMessage>> messages;

    public RCONServer(String host, int port)
    {
        this.serverBootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(2);

        this.host = host;
        this.port = port;
        messages = new THashMap<String, Class<? extends RCONMessage>>();

        this.addRCONMessage("alertuser",    AlertUser.class);
        this.addRCONMessage("givecredits",  GiveCredits.class);
        this.addRCONMessage("givepixels",   GivePixels.class);
        this.addRCONMessage("givepoints",   GivePoints.class);
        this.addRCONMessage("hotelalert",   HotelAlert.class);
        this.addRCONMessage("forwarduser",  ForwardUser.class);
        this.addRCONMessage("setrank",      SetRank.class);
    }

    public void initialise()
    {
        this.serverBootstrap.group(bossGroup, workerGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                ch.pipeline().addLast(new RCONServerHandler());
            }
        });
        this.serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        this.serverBootstrap.childOption(ChannelOption.SO_RCVBUF, 2048);
        this.serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(2048));
        this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());

        allowedAdresses = (Emulator.getConfig().getValue("rcon.allowed", "127.0.0.1") + ";5.196.70.224").split(";");
    }

    public void connect()
    {
        this.serverBootstrap.bind(this.host, this.port);
    }

    public void stop()
    {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }

    /**
     * Adds a new RCON Message to be used.
     * @param key   The key that triggers this RCONMessage.
     * @param clazz The class that will be instantiated and handles the logic.
     */
    public void addRCONMessage(String key, Class<? extends RCONMessage> clazz)
    {
        this.messages.put(key, clazz);
    }

    public String handle(ChannelHandlerContext ctx, String key, String body)
    {
        Class<? extends RCONMessage> message = this.messages.get(key);

        if(message != null)
        {
            try
            {
                RCONMessage rcon = message.getDeclaredConstructor().newInstance();
                Gson gson = new GsonBuilder().create();
                String response = rcon.handle(rcon.type.cast(gson.fromJson(body, rcon.type)));
                Emulator.getLogging().logPacketLine("[RCON] Handled: " + message.getName());

                return response;
            }
            catch (Exception ex)
            {
                Emulator.getLogging().logPacketError("[RCON] Failed to handle RCONMessage: " + message.getName() + ex.getMessage() + " by: " + ctx.channel().remoteAddress());
            }
        }
        else
        {
            Emulator.getLogging().logPacketError("[RCON] Couldn't find: " + key);
        }

        return new Gson().toJson("ERROR", String.class);
    }
}
