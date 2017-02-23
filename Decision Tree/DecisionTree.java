import java.util.*;
import java.io.*;

public class DecisionTree{
ArrayList<ArrayList<Integer>> traindata;
ArrayList<ArrayList<Integer>> valdata;
ArrayList<ArrayList<Integer>> testdata;
ArrayList<String> attrs = new ArrayList<String>();
int used[];

  public static void main(String args[]){
    if(args.length < 6) System.out.println("Not enough arguments");
    else{
    //Process Arguments
      DecisionTree D = new DecisionTree();
      int L = Integer.parseInt(args[0]);
      int K = Integer.parseInt(args[1]);
      String trainfile, testfile, valfile;
      trainfile = args[2];
      valfile = args[3];
      testfile = args[4];
      int toprint = 0;
      if(args[5].equals("yes")) toprint = 1;
      else if(!args[5].equals("no")){ 
        System.out.println("Wrong arugment for to-print");
        return;
      }
      D.traindata = D.process(trainfile);
      D.valdata = D.process(valfile);
      D.testdata = D.process(testfile);
      D.used = new int[D.attrs.size()];
      
      System.out.println("\nInformation Gain --->");
      Tree ROOT = D.train(1,D.traindata,D.used);     
      double accuracy = D.classify(D.testdata,ROOT);      
      double oldacc = D.classify(D.valdata, ROOT);
      Tree PRUNED = D.prune(L,K,oldacc,ROOT,D.valdata);
      double newac = D.classify(D.testdata,PRUNED);
      if(toprint == 1){
        System.out.println("Pruned Tree: ");
        D.treePrint(PRUNED,0);
      }
      System.out.println("\n\nPre Pruning Accuracy: " + accuracy);
      System.out.println("Post Pruning Accuracy: " + newac);
      //--//
      System.out.println("\n\nVariance Impurity  --->");
      Tree ROOT2 = D.train(0,D.traindata,D.used);     
      double accuracy2 = D.classify(D.testdata,ROOT2);        
      double oldacc2 = D.classify(D.valdata, ROOT2);
      Tree PRUNED2 = D.prune(L,K,oldacc2,ROOT2,D.valdata);
      double newac2 = D.classify(D.testdata,PRUNED2);
      if(toprint == 1){
        System.out.println("Pruned Tree: ");
        D.treePrint(PRUNED2,0);
      }
      System.out.println("\n\nPre Pruning Accuracy: " + accuracy2);      
      System.out.println("Post Pruning Accuracy: " + newac2);
    }
  }
  
  ArrayList<ArrayList<Integer>> process(String fname){
    Scanner sc = null;
    try{
      sc = new Scanner(new File(fname));          
    }
    catch(Exception e){}
      
      ArrayList<ArrayList<Integer>> data = new ArrayList<ArrayList<Integer>>();
      String atline = sc.nextLine();
      if(attrs.size() == 0){
        String temp[] = atline.split(",");
        for(int i=0;i < temp.length;i++){
          attrs.add(temp[i]);
        }
      }
      while(sc.hasNextLine()){       
          String line = sc.nextLine();
          ArrayList<Integer> tmp = new ArrayList<Integer>();
          String temp[] = line.split(",");
          for(int i=0;i < temp.length;i++){
            tmp.add(Integer.parseInt(temp[i]));
          }
          data.add(tmp);
        
      }
      sc.close();
    return data;  
  }
  
