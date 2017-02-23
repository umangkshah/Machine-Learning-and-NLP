import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;
import java.net.*;

public class MakeURLHashtag {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String wrd = "";
        
        File f = new File("words.txt");
        try{
            //read list of words
        Scanner filerd = new Scanner(f);
            List<String> words = new ArrayList<String>();
        while(filerd.hasNext()){
            wrd = filerd.next();
            words.add(wrd);
        }
        
        //test cases    
        int t;
            int start=0,end=0,num=0;
        t = sc.nextInt();
        
            
        //for all cases
        while(t>0){
            
            //init
            String outw[] = new String[100];
           
            //read input string & lowercase
            String txt = sc.next();
                   
            txt = txt.toLowerCase();
            
            
            //strip # and url
            if(txt.charAt(0) == '#'){
                start = 1;
                end = txt.length();
                
            }
            else if(txt.indexOf("www.") != -1){
                start = 4;
                end = txt.lastIndexOf(".");
                 //System.out.print(start + "  " + end);
            }
            else if(! Character.isDigit(txt.lastIndexOf(".")+1)){
                start =0;
                end = txt.lastIndexOf(".");
            }
            txt = txt.substring(start,end);
            start = 0;
            end = txt.length();
           //System.out.println(txt.substring(end-1,end));
            
            
            //find and store words in tmp
            String tmp ="";
            int none = 0;
            int over = 0;
            int j=start;
            int tmpn = end;
            int track = 0;
            int incr = 0;
            //main
            while(over == 0 && none == 0){
                
                if(Character.isDigit(txt.charAt(j))){                    
                        while(Character.isDigit(txt.charAt(j)) || txt.charAt(j) == '.'){
                            tmp = tmp + txt.charAt(j);
                            j++;
                            if(j==end)break;
                            }

                       
                        outw[track] = tmp;
                        tmp = "";
                        num=1;
                        incr = 1;
                        track++;
                    }
                
                else{
                    incr = 0;
                    if(j < end-1 && tmpn > j){
                        
                        //System.out.print(" j= " + j + " tmpn= " + tmpn);
                       // System.out.println("track= " + track);
                        
                        tmp = txt.substring(j,tmpn);
                        //System.out.print(tmp+"   ");
                            if(words.contains(tmp) || tmp.equals("i")){
                                num=0;
                                outw[track]=tmp;
                                track++; 
                                j=j+tmp.length();
                                tmp="";
                                tmpn = end+1;
                            }
                    }
            }
                    
                
                    //if((end-outw[track-1].length+1) == txt.indexOf(outw[track-1]))
                        if(j == end)over = 1;

                    else if(tmpn < j){
                        //System.out.print(" HERE ");
                        if(num == 1)
                            none = 1;
                        else if(track == 0)
                            none = 1;
                        else{
                        track--;
                        j = j - outw[track].length();
                        tmpn++;
                        }
                    }
                    if(incr != 1){
                       if(tmpn != 0)
                        tmpn--;
                        else
                        tmpn = -1;
                    }
            }
            
            if(none == 1)
                System.out.print(txt);
            else{
            for(int i=0;i<track-1;i++)
            System.out.print(outw[i] + " ");
            System.out.print(outw[track-1]); 
            
               
        }
            System.out.println();
             t--;
        }
        }
        catch(FileNotFoundException ee) {System.out.println("fail");          }
    }
}
           
           
