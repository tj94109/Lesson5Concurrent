package com.terrance.Lesson5Concurrent;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Lesson5Concurrent {

    private static long count = 0;
    private static int numberOfThreads =0;

   //Paths to .class and .java files
    private static String fileJava = "src/main/java/com/terrance/Lesson5Concurrent/Lesson5Concurrent.java";
    private static String pathJava = System.getProperty("user.dir") + File.separator + fileJava;
    private static String pathClass = Lesson5Concurrent.class.getResource("Lesson5Concurrent.class").getPath();

    public static AtomicLong countAtomic = new AtomicLong(0);
    public static Lock lock = new ReentrantLock();


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String arg = "";
        numberOfThreads = Integer.parseInt(args[1]);
        System.out.println("Threads: "  + numberOfThreads);

        ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);

        if(args.length > 2){

            // --ReentrantLock Option
            if(args[2].equalsIgnoreCase("--ReentrantLock"))

                for(int i =0 ; i <= numberOfThreads; i++){
                    exec.execute(new countBothJavaAndClassUsingLock());
                }
                try{
                    exec.shutdown();
                    exec.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("Reentrant Count Total: " + count);// Results : 175032 92736
                }

            // --AtomicLong Option
            if(args[2].equalsIgnoreCase("--AtomicLong"))

                try{
                    for(int i =0 ; i <= numberOfThreads; i++){
                        Thread thread = new Thread(new countBothJavaAndClassUsingAtomiclong());
                        thread.start();
                        thread.join(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    System.out.println("AtomicCount Total: " + countAtomic.get()); //173400 92826
                }


        }else{  // default execution no lock

            for(int i =0 ; i <= numberOfThreads; i++){
                exec.execute(new countBothJavaAndClass());
            }

            try{
                exec.shutdown();
                exec.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                System.out.println("No Lock Count Total: " + count); //148457 73272
            }

        }

    }
    //No Lock
    public static class countBothJavaAndClass implements Runnable{

        @Override
        public void run() {
            try {
                countCharacters(pathClass);
                countCharacters(pathJava);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class countBothJavaAndClassUsingLock implements Runnable{

        @Override
        public void run() {
            try {
                countCharactersLock(pathClass);
                countCharactersLock(pathJava);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //Atomic Long Class
    public static class countBothJavaAndClassUsingAtomiclong implements Runnable{

        @Override
        public void run() {
            try {
                countCharactersAtomicLong(pathClass);
                countCharactersAtomicLong(pathJava);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //default count characters method
    public static void countCharacters(String myFile) throws IOException {
        Path path = Paths.get(myFile);

        File file = new File(myFile);
        FileInputStream input = new FileInputStream(file);
        byte[] byteArray = new byte[(int) file.length()];
        input.read(byteArray);
        input.close();

        String s  = new String(byteArray);
        String[] lines = s.split("\\r?\\n");
        for(String line : lines) {
            count += line.replace(" ","").length();
        }
        //System.out.println(path.getFileName() + " : " + count);
    }

    // Count Characters Method using Lock
    public static void countCharactersLock(String myFile) throws IOException {
        Path myPath = Paths.get(myFile);

        File file = new File(myFile);
        FileInputStream input = new FileInputStream(file);
        byte[] byteArray = new byte[(int) file.length()];
        input.read(byteArray);
        input.close();

        String s  = new String(byteArray);
        String[] lines = s.split("\\r?\\n");
        for(String line : lines) {
            lock.lock();
            try{
                count += line.replace(" ","").length();
            }finally {
                lock.unlock();
            }

        }
        //System.out.println(myPath.getFileName() + " : " + count);
    }

    //default count characters method using Atomic Long
    public static void countCharactersAtomicLong(String myFile) throws IOException {
        Path path = Paths.get(myFile);

        File file = new File(myFile);
        FileInputStream input = new FileInputStream(file);
        byte[] byteArray = new byte[(int) file.length()];
        input.read(byteArray);
        input.close();
        String s  = new String(byteArray);
        String[] lines = s.split("\\r?\\n");
        for(String sl : lines) {
            countAtomic.getAndAdd(sl.replace(" ","").length());
        }
        //System.out.println(path.getFileName() + " : " + countAtomic.get());
    }

}