  Tree train(int mode, ArrayList<ArrayList<Integer>> S, int used[]){
    int x = findBest(mode, S, used);
    if(x == -1){
      //make leaf
      Tree leaf = new Tree();
      leaf.makeLeaf(S.get(0).get(attrs.size()-1));
      return leaf;
    }
    else if(x == -2){ return null;}
    else{
      //make internal
      ArrayList<ArrayList<Integer>> S0 = new ArrayList<ArrayList<Integer>>();
      ArrayList<ArrayList<Integer>> S1 = new ArrayList<ArrayList<Integer>>();
      for(int i=0;i < S.size();i++){  
        if(S.get(i).get(x) == 0) S0.add(S.get(i));
        else S1.add(S.get(i));
      }
      Tree node = new Tree();
      if(S1.size() == 0 || S0.size() == 0){
        if(S1.size() == 0)
          node.makeLeaf(S0.get(0).get(attrs.size()-1));
        else
          node.makeLeaf(S1.get(0).get(attrs.size()-1));
        return node;
      }
      int m = 0;
      if(S0.size() < S1.size()) m = 1;
      node.makeNode(x,m);
      int used1[] = new int[used.length];
      for(int l=0;l < used.length;l++) used1[l] = used[l];
      used1[x] = 1;
      node.setLeft(train(mode,S0,used1)); 
      int used2[] = new int[used.length];
      for(int l=0;l < used.length;l++) used2[l] = used[l];
      used2[x] = 1;
      node.setRight(train(mode,S1,used2));
      return node;
      
    }
  }
  
  int findBest(int mode, ArrayList<ArrayList<Integer>> S, int use[]){
    int n = attrs.size();
    double measureS = 0.0f;
    int best = -1;
    double max = -999.0f;
    if(mode == 1){
    //Use Entropy
      double cp[] = new double[2];
      for(int i =0;i < S.size();i++){
        cp[S.get(i).get(n-1)]++; //Count for Classes
      }  
      measureS = enty(cp,S.size()); //Calc Entropy
      if(measureS == 0) return -1;   
      
      for(int j=0;j < n-1;j++){
        if(use[j] == 0){
          double ap[][] = new double[2][2];       
          for(int i=0;i < S.size();i++){
            ap[S.get(i).get(j)][S.get(i).get(n-1)]++;  //Count P class = 0
          }
          double totalnum[] = new double[2];
          totalnum[0] = ap[0][0] + ap[0][1];
          totalnum[1] = ap[1][0] + ap[1][1];
          double ent[] = new double[2];
          for(int i=0;i<2;i++){
            if(totalnum[i] != 0){
              double prob = totalnum[i]/S.size();//Sv/S
              double a = ap[i][0]/totalnum[i];//p0
              double b = ap[i][1]/totalnum[i];//p1
              if(a!=0)
              a = a * (Math.log(a) / Math.log(2));//p0logp0
              if(b!=0)
              b = b * (Math.log(b) / Math.log(2));//p1logp1
              ent[i] = -(a + b);//-Sum PiLogPi
              ent[i] = ent[i] * prob;                    
            /*
              ent[i] = enty(ap[i],totalnum[i]);     
              ent[i] = ent[i] * (totalnum[i]/S.size());*/
              }
          }
          double gain = measureS - ent[0] - ent[1];
          if(gain >= max){max = gain; best = j;}    
        }
      }
      return best;
    }
    else if(mode == 0){
    double varS;
    double maxVar = -999.0d;
    double K = S.size();
    double k[] = new double[2];
    for(int i=0;i< S.size();i++)
      k[S.get(i).get(n-1)]++;
    if(k[0] == 0 || k[1] == 0)
      return -1;
    else
      varS = ((k[0]/K)*(k[1]/K));
    //Variance Impurity
    for(int j = 0;j < n-1;j++){
      if(use[j] == 0){
        double SK[] = new double[2];
        for(int i=0;i < S.size();i++){
          SK[S.get(i).get(j)]++;
        }
        double kk[][] = new double[2][2];
        for(int i=0;i < S.size();i++){
            kk[S.get(i).get(j)][S.get(i).get(n-1)]++;
        }
        double gainval = varS;
        for(int i=0;i < 2;i++){
          double prob = SK[i]/K;
          if(prob != 0){
            SK[i] = prob*((kk[i][0]/SK[i]) * (kk[i][1]/SK[i]));
            gainval = gainval - SK[i];
          }          
        }
        if(gainval > maxVar){ maxVar=gainval; best = j;}  
      }
    }
      return best;
    }
    else
      {System.out.println("Wrong mode - 0/1"); return -2;}
  }
  
  double enty(double cp[], double sz){
    double m =0.0f;
    for(int i=0;i < 2;i++){
        if(cp[i] != 0){
        cp[i] = cp[i]/sz;
        cp[i] = cp[i] * (Math.log(cp[i]) / Math.log(2));
        }
        m = m - cp[i];
      }
    return m;  
  }
  
