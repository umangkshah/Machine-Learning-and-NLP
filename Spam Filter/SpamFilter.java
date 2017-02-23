import java.util.*;
import java.io.*;

public class SpamFilter{
  
  public static void main(String args[]){
    //get training files
    int stop = 0;
    int filter = 0;
    double n=0.01, L=1.8;
    int I=57;
    if(args.length > 0){
      if(args[0].equals("yes"))
        stop = 1;
    }
    if(args.length > 3){
      n = Double.parseDouble(args[1]);
      L = Double.parseDouble(args[2]);
      I = Integer.parseInt(args[3]);
    }
    if(stop == 0)
      System.out.println("Not Removing Stop Words");
    else
      System.out.println("Removing Stop Words");
/*    if(filter == 0)
      System.out.println("No Additional Filtering");
    else
      System.out.println("With Additional Filtering");*/
    File folder1 = new File("./train");
    File folder2 = new File("./test");
    
    //Naive Bayes
    NaiveBayesFilter nb = new NaiveBayesFilter(stop,filter);
    nb.count(folder1);
    nb.doCondProb();
    double accuracy = nb.test(folder2);
    System.out.println("Naive Bayes Accuracy=  " + accuracy);
    
    //Logistic Regression
    LogisRegr lg = new LogisRegr(n,L,I,stop,0);//eta, lambda, noOfIter, stopWords, addFiltering
    lg.train(folder1);
    double acc2 = lg.test(folder2);
    System.out.println("Log Reg Accuracy=  " + acc2);
    
  }

}

class Helper{
  
  ArrayList<String> st_w;
  
  Helper(){
    try{
        st_w = new ArrayList<>();
        Scanner sc = new Scanner(new File("stop.txt"));
        while(sc.hasNextLine()){
          st_w.add(sc.nextLine());
        }
      }catch(Exception e){}
  }
  
  String purify(String s){
    for(String word:st_w){
      s = s.replaceAll(word,"");
    }
    return s;
  }
 
}

class NaiveBayesFilter{
  ArrayList< HashMap<String, Double> > prob;
  HashMap<String, Double> vclass;
  HashMap<String, Integer> vocab;
  double prior[];
  int sw;
  Helper h;
  int fil;
  
  NaiveBayesFilter(int sw,int fil){
    prob = new ArrayList< HashMap<String, Double> >();
    prior = new double[2];
    h = new Helper();
    this.sw = sw;
    this.fil = fil;
  }
  
  HashMap<String, Integer> getVocab(){
    return vocab;
  }
  
  void count(File folder){
    vocab = new HashMap<>();  
    //for each class
    File categ = new File(folder + "/ham/");
    double y = makeVocab(categ, 0);    
    categ = new File(folder + "/spam/");
    double n = makeVocab(categ, 1);
    // total docs
    // N c /N
    prior[0] = n/(y+n);
    prior[1] = y/(y+n); 
  }
  
  double makeVocab(File folder, int o){
    File email[] = folder.listFiles();
    //DOCS In class
    double total = email.length;
    //COUNT TOKENS OF TERM
    vclass = new HashMap<>();
    for(int i=0;i < email.length;i++)
      calculate(email[i],o);
    prob.add(o,vclass);
    return total;
  }
  
  void calculate(File f,int o){  
    Scanner sc = null;
    try{
      sc = new Scanner(f);
    }catch(Exception e){}
    String corpus=new String();
    HashMap<String, Double> v = new HashMap<>();
    while(sc.hasNextLine()){
      corpus = sc.nextLine();
      if(sw == 1)
        corpus = h.purify(corpus);
      String a[] = corpus.split(" ");
      for(String x:a){
        //EXTRACT VOCABULARY(D)
        if(vocab.containsKey(x))
          vocab.put(x,vocab.get(x)+1);
        else
          vocab.put(x,1);
        //count in a class  
        if(vclass.containsKey(x))
          vclass.put(x,vclass.get(x)+1.0);
        else
          vclass.put(x,1.0);
        
      }
    }
  }
  
