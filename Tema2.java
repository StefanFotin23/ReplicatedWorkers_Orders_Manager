import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Run the program: java Tema2 <folder_input> <nr_max_threads>");
            return;
        }
        String folderInput = args[0];
        int P = Integer.parseInt(args[1]);

        //Output files path
        String ordersOutString = System.getProperty("user.dir") + "/orders_out.txt";
        String productsOutString = System.getProperty("user.dir") + "/order_products_out.txt";

        try {
            //Delete the contents of the output files
            PrintWriter ordersWriter = new PrintWriter(ordersOutString);
            ordersWriter.print("");
            ordersWriter.close();

            PrintWriter productsWriter = new PrintWriter(productsOutString);
            productsWriter.print("");
            productsWriter.close();

            //Prepare Scanners for reading
            String orderProductsString = folderInput + "/order_products.txt";
            String ordersString = folderInput + "/orders.txt";

            File orders = new File(ordersString);
            Scanner ordersScanner = new Scanner(orders);

            //Order Threading part
            AtomicInteger ordersInQueue = new AtomicInteger(0);
            ExecutorService orderThreadPool = Executors.newFixedThreadPool(P);

            ordersInQueue.incrementAndGet();
            orderThreadPool.submit(new OrderRunnable(ordersScanner, orderThreadPool, ordersInQueue, P,
                    orderProductsString));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
