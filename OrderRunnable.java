import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderRunnable implements Runnable {
    private final Scanner ordersScanner;
    private final int threadNumber;
    private final ExecutorService orderThreadPool;
    private final AtomicInteger orderInQueue;
    private final String productsPath;
    private Integer orderProductsNumber;
    private String orderName;

    public OrderRunnable(Scanner ordersScanner, ExecutorService threadPool, AtomicInteger inQueue, int threadNumber,
                         String productsPath) {
        this.ordersScanner = ordersScanner;
        this.threadNumber = threadNumber;
        this.orderThreadPool = threadPool;
        this.orderInQueue = inQueue;
        this.productsPath = productsPath;
    }

    @Override
    public void run() {
        String orderLine;
        synchronized (ordersScanner) {
            orderLine = ordersScanner.nextLine();
        }
        List<String> orderLineList = Arrays.asList(orderLine.split(","));

        orderName = orderLineList.get(0);
        String orderProductsNumberString = orderLineList.get(1);
        orderProductsNumber = Integer.parseInt(orderProductsNumberString);

        if (orderName == null) {
            System.out.println("OrderName = null");
        }

        if (orderProductsNumber == null) {
            System.out.println("orderProductsNumber = null");
        }

        if (ordersScanner.hasNextLine()) {
            orderInQueue.incrementAndGet();
            orderThreadPool.submit(new OrderRunnable(ordersScanner, orderThreadPool, orderInQueue, threadNumber,
                    productsPath));
        }

        File productsFile = new File(productsPath);
        Scanner productsScanner = null;
        try {
            productsScanner = new Scanner(productsFile);
        } catch (FileNotFoundException e) {
            System.out.println("Error at productsScanner");
            e.printStackTrace();
        }

        //Product Threading part
        AtomicInteger shippedProducts = new AtomicInteger(0);
        AtomicInteger productsInQueue = new AtomicInteger(0);
        ExecutorService productsThreadPool = Executors.newFixedThreadPool(1);

        productsThreadPool.submit(new ProductsRunnable(productsScanner, productsThreadPool, productsInQueue,
                shippedProducts, orderName, orderProductsNumber));

        int left = orderInQueue.decrementAndGet();
        if (left == 0) {
            ordersScanner.close();
            orderThreadPool.shutdown();
        }
    }
}