  //do condprob
  void doCondProb(){
    double V[] = new double[2];
    for(int i=0;i < 2;i++){
      vclass = prob.get(i);
      for(Map.Entry m:vocab.entrySet()){
        if(vclass.containsKey(m.getKey().toString()))
          V[i] = V[i] + vclass.get(m.getKey().toString()) + 1;
        else
          V[i]++;
      }
    }
    if(fil == 1){
     V[0] += 10;
     V[1] += 90;
    }
    for(int i=0;i < 2;i++){
      for(Map.Entry m:vocab.entrySet()){
        String word = m.getKey().toString();
        double conprob = 0.0;
        if(prob.get(i).containsKey(word))
          conprob = prob.get(i).get(word);         
        if(fil == 1 && word.equals("http")){
          int add = (i==0?10:90);
          conprob += add;
        }
        conprob++;
        conprob = conprob/V[i];
        prob.get(i).put(word,conprob);
      }
    }
  }
  
  double test(File folder){
    File categ = new File(folder + "/ham");
    String y = classify(categ,0);    
    String res[] =  y.split(":");
    categ = new File(folder + "/spam");
    String n = classify(categ,1);
    String res2[] =  n.split(":");
    double correct = Integer.parseInt(res[0]) + Integer.parseInt(res2[0]);
    double total = Integer.parseInt(res[1]) + Integer.parseInt(res2[1]);
    double acc =  correct/total;
    return acc;
  }

  String classify(File folder,int c){
    
    String ans = new String();
    File email[] = folder.listFiles();
    Scanner sc = null;
    int correct=0;
    int total = email.length;
    for(int i=0;i < email.length;i++){
      double classprob[] = new double[2];
      for(int j=0;j < 2;j++)
        classprob[j] = classprob[j] + (double) Math.log(prior[j]);
      try{
      sc = new Scanner(email[i]);
      }catch(Exception e){}
      while(sc.hasNextLine()){
        String line = sc.nextLine();
        if(sw == 1)
          line = h.purify(line);
        String words[] = line.split(" ");
        for(String x:words){
          for(int j=0;j<2;j++){
            double p = 0.0;
            if(prob.get(j).containsKey(x))
              p = Math.log(prob.get(j).get(x));
            classprob[j] = classprob[j] + p;
          }
        }
      }
      int max = ((classprob[0] > classprob[1]) ? 0 : 1);
      if(max == c)
        correct++;
    }
  ans = correct + ":"+total;
  return ans;
  }
}

class LogisRegr{
  //count of word in each email
  ArrayList< ArrayList<HashMap<String, Double>>> X;
  HashMap<String, Double> W;
  HashMap<String, Integer> vocab;
  //Weight is for words
  double n;
  double Ld;
  int I;
  int sw;
  Helper h;
  int tested;
  int fil;
  
  LogisRegr(double n, double Ld, int I,int sw,int fil){
    this.n = n;
    this.Ld = Ld;
    this.I = I;
    X = new ArrayList<ArrayList< HashMap<String, Double>>>();
    vocab = new HashMap<>();
    this.sw = sw;
    h = new Helper();
    tested = 0;
    this.fil = fil;
    System.out.println("eta: " + n + " lambda: " + Ld + " Iterarions: " + I);
  }
  
  void train(File folder){
    File categ1 = new File(folder + "/ham/"); 
    File categ2 = new File(folder + "/spam/");
    doCounts(categ1,0);
    doCounts(categ2,1);
    learnWeights();
  }
  
  void doCounts(File f,int y){
    File email[] = f.listFiles();
    X.add(y,new ArrayList<HashMap<String, Double>>());
    for(int l=0;l < email.length;l++)
      calcX(email[l],l,y);
    vocab.put("BIAS",1);
    W = new HashMap<String, Double>();
    for(Map.Entry m:vocab.entrySet()){
      W.put(m.getKey().toString(), 0.0);
    }
    W.put("BIAS",0.0);
  }
  
  void calcX(File email,int l,int y){
    Scanner sc=null;
    try{
      sc = new Scanner(email);
    }catch(Exception e){}    
    HashMap<String,Double> xl = new HashMap<String, Double>();
    while(sc.hasNextLine()){
      String line = sc.nextLine();
      if(sw == 1)
        line = h.purify(line);
      String words[] = line.split(" ");
      for(String ft:words){
        if(xl.containsKey(ft))
          xl.put(ft,xl.get(ft)+1);
        else
          xl.put(ft,1.0);
        if(y <= 1){
          if(vocab.containsKey(ft))
            vocab.put(ft,vocab.get(ft)+1);
          else
            vocab.put(ft,1);
        }
      }
    }
    xl.put("BIAS",1.0);
    X.get(y).add(l,xl);
  }
  
