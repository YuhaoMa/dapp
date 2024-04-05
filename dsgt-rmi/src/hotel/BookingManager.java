package hotel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.stream.Collectors;

public class BookingManager extends UnicastRemoteObject implements IBookingManager {

	private Room[] rooms;

	public BookingManager() throws RemoteException {
		super();
		this.rooms = initializeRooms();
	}
	@Override
	public boolean isRoomAvailable(Integer roomNumber, LocalDate date) {
		for (Room room : rooms) {
			if (room.getRoomNumber().equals(roomNumber)) {
				return room.getBookings().stream()
						.noneMatch(booking -> booking.getDate().equals(date));
			}
		}
		return false; // Room not found
	}
	@Override
	public void addBooking(BookingDetail bookingDetail) {
		long startTime = System.currentTimeMillis(); // 记录方法开始时间

		System.out.println("Attempting to add booking for guest: " + bookingDetail.getGuest()
				+ " in room " + bookingDetail.getRoomNumber() + " on date " + bookingDetail.getDate());

		for (Room room : rooms) {
			if (room.getRoomNumber().equals(bookingDetail.getRoomNumber())) {
				if (isRoomAvailable(bookingDetail.getRoomNumber(), bookingDetail.getDate())) {
					room.getBookings().add(bookingDetail);
					long endTime = System.currentTimeMillis(); // 记录方法结束时间
					System.out.println("Booking successful for room " + bookingDetail.getRoomNumber()
							+ ". Execution time: " + (endTime - startTime) + " ms");
					return;
				} else {
					long endTime = System.currentTimeMillis(); // 记录方法结束时间
					System.out.println("Booking failed for room " + bookingDetail.getRoomNumber()
							+ " (Room not available). Execution time: " + (endTime - startTime) + " ms");
					return;
				}
			}
		}

		long endTime = System.currentTimeMillis(); // 记录方法结束时间
		System.out.println("Booking failed (Room " + bookingDetail.getRoomNumber() + " not found). "
				+ "Execution time: " + (endTime - startTime) + " ms");
	}


	@Override
	public Set<Integer> getAvailableRooms(LocalDate date) {
		Set<Integer> availableRooms = new HashSet<>();
		for (Room room : rooms) {
			if (isRoomAvailable(room.getRoomNumber(), date)) {
				availableRooms.add(room.getRoomNumber());
			}
		}
		return availableRooms;
	}

	@Override
	public Set<Integer> getAllRooms() {
		Set<Integer> allRooms = new HashSet<>();
		for (Room room : rooms) {
			allRooms.add(room.getRoomNumber());
		}
		return allRooms;
	}

	private Room[] initializeRooms() {
			Room[] rooms = new Room[500]; // 创建一个包含500个房间的数组
			for (int i = 0; i < rooms.length; i++) {
				// 假设房间编号从101开始递增
				rooms[i] = new Room(101 + i);
			}
			return rooms;
	}
	public static void main(String[] args) {
		try {
			// 设置RMI服务使用的主机名或IP地址
			//System.setProperty("java.rmi.server.hostname", "157.56.180.87");
			// 创建并导出远程对象实例
			BookingManager manager = new BookingManager();

			// 启动本地RMI注册表，端口默认为1099
			Registry registry = LocateRegistry.createRegistry(1098);

			// 将远程对象注册到RMI注册表中，使用名字"BookingManager"
			registry.rebind("BookingManager", manager);

			System.out.println("BookingManager is ready.");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
