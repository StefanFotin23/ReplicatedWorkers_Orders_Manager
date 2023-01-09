import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductsRunnable implements Runnable {
    private final Scanner productsScanner;
    private final ExecutorService productsThreadPool;
    private final AtomicInteger productsInQueue;
    private final AtomicInteger shippedProducts;
    private final String orderName;
    private final Integer orderProductsNumber;

    public ProductsRunnable(Scanner orderProductsScanner, ExecutorService threadPool, AtomicInteger productsInQueue,
                            AtomicInteger shippedProducts, String orderName, Integer orderProductsNumber) {
        this.productsScanner = orderProductsScanner;
        this.productsThreadPool = threadPool;
        this.productsInQueue = productsInQueue;
        this.orderName = orderName;
        this.orderProductsNumber = orderProductsNumber;
        this.shippedProducts = shippedProducts;
    }

    @Override
    public void run() {
        String ordersOutString = System.getProperty("user.dir") + "/orders_out.txt";
        String productsOutString = System.getProperty("user.dir") + "/order_products_out.txt";

        String orderProductsLine;
        synchronized (productsScanner) {
            orderProductsLine = productsScanner.nextLine();
        }
        List<String> productsLineList = Arrays.asList(orderProductsLine.split(","));
        String productOrderName = productsLineList.get(0);
        String productName = productsLineList.get(1);

        if (productOrderName == null) {
            System.out.println("ProductOrderName = null");
        }

        if (productOrderName.equals(orderName)) {
            shippedProducts.incrementAndGet();
            //Put SHIPPED to product from order
            try {
                FileWriter productsWriter = new FileWriter(productsOutString, true);
                productsWriter.write(orderName + "," + productName + ",shipped\n");
                productsWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred at order_products_out.txt");
                e.printStackTrace();
            }
        }

        if (productsScanner.hasNextLine() && shippedProducts.get() < orderProductsNumber) {
            productsInQueue.incrementAndGet();
            productsThreadPool.submit(new ProductsRunnable(productsScanner, productsThreadPool,
                    productsInQueue, shippedProducts, orderName, orderProductsNumber));
        }

        if (shippedProducts.get() > orderProductsNumber) {
            productsThreadPool.shutdown();
            productsScanner.close();
        }

        //If we got all N products of the order, we finnish it
        if (shippedProducts.get() == orderProductsNumber) {
            productsThreadPool.shutdown();
            productsScanner.close();
            //End the pool, so no other threads are created further more

            if (shippedProducts.get() != 0) {
                //Put SHIPPED to Order
                try {
                    FileWriter ordersWriter = new FileWriter(ordersOutString, true);
                    ordersWriter.write(orderName + "," + orderProductsNumber + ",shipped\n");
                    ordersWriter.close();
                } catch (IOException e) {
                    System.out.println("An error occurred at orders_out.txt");
                    e.printStackTrace();
                }
            }
        }
    }
}
