package laboras2_a;

/**
 *
 * @author Edvinas Dulskas IFF-6/13
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Class for sorting data
class SortStruct{
    private int sortField;
    private int count;
    
    public SortStruct(){}
    
    public SortStruct(int sortField, int count){
        this.sortField = sortField;
        this.count = count;
    }
    
    public int getSortField(){ return sortField; }
    
    public int getCount(){ return count; }
    
    public void setSortField(int sortField){ this.sortField = sortField; }
    
    public void setCount(int count){ this.count = count; }
}

// Class for maker data
class Automobile{
    private String model;
    private int year;
    private double price;
    
    public Automobile(){}
    
    public Automobile(String model, int year, double price){
        this.model = model;
        this.year = year;
        this.price = price;
    }
    
    public String getModel(){ return model; }
    
    public int getYear(){ return year; }
    
    public double getPrice(){ return price; }
    
    public void setModel(String model){ this.model = model; }
    
    public void setYear(int year){ this.year = year; }
    
    public void setPrice(double price){ this.price = price; }
}

// Monitor class
class Monitor{
    private boolean[] isMaking;         // active makers
    private int count;                  // count of shared array
    private SortStruct[] B;             // shared array
    
    public Monitor(int n, int m){
        B = new SortStruct[100];
        isMaking = new boolean[n];
        for(boolean g : isMaking){
            g = true;
        }
        count = 0;
    }
    
    public SortStruct[] getB(){ return B; }
    
    public int getCount(){ return count; }
    
    private int insertPlace(int elem){
        if(count == 0){                      // inssert into front
            return 0;
        }
        for(int i = 0; i < count; i++){      // insert into middle
            if(elem <= B[i].getSortField()){
                return i;
            }
        }
        return count;                       // insert into end
    }
    
    // synchronies method for element insertion
    public synchronized void insert(Automobile aut){
        int field = aut.getYear();
        int place = insertPlace(field);
        
        if(place == 0 && count == 0 || place == count){     // insert into frond or end
            B[place] = new SortStruct(field, 1);
            count++;
        }
        else if(B[place].getSortField() == field){           // if field exists increment by 1
            //B[place].setCount(B[place].getCount()+1);
            B[place] = new SortStruct(field, B[place].getCount()+1);
        }
        else{
            for(int i = count; i > place; i--)
                B[i] = B[i-1];
            B[place] = new SortStruct(field, 1);
            count++;
        }
        notifyAll();
    }
    
    // synchronized method for element removal
    public synchronized boolean remove(SortStruct elem){
        while(count == 0 && this.isStillMaking()){               // check if makers are still making
            try{ wait(); } 
            catch(InterruptedException e){ e.printStackTrace(); }   
        }
        
        if(count == 0 && !this.isStillMaking()){                // there is nothing to remove
            notifyAll();
            return false;
        }
        
        boolean removed = false;
        for(int i = 0; i < count; i++){
            if(B[i].getSortField() == elem.getSortField()){
                int countToRemove = B[i].getCount();
                if(elem.getCount() < B[i].getCount()){
                    B[i].setCount(countToRemove - elem.getCount());
                }
                else{
                    if(i == 0 && count == 1 || i == count - 1){     // if front or end
                        B[i] = null;
                        count--;
                    }
                    else{
                        for(int j = i; j < count-1; j++)
                            B[j] = B[j+1];
                        B[count-1] = null;
                        count--;
                    }
                }
                removed = true;
            }
        }
        
        notifyAll();
        return removed;
    }
    
    public synchronized void setNotMaking(int maker){
        isMaking[maker] = false;
        notifyAll();
    }
    
    public synchronized boolean isStillMaking(){
        for(boolean m : isMaking)
            if(m){
                notifyAll();
                return true;
            }
        notifyAll();
        return false;
    }
}

// class for makers data
class Maker extends Thread {
    private Automobile[] data;
    private int maker;
    
    public Maker(int maker, Automobile[] data){
        this.data = data;
        this.maker = maker;
    }
    
    @Override
    public void run(){
        for(Automobile d : data){
            program.monitor.insert(d);
        }
        program.monitor.setNotMaking(maker);
    }
}

// class for buyer data
class Buyer extends Thread{
    private SortStruct[] data;
    private int buyer;
    
    public Buyer(int buyer, SortStruct[] data){
        this.buyer = buyer;
        this.data = data;
    }
    
    @Override
    public void run(){
        boolean finish = false;
        while(!finish){
            boolean found = false;
            for(SortStruct d : data){
                boolean removed = program.monitor.remove(d);
                if(removed)
                    found = true;
            }
            if(!found && !program.monitor.isStillMaking()){
                for(SortStruct d : data)
                    found = program.monitor.remove(d);
                finish = true;
            }
        }
    }
}

public class program {
    //public static final String dataFILE = "IFF-6-13_DulskasE_L2_dat_1.txt";
    public static final String dataFILE = "IFF-6-13_DulskasE_L2_dat_2.txt";
    //public static final String dataFILE = "IFF-6-13_DulskasE_L2_dat_3.txt";
    public static final String resultFILE = "IFF-6-13_DulskasE_L2_rez.txt";
    public static final int N = 5;
    public static final int M = 4;
    public static final int NprocessDataCount = 31;
    
    public static List<Automobile[]> DataN = new ArrayList<>();
    public static List<SortStruct[]> DataM = new ArrayList<>();
    
    public static Monitor monitor = new Monitor(N, M);
    public static PrintWriter writer;
    
    private static void Read(String fd){
        try(Scanner scanner = new Scanner(new File(fd)).useLocale(Locale.ENGLISH)){
            for(int i = 0; i < N; i ++){
                int count = scanner.nextInt();
                Automobile[] temp = new Automobile[count]; 
                for(int j = 0; j < count; j++){
                    String model = scanner.next();
                    int year = scanner.nextInt();
                    double price = scanner.nextDouble();
                    temp[j] = new Automobile(model, year, price);
                }
                DataN.add(temp);
            }      
            
            for(int i = 0; i < M; i ++){
                int count = scanner.nextInt();
                SortStruct[] temp = new SortStruct[count];
                for(int j = 0; j < count; j++){
                    int field = scanner.nextInt();
                    int cc = scanner.nextInt();
                    temp[j] = new SortStruct(field, cc);
                }
                DataM.add(temp);
            }
            
        } catch(Exception e) { System.out.println(e); }
    }
    
    private static void Print(){
        int lineNr = 1;
        writer.println("     |Automobilių duomenys|");
        writer.println("     |-----------------------------------");
        for (int i = 0; i < N; i++){
            writer.println(String.format("     |%-15s|%-8s|%-7s|", "Modelis", "Metai", "Kaina"));
            writer.println("     |-----------------------------------");
            for (Automobile get : DataN.get(i)) {
                writer.println(String.format("%3d) |%-15s|%-8s|%-7s|", lineNr++, get.getModel(), get.getYear(), get.getPrice()));
            }
            lineNr = 1;
            writer.println("     |-----------------------------------");
        }
        writer.println("     | Rikiavimo duomenys|");
        writer.println("     |-----------------------------------");
        for (int i = 0; i < M; i++){
            writer.println(String.format("     |%-12s|%-8s|", "Rik. Laukas", "Kiekis"));
            writer.println("     |-----------------------------------");
            for (SortStruct get : DataM.get(i)) {
                writer.println(String.format("%3d) |%-12s|%-8s|", lineNr++, get.getSortField(), get.getCount()));
            }
            lineNr = 1;
            writer.println("     |-----------------------------------");
        }
    }
    
    public static void PrintResult(){
        int lineNr = 1;
        writer.println("*******************************************");
        writer.println("                REZULTATAI      ");
        writer.println("*******************************************");
        writer.println("     |Masyvas B            |");
        writer.println("     |-----------------------------------");
        writer.println(String.format("     |%-12s|%-8s|", "Rik. Laukas", "Kiekis"));
        writer.println("     |-----------------------------------");
        for (int i = 0; i < monitor.getCount(); i++) {
            int rikiuojamasLaukas = monitor.getB()[i].getSortField();
            int kiekis = monitor.getB()[i].getCount();
            writer.println(String.format("%3d) |%-12s|%-8s|", lineNr++, rikiuojamasLaukas, kiekis));
        }
    }
    
    private static void runProccesses(){
        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < N; i++) {
            es.execute(new Maker(i, DataN.get(i)));
        }

        for (int i = 0; i < M; i++) {
            es.execute(new Buyer(i, DataM.get(i)));
        }

        es.shutdown();
        try {
            es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        try{
            writer = new PrintWriter(resultFILE);
        } catch(IOException e){
            System.out.println(e);
            return;
        }
        Read(dataFILE);
        System.out.println("Data is read.");
        Print();
        System.out.println("Data is printed to file.");
        runProccesses();
        PrintResult();
        System.out.println("Results are appended to file.");
        writer.close();
        System.out.println("Program finished.");
    }
    
}
