import java.io.*;
import java.net.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws UnknownHostException, IOException
    {
        ServerSocket server = null;
        final Socket socket = new Socket();
        List<Process> objects = null;

        int n = 100000000;
        int fromClient[] = new int[n];

        boolean doParallel = true;
  
        try {
            // server is listening on port 1234
            server = new ServerSocket(5555);
            System.out.println("listening on port 5555");
            server.setReuseAddress(true);


            Socket client = server.accept();
  
                // Displaying that new client is connected
                // to server
            System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

            DataInputStream inFromClient = new DataInputStream(client.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());

            //System.out.println("\nUnsorted array");
            //for(int i = 0 ; i < n; i++) {
            //    fromClient[i] = inFromClient.readInt();
            //    System.out.println(i + ": " + fromClient[i]);
            //}

            if(doParallel){
                // Test custom (multi-threaded) merge sort (recursive merge) implementation
                int[] arr = Arrays.copyOf(fromClient, fromClient.length);
                long t = System.nanoTime();
                threadedSort(arr);
                long f = System.nanoTime();
                long time = f - t;
                System.out.println("Time spent for multi threaded recursive mergesort: " + time + " nanoseconds");
            }

            if(!doParallel){
                long t = System.nanoTime();
                sort(fromClient, 0, fromClient.length - 1);
                long f = System.nanoTime();
                long time = f - t;

                System.out.println("Time spent for single threaded recursive mergesort: " + time + " nanoseconds");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //method to merge arrays for merge sort
    static void merge(int arr[], int l, int m, int r)
    {
        int n1 = m - l + 1;
        int n2 = r - m;
  
        int L[] = new int[n1];
        int R[] = new int[n2];
  

        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];
  

        int i = 0, j = 0;

        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            }
            else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
  
    // Main sort function for recursive merge sort
    static void sort(int arr[], int l, int r)
    {
        if (l < r) {
            int m = l + (r - l) / 2;
  
            sort(arr, l, m);
            sort(arr, m + 1, r);
  
            merge(arr, l, m, r);
        }
    }

    //thread count
    private static final int Threads = 2;
       
    //custom thread class that extends Thread
    private static class SortThreads extends Thread{
        SortThreads(int[] arr, int start, int end){

            sort(arr, start, end);
  
            this.start();
        }
    }
     
      //function for threaded recursive merge sort
    public static void threadedSort(int[] array){
        final int length = array.length;

        //find if problem can be split evenly among threads otherwise, determine workload for each thread
        boolean exact = length % Threads == 0;
        int maxlim = exact? length / Threads: length / (Threads-1);

        maxlim = maxlim < Threads? Threads : maxlim;

        //list of all threads
        final ArrayList<SortThreads> threads = new ArrayList<>();

        //split arrays and send to different threads to sort
        for(int i=0; i < length; i += maxlim){
            int beg = i;
            int remain = (length)-i;
            int end = remain < maxlim? i + (remain - 1): i + (maxlim -1 ); 
            final SortThreads t = new SortThreads(array, beg, end);
            threads.add(t);
        }
        //join all the threads
        for(Thread t: threads){
            try{
                t.join();
            } catch(InterruptedException ignored){}
        }

        //merge the arrays
        for(int i = 0; i < length; i += maxlim){
            int mid = i == 0? 0 : i - 1;
            int remain = (length) - i;
            int end = remain < maxlim? i + (remain - 1): i + (maxlim - 1);
            merge(array, 0, mid, end);
        }
    }

}
