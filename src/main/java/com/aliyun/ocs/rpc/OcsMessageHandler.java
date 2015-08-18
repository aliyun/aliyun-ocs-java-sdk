package com.aliyun.ocs.rpc;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class OcsMessageHandler extends SimpleChannelHandler {

	private OcsRpcContext context;

	public OcsMessageHandler(OcsRpcContext context) {
		this.context = context;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

		this.context.exceptionCaught(ctx.getChannel(), e.getCause());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Channel c = ctx.getChannel();
		OcsReplyMessageWrapper m = (OcsReplyMessageWrapper) e.getMessage();
		this.context.messageReceived(c, m);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		this.context.channelDisconnected(ctx.getChannel());
	}
}