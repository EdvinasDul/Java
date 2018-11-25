package iff.pkg6.pkg10.zdrukteinis_l3b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jcsp.lang.Alternative;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.ChannelInput;
import org.jcsp.lang.ChannelOutput;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.Parallel;

/**
 * Klasė skirtą rikiavimui pagal atlikėjo gymimo metus
 */
class NameComparator implements Comparator<Artist> {

        @Override
        public int compare(Artist a, Artist b) {
            return a.birthYear.compareToIgnoreCase(b.birthYear);
        }
    }

 /**
 * Atlikėjo klasė.
 **/
class Artist {
    public String name;
    public String birthYear;
    public int albumsSold;
    
    public Artist(String namePassed, String birthYearPassed, int albumsSoldPassed) {
        name = namePassed;
        birthYear = birthYearPassed;
        albumsSold = albumsSoldPassed;
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        return birthYear;
    }

    public int getAlbumsSold() {
        return albumsSold;
    }
}

/**
 * Atlikėjo klasė.
 **/
class ArtistSortableWrite{
    public int birthYear;
    public int counter;
    public String genre;
    
    public ArtistSortableWrite(int birthYearPassed, int counterPassed, String genrePassed) {
        birthYear = birthYearPassed;
        counter = counterPassed;   
        genre = genrePassed;
    }
}

//Rikiavimo struktūra
class ArtistTemp {
    public String  birthYear;
    public int counter;
    public String genre;
    
    public ArtistTemp(String birthYearPassed, int counterPassed) {
        birthYear = birthYearPassed;
        counter = counterPassed;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public int getCounter() {
        return counter;
    }
}

//Tam tikro žanro atlikėjų masyvas
class Genre {
    public List<Artist> artists;
    public String genre;
    public int artistsCount;
    
    public Genre() {
            this.genre = "";
            this.artistsCount = 0;
            this.artists = new ArrayList<>();
        }
    
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genrePassed) {
        genre = genrePassed;
    }
    
    public void setArtistAmount(int passedAmount){
        this.artistsCount = passedAmount;
    }

    public Artist getStudentas(int i) {
            return artists.get(i);
        }

        public void setArtist(int i, Artist s) {
            artists.add(i, s);
        }

        public void setArtist(Artist s) {
            artists.add(s);
        }
    public List<Artist> getArtists() {
            return artists;
        }
    
    public void setArtists(List<Artist> artists) {
            this.artists = artists;
        }
    
    public Artist getArtist(int i){
        return this.artists.get(i);
    }
    
    public void Sort() {
            artists.sort(new NameComparator());
        }
   
    
    public int getArtistCount(){
        return artistsCount;
    }
}

// Klasė, sauganti reikiamų rikiavimo laukų ir jų kiekių objektų masyvą
class GenreTemp {
    public List<ArtistTemp> artists;
    public String genre;
    public int artistAmount;

    public GenreTemp(List<ArtistTemp> artistsPassed, String genrePassed) {
        artists = artistsPassed;
        genre = genrePassed;

    }
    
    public int getArtistCount(){
       return artists.size();
    }
    
       public GenreTemp() {
            this.genre = "";
            this.artistAmount = 0;
            this.artists = new ArrayList<>();
        }
    
     public void setArtist(ArtistTemp s) {
            artists.add(s);
        }
    
    public void setArtistAmount(int amount){
        artistAmount = amount;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genreInput) {
        genre = genreInput;
    }

    public List<ArtistTemp> getArtistsSortable() {
        return artists;
    }

    public void setArtistsSortable(List<ArtistTemp>  artistsSortablePassed) {
        artists = artistsSortablePassed;
    }

    public ArtistTemp getArtist(int index) {
        return artists.get(index);
    }
}

public class IFF610ZDrukteinis_L3b {
    
    //bendras rašymo ir skaitymo kanalas
    static One2OneChannel BChan = Channel.one2one();
    
    //Rašytojo klasė
    static class Writer implements CSProcess {
        
        private final ChannelOutput out; //output channel
        private final Genre P;
        int number; //gijos nr

        Writer(ChannelOutput out, Genre p, int number) {
            this.out = out;
            this.P = p;
            this.number = number;
        }

        @Override
        public void run() {
            for (int i = 0; i < P.getArtistCount(); i++) {
                out.write(new ArtistTemp(P.getArtist(i).getBirthDate(), 1));
            }
            out.write(null);
        }
    }
    
