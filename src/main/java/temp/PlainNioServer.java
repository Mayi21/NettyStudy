package temp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer {
	public void server(int port) throws Exception {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		ServerSocket serverSocket = serverSocketChannel.socket();
		InetSocketAddress socketAddress = new InetSocketAddress(port);
		serverSocket.bind(socketAddress);
		Selector selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		final ByteBuffer msg = ByteBuffer.wrap("Hi \r\n".getBytes());
		while (true) {
			try {
				selector.select();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				try {
					if (selectionKey.isAcceptable()) {
						ServerSocketChannel socketChannel = (ServerSocketChannel) selectionKey.channel();
						SocketChannel client = socketChannel.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
						System.out.println("Accept connection from " + client);
					}
					if (selectionKey.isWritable()) {
						SocketChannel client = (SocketChannel) selectionKey.channel();
						ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
						while (buffer.hasRemaining()) {
							if (client.write(buffer) == 0) {
								break;
							}
						}
						client.close();
					}
				} catch (IOException e) {
					selectionKey.cancel();
					try {
						selectionKey.channel().close();
					} catch (IOException cex) {

					}
				}
			}
		}
	}
}