  void treePrint(Tree T,int lvl){  
    if(T.isLeaf())
      System.out.print(T.getVal());
    else{
      System.out.println();
      for(int i=0;i<lvl;i++) System.out.print("| ");
      System.out.print(attrs.get(T.getAtt()) + " = 0: ");
      treePrint(T.getLeft(),lvl+1);
      System.out.println();
      for(int i=0;i<lvl;i++) System.out.print("| ");
      System.out.print(attrs.get(T.getAtt()) + " = 1: ");
      treePrint(T.getRight(),lvl+1);   
    }
  }
  
  double classify(ArrayList<ArrayList<Integer>> S, Tree tree){
    int num = attrs.size();
    double match=0.0f;
    for(int i=0;i < S.size();i++){
      Tree t = tree;
      while(!t.isLeaf()){
        if(S.get(i).get(t.getAtt()) == 0) t = t.getLeft();
        else t = t.getRight();
      }  
      if(t.getVal() == S.get(i).get(num-1)) 
        match++;   
    }
    return (match/S.size());
  }
  
  int noOfNonLeaf(Tree T){
    if(T.isLeaf()){
      return 0;
    }
    else{
      return 1+noOfNonLeaf(T.getLeft())+noOfNonLeaf(T.getRight());
    }
  }
  
  Tree prune(int L,int K,double oldacc,Tree D,ArrayList<ArrayList<Integer>> vdata){
    Random r = new Random();
    Tree Dbest = D.copy();
    for(int i=1;i <= L;i++){
      Tree treedash = D.copy();
      int M = 1;
      if(K > 1)
        M = r.nextInt(K-1) + 1;
      for(int j=1;j <= M;j++){
        int N = noOfNonLeaf(treedash);
        int P = 1;
        if(N > 1)
          P = r.nextInt(N-1) + 1;
        if(P > 1)
        treedash = trim(P,treedash);       
      }
      double newacc = classify(vdata,treedash);
      if(newacc > oldacc){
        Dbest = treedash;
        oldacc = newacc;
      }
    }
    return Dbest;
  }

  Tree trim(int p, Tree Ddash){
    int head = 0; int tail = 0;
    ArrayList<Tree> queue = new ArrayList<Tree>();
    queue.add(Ddash);
    Tree temp=Ddash;
    while(p > 0 && head < queue.size()){
      //visit node
      temp = queue.get(head++);
      p--;
      //check if not leaf
      //add children to queue
      if(!temp.getLeft().isLeaf())
        queue.add(temp.getLeft());
      if(!temp.getRight().isLeaf())
        queue.add(temp.getRight());   
    }
    temp.makeLeaf(temp.getMore());
    return Ddash;
  }
  
}//Dec Tree Class
  
  
//Class for Tree Data Structure  
class Tree implements Cloneable{
  int att;
  int leaf;
  int val;
  int more;
  Tree left;
  Tree right;
  
  Tree getLeft(){
    return left;
  }
  
  Tree getRight(){
    return right;
  }
  
  void makeLeaf(int ans){
    val = ans;
    leaf = 1;
    att = -1; more = -1; 
    left = null; right = null;
  }
  
  void makeNode(int atr,int max){
    att = atr;
    more = max;
    val = 0;
    left = null; right = null;
    leaf = 0;
  }
  
  void setLeft(Tree t){
    left = t;
  }
  
  void setRight(Tree t){
    right = t;
  }
  
  int getAtt(){
    return att;
  }
  
  int getVal(){
    return val;
  }
  
  boolean isLeaf(){
    if(leaf == 1)return true;
    else return false;
  }
  
  int getMore(){
    return more;
  }
  
  protected Object clone(){
    try{
    return super.clone();
    }catch(Exception e){}
    return null;
  }
  
  Tree copy(){ 
    Tree N = new Tree();
    if(this.isLeaf()){
      N.makeLeaf(this.val);
      return N;
    }  
    else{  
    N.makeNode(this.att,this.more);
    N.left = this.left.copy();
    N.right = this.right.copy();
    return N;
    }
  }
}