    //Skaitytojo klasė
    static class Reader implements CSProcess {

        private final ChannelOutput out;
        /**
         * 1 - pasalintas is bendro masyvo 
         * 0 - nepasalintas is bendro masyvo (nera tokio objekto bendram masyve) 
         * -1 - write pricesai jau basibaige tai tikrinam trynima paskutini 
         * karta ir raukiam sitai
         */
        private final ChannelInput in;
        private List<ArtistTemp> P = new ArrayList<>();
        private final int number;

        /**
         *
         * @param out - išėjimo kanalas 
         * @param in - įėjimo kanalas
         * @param c - data
         * @param number - gijos nr
         */
        public Reader(ChannelOutput out, ChannelInput in, List<ArtistTemp> c, int number) {
            this.out = out;
            this.in = in;
            P = c;
            this.number = number;
        }

        @Override
        public void run() {
            int response; //feedback iš master klasės 
            response = 0;
            boolean proceed = true; //ar tęsti
            while (proceed && !P.isEmpty()) { //jei ne paskutine iteracija ir masyvas ne tuščias
                for (Iterator<ArtistTemp> iterator = P.iterator(); iterator.hasNext();) {
                    ArtistTemp x = iterator.next();
                    out.write(x);
                    response = (int) in.read();
                    if (response == 1) {
                        iterator.remove();
                    }                 
                    else if (response == -1) {
                        proceed = false;
                    }
                }
            }
            out.write(null);
        }
    }
    
    /**
    * Bendras rezultatų masyvas
    */
    static class MainArray {

        private final List<ArtistTemp> B;

        public MainArray() {
            this.B = new ArrayList<>();
        }

        public void remove(int i) {
            B.remove(i);
        }

        public boolean isEmpty() {
            return B.isEmpty();
        }

        /**
         * Metodas, skirtas grąžinti i-tąjį narį iš bendro masyvo
         *
         * @param i - indeksas masyve
         * @return
         */
        public ArtistTemp get(int i) {
            return B.get(i);
        }

        public void add(ArtistTemp a) {
            B.add(a);
        }

        public void add(int i, ArtistTemp a) {
            B.add(i, a);
        }

        public int size() {
            return B.size();
        }
    }
    
    /**
    * Procesų valdytojas
    */
    static class ProcessManager implements CSProcess {

        MainArray B = new MainArray();
        MainArray B_NotDeleted = new MainArray();
        private final One2OneChannel[] readerInput;
        private final One2OneChannel[] writerInput;
        private boolean[] preselect;
        private int finishedW = 0; //kiek writeriu baige darba
        private int finishedR = 0;
        private int deleteIterations = 0;
        private int maxDeleteIterations = 0;
        
         /**
         * 1 - pašalintas iš bendro masyvo 
         * 0 - nepašalintas iš bendro masyvo (nėra tokio objekto bendrame masyve) 
         * -1 - write pricesai jau pasibaigė tai tikrinae paskutinį trynimą 
         */
        private final ChannelOutput out;

        //Konstruktorius
        public ProcessManager(final One2OneChannel[] in0, final One2OneChannel[] in1, final ChannelOutput out, int maxDeleteIterations) {
            this.writerInput = in0;
            this.readerInput = in1;
            this.out = out;
            this.maxDeleteIterations = 0;
            boolean[] preselect = new boolean[GENRES_TOTAL + GENRES_TOTAL];
            for(int i = 0; i < preselect.length; i++){
                preselect[i] = true;
            }
            this.preselect = preselect;
            this.maxDeleteIterations = maxDeleteIterations;
        }

        
        //Paleidimas
        @Override
        public void run() {
            Alternative alternative = createGuards();
            while (finishedW != W_Count || finishedR != R_Count) {
                int alternativeChoice = alternative.fairSelect(preselect);
                preselect[alternativeChoice] = (finishedW != W_Count); //jei rasytojai nebaige - true
                preselect[alternativeChoice] = (!B.isEmpty() || finishedR != R_Count); //jei masyve yra elementu arba nebaige darbo visos gijos
                
                int write = 0;
                for(int i = 0; i < GENRES_TOTAL; i++){
                       if(alternativeChoice == i)
                        {
                             writer(write);
                             break;
                        }
                       write++;
                }
                int read = 0;
                for(int i = GENRES_TOTAL; i < GENRES_TOTAL*2; i++){      
                        
                        if(alternativeChoice == i)
                        {
                            reader(read);
                            break;
                        }
                        read++;
                  
                }
            }

            PrintArray();
            PrintResultsToFile();
        }
        
