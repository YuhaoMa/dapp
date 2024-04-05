package staff;
import java.rmi.Naming;
import java.time.LocalDate;
import java.util.Set;
import java.time.Instant;
import java.time.Duration;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import hotel.BookingDetail;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import hotel.IBookingManager;
import hotel.BookingManager;

public class BookingClient extends AbstractScriptedSimpleTest {

	private IBookingManager bm;

	public static void main(String[] args) throws Exception {
		BookingClient client = new BookingClient();

		//client.run1();
		//client.run2();
		client.run3();
	}

	/***************
	 * CONSTRUCTOR *
	 ***************/
	public BookingClient() {
		try {
			Instant startTime = Instant.now(); // 记录连接开始时间

			// 设置服务器的主机名
			String serverHostname = "yiman.japaneast.cloudapp.azure.com";

			// 设置注册表的端口号
			int registryPort = 1098;

			// 构建远程对象的URL
			String url = "//" + serverHostname + ":" + registryPort + "/BookingManager";

			// 查找远程对象
			bm = (IBookingManager) Naming.lookup(url);

			Instant endTime = Instant.now(); // 记录连接成功时间
			Duration duration = Duration.between(startTime, endTime); // 计算连接用时

			// 在这里可以调用远程对象的方法进行操作
			// bm.someMethod();

			System.out.println("Connected to BookingManager successfully in " + duration.toMillis() + " milliseconds.");
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}



	// 实现抽象方法，调用远程对象的方法
	@Override
	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) throws Exception {
		return bm.isRoomAvailable(roomNumber, date);
	}

	@Override
	public void addBooking(BookingDetail bookingDetail) throws Exception {
		bm.addBooking(bookingDetail);
	}

	@Override
	public Set<Integer> getAvailableRooms(LocalDate date) throws Exception {
		return bm.getAvailableRooms(date);
	}

	@Override
	public Set<Integer> getAllRooms() throws Exception {
		return bm.getAllRooms();
	}
}