  void learnWeights(){
    double best = Double.NEGATIVE_INFINITY;
    File f = new File("./test");
    for(int k=0;k <= I;k++){  
      HashMap<String, Double> W2 = new HashMap<String, Double>();
      for(Map.Entry m:vocab.entrySet()){
          String i = m.getKey().toString();
          double w = W.get(i);
          //if(k==5) System.out.println("weight["+i+"]: " + w);
          double smn = sumn(i);      
          if(i.equals("http") && fil == 1){  
              w = W.get(i) + (n);
          }
          else
            w = W.get(i) + (n * smn) - (n*Ld*W.get(i));// Wi = Wi + n*SUM(Xil(Yl - P(Yl|Xl,W)))
          W2.put(i,w);
      }
     W = W2;
     /*
     double acc = test(f);
     if(k > 2 && acc >= best && acc != 0.7280334728033473){
      System.out.println("Iter " + k + " : " + acc);
      best = acc;
     }*/
    }
    tested = 0;
  }
  
  double sumn(String i){//SUM(Xil(Yl - P(Yl|Xl,W)))
    double ans = 0.0;
    for(int y = 0;y < 2;y++){//All emails
      ArrayList<HashMap<String, Double>> a = X.get(y);
      for(int l=0;l < a.size();l++){
        if(a.get(l).containsKey(i))
          ans = ans + ((a.get(l).get(i)) * (y -  P(l,y)));  //Xil(Yl - P(Yl|Xl,W))        
      }
    }
    
    return ans;
  }
  
  double P(int l,int y){//P(Yl|Xl,W)
    double sumWX = sumwx(l,y);
    if(Math.exp(sumWX) >= Double.POSITIVE_INFINITY) 
      return 1.0;
    else if(Math.exp(sumWX) >= Double.NEGATIVE_INFINITY)
      return 0.0;
    else{
      double b = Math.exp(sumWX)/(1 + Math.exp(sumWX));
      return b;
    }
  }
  
  double sumwx(int l,int y){  
    double ans = 0.0;
    try{
    HashMap<String, Double> Xi = X.get(y).get(l);
    for(Map.Entry m:Xi.entrySet()){
      String wd = m.getKey().toString();
      ans = ans + (W.get(wd) * Xi.get(wd));
    }
    }catch(Exception e){System.out.println(y);}
    return ans;
  } 
  
  
  double test(File folder){
    File categ1 = new File(folder + "/ham");
    File categ2 = new File(folder + "/spam");
    String y = classify(categ1,0);    
    String res[] =  y.split(":");
    String n = classify(categ2,1);
    String res2[] =  n.split(":");
    double correct = Integer.parseInt(res[0]) + Integer.parseInt(res2[0]);
    double total = Integer.parseInt(res[1]) + Integer.parseInt(res2[1]);
    double acc =  correct/total;
    return acc;
  }
  
  String classify(File folder, int y){
    String ans = new String();
    File email[] = folder.listFiles();
    Scanner sc = null;
    int correct=0;
    int total = email.length;
    if(tested == 0)
      X.add(y+2,new ArrayList<HashMap<String, Double>>());
    for(int i=0;i < email.length;i++){
      if(tested == 0)
        calcX(email[i],i,y+2);
      HashMap <String, Double> xi = X.get(y+2).get(i);
      try{
      sc = new Scanner(email[i]);
      }catch(Exception e){}
      double sum = 0.0;
      for(Map.Entry m:xi.entrySet()){ 
        String x = m.getKey().toString();
        if(W.containsKey(x))
          sum = sum + (W.get(x)*xi.get(x));
      }
      int c = 1;
      if(sum > 0)
        c = 0;
      if(c == y) correct++;
    }
    String op = correct+":"+total;
    if(y == 1) tested = 1;
    return (op);
  }

}