        /**
         * Pašalina reikšmes iš bendro masyvo.
         */
        private boolean removeValue(ArtistTemp val) {
            deleteIterations++;
            for (int i = 0; i < B.size(); i++) {
                if (B.get(i).birthYear.equalsIgnoreCase(val.birthYear)) {
                    B.get(i).counter--;
                    if (B.get(i).counter <= 0) {
                        B.remove(i);
                    }
                    return true;
                }
            }
            return false;
        }
        
        /**
         * S pausdina bendrą masyvą į konsolę.
         */
        private void PrintArray() {
            if (B.isEmpty()) {
                System.out.println("Bendras masyvas tuščias");
            } else {
                System.out.println("Bsendras masyvas:");
                System.out.println("Metai     kiekis");
                for (int i = 0; i < B.size(); i++) {
                    System.out.printf("%-2d) %-10s %-2d\n", i + 1, B.get(i).birthYear, B.get(i).counter);
                }
            }
        }
        
        /**
         * Rezultatų spausdinimas.
         */
         private void PrintResultsToFile() {            
            
            int lineNr = 1;
            resultWriter.println("\n     *******************************************");
            resultWriter.println("                     REZULTATAI      ");
            resultWriter.println("     *******************************************\n");
            resultWriter.println("     |Bendras Masyvas      |");
            resultWriter.println("     -----------------------");
            resultWriter.println(String.format("     |%-12s|%-8s|", "Rik. Laukas", "Kiekis"));
            resultWriter.println("     -----------------------");
            for (int i = 0; i < B.size(); i++) {
                String sortedField = B.get(i).birthYear;
                int counter = B.get(i).counter;
                resultWriter.println(String.format("%3d) |%-12s|%-8s|", lineNr++, sortedField, counter));
            }
            resultWriter.println("     -----------------------");
            
         }
        
        /**
         * Metodas, skirtas pridėti duomenims į bendrą masyvą.
         *
         * @param val
         * @return true, jei reikšmė pridėta į masyvą
         */
        public boolean addValue(ArtistTemp val) {
            
            for (ArtistTemp B1 : B.B) {
                if (B1.birthYear == null ? val.birthYear == null : B1.birthYear.equals(val.birthYear) ) {
                    B1.counter++;
                    return true;
                }
            }
            if (B.isEmpty()) {
                B.add(val);
            } else if (B.get(0).birthYear.compareToIgnoreCase(val.birthYear ) > 0) {
                B.add(0, val);
            } else if (B.get(B.size() - 1).birthYear.compareToIgnoreCase(val.birthYear ) < 0) {
                B.add(B.size(), val);
            } else {
                int i = 0;
                while (B.get(i).birthYear.compareToIgnoreCase(val.birthYear ) < 0) {
                    i++;
                }
                B.add(i, val);
            }
            return true;
        }
        
        /**
         * Rašytojas
         */
        private void writer(int alternativeChoice) {
            ArtistTemp data;
            data = (ArtistTemp) writerInput[alternativeChoice].in().read();
            
            if (data == null) {
                finishedW++;
            } else {
                addValue(data);
            }
        }

        /**
         * Skaitytojas
         */
        private void reader(int alternativeChoice) {
            ArtistTemp x = (ArtistTemp) readerInput[alternativeChoice].in().read();
           
            if (x != null) {

                    if (removeValue(x)) { //jei sekmingai pasalintas
                        
                        out.write((int) 1); //1 - pasalintas 
                    } else if (finishedW == W_Count && deleteIterations > maxDeleteIterations) {

                        out.write((int) -1); //jei jau 
                    } else {

                        out.write((int) 0); //siuncia i kanala 0 - neistrintas
                    }
                
            } else {
                finishedR++; //jei ateina null tai tas jau baige savo darba
            }
        }
        
