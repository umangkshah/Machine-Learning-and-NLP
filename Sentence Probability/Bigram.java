import java.util.*;
import java.io.*;

public class Bigram{

  static PrintWriter wout;
  
  public static void main(String arg[])throws Exception{
    //Read Corpus File
    wout = new PrintWriter(new File("output_uks.txt"));//Output file
    Scanner sc = new Scanner(new File("Corpus.txt"));//Input file corpus
    //Sentences to test
    String sent1 = "The chief executive said that the company’s profit was going down last year";
    String sent2 = "The president said the revenue was good last year";
    String corpus=new String();
    //Corpus Preprocessing
    while(sc.hasNextLine()){
    corpus = corpus +" "+ sc.nextLine();
    }
    corpus = corpus.toLowerCase();
    corpus = corpus.replaceAll("\\s\\,","");//remove commas
    //corpus = corpus.replaceAll("\\s\\'","\\'");//remove commas
    String sentence[] = corpus.split("\\s\\.\\s");//Split to sentences
    ArrayList <String []> lines = new ArrayList<String[]>();
    for(int i=0;i < sentence.length;i++){
      sentence[i] = sentence[i].replaceAll("\\'\\'\\s","");
      //sentence[i] = sentence[i].replaceAll("\\`\\`\\s","");
      if(sentence[i].length() != 0){
        String tmp[] = sentence[i].split(" ");//split sentence to words
        lines.add(tmp);
      }
    }
    //Build Vocabulary with Count
    HashMap<String, Integer> vocab = new HashMap<>();
    for(int i=0;i < lines.size();i++){
      String tmp[] = lines.get(i);
      for(int j=0;j < tmp.length;j++){
        if(vocab.containsKey(tmp[j]))
          vocab.put(tmp[j],vocab.get(tmp[j])+1);
        else
          vocab.put(tmp[j],1);
        }
    }

    //Bigram Counts
    HashMap<String, HashMap<String, Integer>> bigrams = new HashMap<>();

    //Do Counts
    for(String a[]:lines){
      for(int j=1;j < a.length-1;j++){
        if(!bigrams.containsKey(a[j-1]))
          bigrams.put(a[j-1], new HashMap<String, Integer>());
        else{
          HashMap<String, Integer> tmp = bigrams.get(a[j-1]);
          if(!tmp.containsKey(a[j]))
            tmp.put(a[j],1);
          else
            tmp.put(a[j],tmp.get(a[j])+1);
          bigrams.put(a[j-1],tmp);
        }
      }
    }  
    //total no of tokens
    int N=0;
    for(Map.Entry m:vocab.entrySet()){
      int k = (int) m.getValue();
      N += k;
    }
    
    System.out.println("Sentence1- " + sent1 + "\nSentence2- " + sent2);
    wout.println("Sentence1- " + sent1 + "\nSentence2- " + sent2);
    
    sent1 = sent1.toLowerCase();
    sent2 = sent2.toLowerCase();
    ArrayList <String> tokens1 = makeTokens(sent1);
    ArrayList <String> tokens2 = makeTokens(sent2);  
    /*    */
    System.out.println("\nNo Smoothing-- >");
    wout.println("\nNo Smoothing-- >\n----------------------------------------------");
    wout.println("Sentence 1:");
    double prob1 = makeTable(bigrams,vocab,sent1,tokens1);
    wout.println("\nSentence 2:");
    double prob2 = makeTable(bigrams,vocab,sent2,tokens2);
    System.out.println("Sentence 1:" + prob1 + "  Sentence 2:" + prob2);
    System.out.println("Both Sentences have zero probability");
    /*    */
    System.out.println("\nAdd one Smoothing-- >");
    wout.println("\nAdd One Smoothing-- >\n-----------------------------------------");
    wout.println("Sentence 1:");
    prob1 = makeSmoothTable(bigrams,vocab,sent1,tokens1,N);
    wout.println("\nSentence 2:");
    prob2 = makeSmoothTable(bigrams,vocab,sent2,tokens2,N);
    System.out.println("Sentence 1:" + prob1 + "  Sentence 2:" + prob2);
    int ans = (prob1 > prob2) ? 1 : 2;
    System.out.println("Sentence " + ans + " is more probable");
    /*    */
    System.out.println("\nGood Turing Discount-- >");
    wout.println("\nGood Turing Discount-- >\n---------------------------------------");
    wout.println("Sentence 1:");
    prob1 = goodTuringTable(bigrams,vocab,sent1,tokens1,N);
    wout.println("\nSentence 2:");
    prob2 = goodTuringTable(bigrams,vocab,sent2,tokens2,N);
    System.out.println("Sentence 1:" + prob1 + "  Sentence 2:" + prob2);
    ans = (prob1 > prob2) ? 1 : 2;
    System.out.println("Sentence " + ans + " is more probable");
    
    System.out.println("\nTables are printed to the file - output_uks.txt");
   // System.out.println(corpus);
    wout.close();
  }
  /* Function Definitions */
  
  static ArrayList <String> makeTokens(String s){
    String words[] = s.split(" ");
    ArrayList <String> a = new ArrayList <String>();
    for(String x: words){
      if(!a.contains(x))
        a.add(x);
    }
    return a;
  }
  
