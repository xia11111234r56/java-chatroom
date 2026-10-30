//用于发送和接收网络消息（对象）的工具类
//封装了 Java 的 ObjectInputStream 和 ObjectOutputStream，
//实现了通过 Socket 网络连接发送和接收 Java 对象的功能
package jiwangkeshe;
import java.io.*;
import java.net.*;
public class IOStream 
{
	//readMessage()：从 Socket 中读取消息（反序列化一个对象）。
	public static Object readMessage(Socket socket) 
	{	
		Object object = null;
		try {
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);//对象输入流
			object = objectInputStream.readObject();//调用 readObject() 读取并还原对象
		} catch (ClassNotFoundException e) {//如果类找不到或IO异常，就打印错误信息
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object; //返回读到的对象
	}
	//writeMessage()：向 Socket 中写入消息（序列化一个对象）。
	public static void writeMessage(Socket socket, Object message) 
	{
		try {
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(message);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
