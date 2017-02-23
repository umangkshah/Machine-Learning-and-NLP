import java.util.*;
import java.io.*;

public class Viterbi{
  public static void main(String arg[])throws Exception{
    if(arg.length < 1)
      throw new Exception("Argument needed");
    String txt = arg[0];
    int O[] = new int[txt.length()];
    for(int i=0;i < txt.length();i++){
      O[i] = Integer.parseInt(txt.substring(i,i+1));
    }
    String state[] = {"S","H","C"};
    double b[][] = new double[4][3];
    //values of bj(Ok) => b[o,j]
    b[1][1] = 0.2;
    b[2][1] = 0.4;
    b[3][1] = 0.4;
    b[1][2] = 0.5;
    b[2][2] = 0.4;
    b[3][2] = 0.1;
    //aij values => a[i,j]
    double a[][] = new double[3][3];
    a[0][1] = 0.8;
    a[0][2] = 0.2;
    a[1][1] = 0.7;
    a[1][2] = 0.3;
    a[2][1] = 0.4;
    a[2][2] = 0.6;
    //
    int T = O.length - 1;
    int path[] = new int[T+1];
    double v[][] = new double[state.length+1][O.length];
    int bt[][] = new int[state.length+1][O.length];
    
    for(int i=1;i < state.length;i++)
      v[i][0] = a[0][i] * b[O[0]][i];
    
    for(int t = 1;t < O.length;t++){
      for(int j = 1;j < state.length;j++){
         double max = -9999.0;
         int best = 0;
         for(int k = 1;k < state.length;k++){
            double tmp = v[k][t-1] * a[k][j] * b[O[t]][j];
            if(tmp > max){  max = tmp; best = k; }
         }
         v[j][t] = max;
         bt[j][t] = best;
      }
    }
  
    int trace = (v[1][T] > v[2][T]? 1 : 2);
    
    for(int i = T;i >= 0; i--){
      path[i] = trace;
      trace = bt[trace][i];      
    }
    
    
    for(int x:path)
      System.out.print(state[x]);
    System.out.println();
  }
}