  static double makeTable(HashMap<String, HashMap<String, Integer>> bigrams, HashMap<String, Integer> vocab,String s,ArrayList <String> wlist){
    HashMap<String, HashMap<String, Double>> sent_bigrams = new HashMap<>();
    for(String x:wlist){
      sent_bigrams.put(x, new HashMap<String, Double>());
      for(String y:wlist){
        if(bigrams.get(x) == null || bigrams.get(x).get(y) == null)
          sent_bigrams.get(x).put(y,0.0d);
        else
          sent_bigrams.get(x).put(y,(double)bigrams.get(x).get(y));
      }
    }    
    wout.println("\nBigram Counts:");
    printTable(sent_bigrams);
    for(String x:wlist){
      for(String y:wlist){
        double prob = 0.0d;
        if(vocab.containsKey(x))
          prob = sent_bigrams.get(x).get(y)/vocab.get(x);
        sent_bigrams.get(x).put(y,prob);
      }
    } 
    wout.println("\nBigram Probabilities:");
    printTable(sent_bigrams);
    String ws[] = s.split(" ");
    double sprob = 1.0d;
    for(int i=1;i < ws.length;i++){
      sprob = sprob * sent_bigrams.get(ws[i-1]).get(ws[i]);
    }
    return sprob;   
  }
  
  static double makeSmoothTable(HashMap<String, HashMap<String, Integer>> bigrams, HashMap<String, Integer> vocab,String s,ArrayList <String> wlist,int n){
    HashMap<String, HashMap<String, Double>> sent_bigrams = new HashMap<>();
    for(String x:wlist){
      sent_bigrams.put(x, new HashMap<String, Double>());
      for(String y:wlist){
        if(bigrams.get(x) == null || bigrams.get(x).get(y) == null){
          double c = 1.0d;
          sent_bigrams.get(x).put(y,c);
          }
        else{
          double c = (double)bigrams.get(x).get(y);
          c++;
          sent_bigrams.get(x).put(y,c);
        }
      }
    }    
    wout.println("\nBigram Counts:");
    printTable(sent_bigrams);
    for(String x:wlist){
      for(String y:wlist){
        double prob = 0.0d;
        int count = vocab.size();
        if(vocab.containsKey(x))
          count = count + vocab.get(x);
        prob = (sent_bigrams.get(x).get(y))/count;
        sent_bigrams.get(x).put(y,prob);
      }
    } 
    wout.println("\nBigram Probabilities:");
    printTable(sent_bigrams);
    String ws[] = s.split(" ");
    double sprob = 1.0d;
    for(int i=1;i < ws.length;i++){
      sprob = sprob * sent_bigrams.get(ws[i-1]).get(ws[i]);
    }
    return sprob;   
  }
  
  static double goodTuringTable(HashMap<String, HashMap<String, Integer>> bigrams, HashMap<String, Integer> vocab,String s,ArrayList <String> wlist,int n){
    HashMap<String, HashMap<String, Double>> sent_bigrams = new HashMap<>();
    int max = 0;
    for(String x:wlist){
      sent_bigrams.put(x, new HashMap<String, Double>());
      for(String y:wlist){
        if(bigrams.get(x) == null || bigrams.get(x).get(y) == null)
          sent_bigrams.get(x).put(y,0.0d);
        else{
          int num = bigrams.get(x).get(y);
          max = (num > max ? num : max);
          sent_bigrams.get(x).put(y,(double)num);
        }
      }
    }    
    double arr[] = new double[max+2];
    for(int i=0;i < arr.length;i++){
      arr[i] = 1.0d;
    }
    for(String x:wlist){
      for(String y:wlist){
        int num = sent_bigrams.get(x).get(y).intValue();
        arr[num]++;
      }
    }
    for(String x:wlist){
      for(String y:wlist){
        int num = sent_bigrams.get(x).get(y).intValue();
        double cstar = 1.0d;
        if(num!=0){
          cstar = num+1;
          cstar = cstar * (arr[num+1]/arr[num]); //formula;
          sent_bigrams.get(x).put(y,cstar);
        }
       }
    }
    wout.println("\nBigram Counts:");
    printTable(sent_bigrams);
    for(String x:wlist){
      for(String y:wlist){
        int num = sent_bigrams.get(x).get(y).intValue();
        double pgt = 1.0d;
        if(num == 0){
          pgt = arr[1] / n;        
        }
        else{
          pgt = sent_bigrams.get(x).get(y) / n;
        }
        sent_bigrams.get(x).put(y,pgt);
       }
    }
    wout.println("\nBigram Probability:");
    printTable(sent_bigrams);
    String ws[] = s.split(" ");
    double sprob = 1.0d;
    for(int i=1;i < ws.length;i++){
      sprob = sprob * sent_bigrams.get(ws[i-1]).get(ws[i]);
    }
    return sprob;
  }
  
  static void printTable(HashMap<String, HashMap<String, Double>> table){    
    String sp = " ";
    wout.printf("\n%-10s|",sp);
    for(Map.Entry m:table.entrySet()){          
      wout.printf("%10s\t|",m.getKey().toString());
    }
    wout.println();
    for(Map.Entry m:table.entrySet()){
      String w = m.getKey().toString();
      wout.printf("%10s|\t",w);
      for(Map.Entry n:table.entrySet()){
        String w2 = n.getKey().toString();
        double no = table.get(m.getKey().toString()).get(n.getKey().toString());
        if(no == 0.0)
        { int nuum = 0;
          wout.printf("%10d|\t",nuum);
        }
        else
        wout.printf("%10.6f|\t",no);
      }
      wout.println();
    }
  }
}


