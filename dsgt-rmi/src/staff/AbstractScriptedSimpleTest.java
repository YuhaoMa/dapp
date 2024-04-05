package staff;
import java.util.Arrays;
import java.util.Set;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;
import java.io.IOException;
import hotel.BookingDetail;

public abstract class AbstractScriptedSimpleTest {

	private final LocalDate today = LocalDate.now();
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final List<Long> responseTimes = new ArrayList<>();
	private int successfulRequests = 0;  // 计数成功的请求

	protected abstract boolean isRoomAvailable(Integer room, LocalDate date) throws Exception;
	protected abstract void addBooking(BookingDetail bookingDetail) throws Exception;
	protected abstract Set<Integer> getAvailableRooms(LocalDate date) throws Exception;
	protected abstract Set<Integer> getAllRooms() throws Exception;

	public void run1() throws Exception {
		final int totalRequests = 100;
		final int parallelClients = 1;
		final int requestsPerClient = totalRequests / parallelClients;
		try {

			for (int i = 0; i < parallelClients; i++) {
				final int clientIndex = i;  // 为了在Lambda表达式中使用
				executor.submit(() -> {
					for (int j = 0; j < requestsPerClient; j++) {
						// 确保每个客户端使用唯一的房间范围
						int roomNumber = 101 + j + (clientIndex * requestsPerClient);
						String guestName = "Guest" + (clientIndex * requestsPerClient + j);
						testScenario(roomNumber, guestName);
					}
				});
			}
			executor.shutdown();
			boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
			if (!finished) {
				System.err.println("Not all tasks completed within the allowed time");
			}
			System.out.println("Single-Client:1,Total-Request:100,Different Avaliable Rooms ");
			printTestResults();
		} finally {
			if (!executor.isShutdown()) {
				executor.shutdownNow();
			}
		}
	}
	public void run2() throws Exception {
		final int totalRequests = 100;
		final int parallelClients = 10;
		final int requestsPerClient = totalRequests / parallelClients;
		try {

			for (int i = 0; i < parallelClients; i++) {
				final int clientIndex = i;  // 为了在Lambda表达式中使用
				executor.submit(() -> {
					for (int j = 0; j < requestsPerClient; j++) {
						// 确保每个客户端使用唯一的房间范围
						int roomNumber = 201 + j + (clientIndex * requestsPerClient);
						String guestName = "Guest" + (clientIndex * requestsPerClient + j);
						testScenario(roomNumber, guestName);
					}
				});
			}
			executor.shutdown();
			boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
			if (!finished) {
				System.err.println("Not all tasks completed within the allowed time");
			}
			System.out.println("Multi-Client:10,Total-Request:100,Different Avaliable Rooms ");
			printTestResults();
		} finally {
			if (!executor.isShutdown()) {
				executor.shutdownNow();
			}
		}
	}
	public void run3() throws Exception {
		final int totalRequests = 200;
		final int parallelClients = 10;
		final int requestsPerClient = totalRequests / parallelClients;
		try {


			for (int i = 0; i < parallelClients; i++) {
				final int clientIndex = i;  // 为了在Lambda表达式中使用
				executor.submit(() -> {
					for (int j = 0; j < requestsPerClient+10; j++) {
						// 确保每个客户端使用唯一的房间范围
						int roomNumber = 301 + j + (clientIndex * requestsPerClient);
						String guestName = "Guest" + (clientIndex * requestsPerClient + j);
						testScenario(roomNumber, guestName);
					}
				});
			}
			executor.shutdown();
			boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
			if (!finished) {
				System.err.println("Not all tasks completed within the allowed time");
			}
			System.out.println("Multi-Client:10,Total-Request:300,10/30 Conflict Request ");
			printTestResults();
		} finally {
			if (!executor.isShutdown()) {
				executor.shutdownNow();
			}
		}
	}

	private void testScenario(Integer roomNumber, String guestName) {
		try {
			long startTime = System.nanoTime();

			boolean available = isRoomAvailable(roomNumber, today);
			if (available) {
				BookingDetail bookingDetail = new BookingDetail(guestName, roomNumber, today);
				addBooking(bookingDetail);
				successfulRequests++;  // 增加成功请求的计数
			}

			long endTime = System.nanoTime();
			long responseTime = endTime - startTime;
			synchronized (responseTimes) {
				responseTimes.add(responseTime);
			}

		} catch (Exception e) {
			System.err.println("Error during testing for room " + roomNumber + " by " + guestName
					+ ": " + e.getMessage());
		}
	}

	private void printTestResults() {
		double totalResponseTime = responseTimes.stream().mapToDouble(Long::doubleValue).sum();
		double averageResponseTime = totalResponseTime / responseTimes.size() / 1_000_000.0; // convert to ms
		System.out.println("Average response time: " + String.format("%.3f ms", averageResponseTime));
		System.out.println("Success rate: " + String.format("%.3f%%", 100.0 * successfulRequests / responseTimes.size()));
		System.out.println("Total requests made: " + responseTimes.size());
		System.out.println("Total successful requests: " + successfulRequests);

		// 调用方法将responseTimes保存到CSV
		saveResponseTimesToCSV(responseTimes, "run3-Japan.csv");
	}
	private void checkAvailableRoomsOutput(int expectedSize, Integer[] expectedAvailableRooms) throws Exception {
		Set<Integer> availableRooms = getAvailableRooms(today);
		if (availableRooms != null && availableRooms.size() == expectedSize) {
			if (availableRooms.containsAll(Arrays.asList(expectedAvailableRooms))) {
				System.out.println("List of available rooms (room ID) " + getAvailableRooms(today) + " [CORRECT]\n");
			} else {
				System.out.println("List of available rooms (room ID) " + getAvailableRooms(today) + " [INCORRECT]\n");
			}
		} else {
			System.out.println("List of available rooms (room ID) " + getAvailableRooms(today) + " [INCORRECT]\n");
		}
	}
	private void printAllRooms() throws Exception {
		System.out.println("List of rooms (room ID) in the hotel " + getAllRooms());
	}
	private void saveResponseTimesToCSV(List<Long> responseTimes, String fileName) {
		try (FileWriter writer = new FileWriter(fileName)) {
			// 写入列标题
			writer.append("Response Time (ns)\n");

			// 遍历responseTimes列表，写入每个响应时间
			for (Long responseTime : responseTimes) {
				writer.append(String.valueOf(responseTime)).append("\n");
			}

			System.out.println("Data saved to " + fileName);
		} catch (IOException e) {
			System.err.println("Error writing to CSV file: " + e.getMessage());
		}
	}

}