        /**
        * Guards sukurimas rašytojui ir skaitytojui
        */
        private Alternative createGuards() {
            Guard guards[] = new Guard[GENRES_TOTAL + GENRES_TOTAL];
            int id = 0;
            for(int i =0 ; i < GENRES_TOTAL; i++){
                guards[id++] = this.writerInput[i].in();
            }
            for(int i =0 ; i < GENRES_TOTAL; i++){
                guards[id++] = this.readerInput[i].in();
            }

            Alternative alternative = new Alternative(guards);
            return alternative;
        }
    }
    
    
    /*
    *   Atlikėjų duomenų nuskaitymas
    */
    private static void ReadData(BufferedReader br, Genre P) throws IOException {
        try {
            String line = br.readLine();
            String genre, artistName, artistBirthYear;
            int artistAmount, artistAlbumsSold;
            Scanner ed;
            ed = new Scanner(line);
            genre = ed.next();
            ed = new Scanner(br.readLine());
            artistAmount = ed.nextInt();
            P.setGenre(genre);
            P.setArtistAmount(artistAmount);
            for (int i = 0; i < artistAmount; i++) {
                ed = new Scanner(br.readLine());
                artistName = ed.next();
                artistBirthYear = ed.next();
                artistAlbumsSold = ed.nextInt();
                Artist s = new Artist(artistName, artistBirthYear, artistAlbumsSold);
                P.setArtist(s);
            }
            P.Sort();

        } catch (NullPointerException e) {
            System.out.print("NullPointerException caught");
            System.exit(1);
        }
    }
    
    /*
    *   Rikiuojamos struktūros duomenų nuskaitymas
    */
    private static void ReadDataTemp(BufferedReader br, GenreTemp P_Temp) throws IOException {
        try {
            String line = br.readLine();
            String genre, artistBirthYear;
            int artistAmount, counter;
            Scanner ed;
            ed = new Scanner(line);
            genre = ed.next();
            ed = new Scanner(br.readLine());
            artistAmount = ed.nextInt();
            P_Temp.setGenre(genre);
            P_Temp.setArtistAmount(artistAmount);
            for (int i = 0; i < artistAmount; i++) {
                ed = new Scanner(br.readLine());
                artistBirthYear = ed.next();
                counter = ed.nextInt();
                ArtistTemp a = new ArtistTemp(artistBirthYear, counter);
                P_Temp.setArtist(a);
            }
            

        } catch (NullPointerException e) {
            System.out.print("NullPointerException caught");
            System.exit(1);
        }
    }
    
      /**
     * Lentelės pavidalu spausdina pradinius duomenis į konsolę.
     *
     * @param P
     */
    private static void WriteTable(Genre P) {
        System.out.println("****" + P.genre + "***");
        System.out.println("   Vardas   Kursas  Vidurkis");
        for (int i = 0; i < P.getArtistCount(); i++) {
            Artist A = P.getArtist(i);
            System.out.printf("%d) %-10s %-5s %-3.2s \n", i + 1, A.getName(), A.getBirthDate(), A.getAlbumsSold());
        }
    }
    
    /**
     * Lentelės pavidalu spausdina pradinius duomenis į failą.
     *
     * @param P
     */
    private static void WriteTableToFile(Genre P) {
        int lineNr = 1;
        
            resultWriter.println(String.format("     |%-15s|%-8s|%-16s|", "Vardas", "Metai", "Parduoti albumai"));
            resultWriter.println("     -------------------------------------------");
            resultWriter.println(String.format("     |%-12s", P.genre));
            for (int x = 0; x < P.getArtistCount(); x++) {
                Artist A = P.getArtist(x);
                resultWriter.println(String.format("%3d) |%-15s|%-8s|%-16s|", lineNr++, A.getName(), A.getBirthDate(), A.getAlbumsSold()));
            }
            lineNr = 1;
            resultWriter.println("     -------------------------------------------");
        
        
    }
    
     /**
     * Lentelės pavidalu spausdina pradinius duomenis (rikiavimo struktūros) į failą.
     *
     * @param P
     */
    private static void WriteTableToFileTemp(GenreTemp P) {
        int lineNr = 1;
        
            resultWriter.println(String.format("     |%-15s|%-8s|", "Field", "Count"));
            resultWriter.println("     -------------------------------------------");
            resultWriter.println(String.format("     |%-12s", P.genre));
            for (int x = 0; x < P.getArtistCount(); x++) {
                ArtistTemp A = P.getArtist(x);
                resultWriter.println(String.format("%3d) |%-15s|%-8s|", lineNr++, A.getBirthYear(), A.getCounter()));
            }
            lineNr = 1;
            resultWriter.println("     -------------------------------------------");
        
        
    }
    
    public static final String PATH_TO_DATA = "src\\Data\\";
    public static final String PATH_TO_RESULT = "src\\Result\\";
    
    private static final String C_FName = PATH_TO_DATA + "IFF-6-10_ŽDrukteinis_L3b_dat_3.txt";
    public static final String RESULT_FILE = PATH_TO_RESULT + "IFF-6-10_ŽDrukteinis_L3a_rez.txt";
    
    public static PrintWriter resultWriter;
    
    
    public static int W_Count =5; // Rašytojų procesų skaičius.
    public static int R_Count = 5;  // Skaitytojų procesų skaičius.
    public static int GENRES_TOTAL = 5; // Grupių skaičius/
    public static int GENRES_TEMP_TOTAL = 5; // Rikiavimo struktūros grupių skaičius.
    
    public static One2OneChannel[] one2OneChannelW = Channel.one2oneArray(GENRES_TOTAL);
    public static One2OneChannel[] one2OneChannelR = Channel.one2oneArray(GENRES_TOTAL);

    
    public static void main(String[] args) throws FileNotFoundException, IOException {

        //rezultatų spausdinimui skirtas objektas.
        resultWriter = new PrintWriter(RESULT_FILE);
           
        Genre[] P = new Genre[GENRES_TOTAL];
        GenreTemp[] P_Temp = new GenreTemp[GENRES_TEMP_TOTAL];
        
        //Sukuriamos duomenų struktūros.
        for (int i = 0; i < GENRES_TOTAL; i++) {
            P[i] = new Genre();
        }   
        
        //Sukuriamos rikiuojamos duomenų struktūros.
        for (int i = 0; i < GENRES_TEMP_TOTAL; i++) {
            P_Temp[i] = new GenreTemp();
        }   

        BufferedReader br = new BufferedReader(new FileReader(C_FName));
       
        //Nuskaitomi pradiniai duomenys.
        for (int i = 0; i < GENRES_TOTAL; i++) {
            ReadData(br, P[i]);
            
        }
        
        //Nuskaitomi pradiniai duomenys.
        for (int i = 0; i < GENRES_TEMP_TOTAL; i++) {
            ReadDataTemp(br, P_Temp[i]);
            
        }
        resultWriter.println("     |Atlikėjų duomenų rinkiniai|");
        resultWriter.println("     -------------------------------------------");
        //Įrašomi pradiniai duomenys į failą.
        for (int i = 0; i < GENRES_TOTAL; i++) {
            WriteTableToFile(P[i]);
        }
        
        resultWriter.println("     |Rikiuojami duomenų rinkiniai|");
        resultWriter.println("     -------------------------------------------");
        //Įrašomi pradiniai duomenys į failą.
        for (int i = 0; i < GENRES_TEMP_TOTAL; i++) {
            WriteTableToFileTemp(P_Temp[i]);
        }
        try {
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(IFF610ZDrukteinis_L3b.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("-------------------------------");
        
        //Rikiuojamų duomenų struktūros.
        List<ArtistTemp> T1 = P_Temp[0].artists;
        List<ArtistTemp> T2 = P_Temp[1].artists;
        List<ArtistTemp> T3 = P_Temp[2].artists;
        List<ArtistTemp> T4 = P_Temp[3].artists;
        List<ArtistTemp> T5 = P_Temp[4].artists;
                     
        int maxDeleteItarations = 0;
      
        //Lygiagrečiai paleidžiami procesai
        new Parallel(
                new CSProcess[]{
                    new Writer(one2OneChannelW[0].out(), P[0], 0),
                    new Writer(one2OneChannelW[1].out(), P[1], 1),
                    new Writer(one2OneChannelW[2].out(), P[2], 2),
                    new Writer(one2OneChannelW[3].out(), P[3], 3),
                    new Writer(one2OneChannelW[4].out(), P[4], 4),
                    new Reader(one2OneChannelR[0].out(), BChan.in(), T1, 0),
                    new Reader(one2OneChannelR[1].out(), BChan.in(), T2, 1),
                    new Reader(one2OneChannelR[2].out(), BChan.in(), T3, 2),
                    new Reader(one2OneChannelR[3].out(), BChan.in(), T4, 4),
                    new Reader(one2OneChannelR[4].out(), BChan.in(), T5, 5),
                    
                    new ProcessManager(one2OneChannelW, one2OneChannelR, BChan.out(),maxDeleteItarations)}
        ).run();
        resultWriter.close();
        
    }
    
}