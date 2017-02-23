#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <stdlib.h>
#include <tgmath.h>
using namespace std;

int nodeno=0;
int leaves=0;
struct node{
  int no;
  int att;
  int val;
  struct node *l_c;
  struct node *r_c;
  int more;
};

void process(char* fileloc, vector<string> &attrs, vector< vector<int> > &traindata);
int findbest(int mode,vector< vector<int> > S,bool used[],int n);
struct node* train(int mode,vector< vector<int> > S,bool used[],int n);
float enty(float cp[], int sz);
void treeprint(struct node* tree, vector<string> attrs,int lvl);
float classify(vector< vector<int> > S, struct node* tree,int n);
struct node* prune(int L,int K,float oldacc,struct node* tree,vector < vector<int> > vdata, vector <string> attrlist);
struct node* trim(int p, struct node* Ddash);
int noOfNonLeaf(struct node* &tree);

// train 2 val 3 test 4 toprint 5 mode
int main(int argc, char *argv[]){
  if(argc < 6)
    cout << "Not enough arguments";
  else{  
    char *md = argv[5];
    int mde = atoi(md);
    int toprint;
    string printarg(argv[4]);
    if(printarg == "yes")
      toprint = 1;
    else if(printarg == "no")
      toprint = 0;
    else
    {
      cout << "to print should be yes or no";
      return 1;
      } 
    vector <string> attrs;
    vector < vector <int> > traindata;
    process(argv[1], attrs,traindata);  //Store 1st arg: Training data into vector/array
    bool use[attrs.size()] = {false};
    node *ROOT = new node;
    ROOT = train(mde,traindata,use,attrs.size());   //Training; 1 for Entropy 0 for Variance
    //cout<<endl;
    // Print if yes
    if(toprint == 1)
      treeprint(ROOT,attrs,0);
    /*cout << "total nodes: " << nodeno;
    nodeno=0;
    cout << "leaf nodes: " << leaves << endl;
    leaves = 0;  */
    //process test set
    vector <string> attrs2;
    vector < vector <int> > testdata;  
    process(argv[3], attrs2, testdata);
    //classify test set
    float accuracy = classify(testdata, ROOT, attrs.size()); //Accuracy of Classification
    cout << "Prepruning Accuracy: " << accuracy << endl;
    // process val set
    vector <string> attrs1;
    vector < vector <int> > valdata;  
    process(argv[2], attrs1,valdata);
    float val_accuracy = classify(valdata,ROOT, attrs.size());
    //PRUNE  and Classify    
    struct node* PRUNED = prune(6,5,val_accuracy,ROOT,valdata,attrs);
    float accuracy2 = classify(testdata, PRUNED, attrs.size()); //Accuracy of post pruning
    cout << "Postpruning Accuracy: " << accuracy2 << endl;

  }
  return 1;
}


void process(char* fileloc, vector<string> &attrs, vector<vector<int> > &traindata){
  ifstream dfile (fileloc);
  if ( !dfile.is_open() )
    cout << "File could not be opened";
  else{
    string txt;
    getline(dfile,txt,'\n');
    stringstream ss(txt);
    string a;      
    while(getline(ss,a,',')){
      attrs.push_back(a);
    }     
    while(getline(dfile,txt,'\n')){
      stringstream ss(txt);
      string a;
      vector <int> temp;      
      while(getline(ss,a,',')){
        temp.push_back(atoi(a.c_str()));
      }
      traindata.push_back(temp);
    }
  }
}

struct node* train(int mode,vector< vector<int> > S,bool used[],int n){
  //if(n > 21) cout << n;
  int x = findbest(mode, S, used, n);
  if(x == -1){
    //leaf node found
    node *NODE = new node;
    NODE->att= -1; NODE->more = -1;
    NODE->val = S[0].at(n-1);
    NODE->l_c = NULL;
    NODE->r_c = NULL;
    leaves++;
    return NODE;
  }
  else{   
    used[x] = true;
    bool use0[n];
    copy(used,used+n,use0);
    /*copy(used,used+n,use1);*/
    vector< vector <int> > S0;
    vector< vector <int> > S1;
    for(int i=0;i < S.size();i++){  
      if(S[i].at(x) == 0) S0.push_back(S[i]);
      else S1.push_back(S[i]);
    }
    node *NODE = new node;
    NODE->att= x; NODE->val=0;
    if(S1.size() > S0.size()) NODE->more = 1;
    else NODE->more = 0;
    if(S0.size() == 0 || S1.size() == 0){
      int ok;
      if(S1.size() == 0)
        ok = S0[0].at(n-1);
      else
        ok = S1[0].at(n-1);
      NODE->val = ok;   
    }
    else{
      //cout << " \nNODE for " << x;
      NODE->l_c = train(mode,S0,used,n);   
      NODE->r_c = train(mode,S1,use0,n);
    }
    return NODE;  
  }
}


int findbest(int mode,vector< vector<int> > S,bool used[],int n){
    //if(n > 21) cout << n;
    float measureS = 0.0f;
    int best = -1;
    float max = -999.0f;
    if(mode == 1){//Entropy
      float cp[2] ={0.0f,0.0f};
      for(int i =0;i < S.size();i++){
        cp[S[i].at(n-1)]++; //Count for Classes
      }  
      measureS = enty(cp,S.size()); //Calc Entropy
      if(measureS == 0) return -1;   
      
      for(int j=0;j < n-1;j++){
        if(j > 21) cout << j;
        if(!used[j]){
          float ap[2][2] = { {0.0f,0.0f}, {0.0f , 0.0f}};         
          for(int i=0;i < S.size();i++){
            if(S[i].at(j)==0)
              ap[0][S[i].at(n-1)]++;  //Count P class = 0
            else
              ap[1][S[i].at(n-1)]++;  //Count P class = 1
          }
          float totalnum[2];
          totalnum[0] = ap[0][0] + ap[0][1];
          totalnum[1] = ap[1][0] + ap[1][1];
          float ent[2];
          for(int i=0;i<2;i++){
            if(totalnum[i] != 0){
              ent[i] = enty(ap[i],totalnum[i]);     
              ent[i] = ent[i] * (totalnum[i]/S.size());
              }
          }
          float gain = measureS - ent[0] - ent[1];
          if(gain > max){max = gain; best = j;}    
        }
        /*else
         cout << "Already used: " << j << endl;*/
      }
      if(best > 21) cout << best;
      return best;
    }
    else{//Variance
    
     float K = S.size();
     float k[2] = {0.0f,0.0f};
     for(int i=0;i < K;i++){
      k[S[i].at(n-1)]++;
     }
     measureS = (k[0]/K)*(k[1]/K);
     if(measureS == 0) return -1;
     for(int j=0;j < n-1;j++){
      float ks[2] = {0.0f,0.0f};
      if(!used[j]){      
        for(int i=0;i < S.size();i++) //Count no of attrs 0 / 1 (size of Sets S0 S1)
          ks[S[i].at(j)]++;
        //var(Sj)
        float kk[2][2] = {{0.0f, 0.0f},{0.0f, 0.0f}};
        
        for(int i=0;i < S.size();i++){
          if(S[i].at(j) == 0)
            kk[0][S[i].at(n-1)]++;//count class 0/1 in S0
          else
            kk[1][S[i].at(n-1)]++;//count class 0/1 in S1
        }
        for(int i=0;i < 2;i++){
          float prob = ks[i]/K;//prob S0 i.e P(0) P(1)
          ks[i] = ((kk[i][0]/ks[i])*(kk[i][1]/ks[i])) * prob;
          float gain = measureS - ks[i];
          if(gain > max){max = gain; best = j;}
        }
      }
     }
      if(best > 21) cout << best;  
      return best;
    }
}

float enty(float cp[], int sz){
  float m =0.0f;
  for(int i=0;i < 2;i++){
      if(cp[i] != 0){
      cp[i] = cp[i]/sz;
      cp[i] = cp[i] * log2f(cp[i]);
      }
      m = m - cp[i];
    }
  return m;  
}

void treeprint(struct node* tree, vector<string> attrs,int lvl){  
  if(tree->l_c == NULL && tree->r_c == NULL)
    cout << tree->val;
  else{
    cout << endl;
    for(int i=0;i<lvl;i++) cout << "| ";
    cout << attrs[tree->att] << " = 0: ";
    treeprint(tree->l_c, attrs, lvl+1);
    cout << endl;
    for(int i=0;i<lvl;i++) cout << "| ";
    cout << attrs[tree->att] << " = 1: ";
    treeprint(tree->r_c, attrs, lvl+1);    
  }
}


float classify(vector< vector<int> > S, struct node* tree,int num){
  if(num > 21) cout << num;
  float match=0.0f;
  for(int i=0;i < S.size();i++){
    struct node* ptr = tree;
    while(ptr->l_c!=NULL && ptr->r_c!=NULL){
      if(S[i].at(ptr->att) == 0) ptr = ptr->l_c;
      else ptr = ptr->r_c;
    }  
    if(ptr->val == S[i].at(num-1)) 
      match++;
    //else cout << "nomatch";    
  }
  return (match/S.size());
}

int noOfNonLeaf(struct node* &tree){
  if(tree->l_c == NULL && tree->r_c == NULL){
    //cout << tree->att << " ";
    return 0;
  }
  else{    
//    if(tree->att < 0 || tree->att > 20)
   //cout << tree->att << " ";
    return 1+noOfNonLeaf(tree->l_c)+noOfNonLeaf(tree->r_c);
  }
}

struct node* prune(int L,int K,float oldacc,struct node* D,vector < vector<int> > vdata, vector <string> attrlist){
  struct node* Dbest = D;
  for(int i=1;i <= L;i++){
    struct node* treedash = D;
    cout << treedash->att << " ";
    int M = rand() % K + 1;
    for(int j=1;j <= M;j++){
      int N = noOfNonLeaf(treedash);
      int P = rand() % N + 1;
      treedash = trim(P,treedash);
    }
    float newacc = classify(vdata,treedash,attrlist.size());
    if(newacc > oldacc){
      Dbest = treedash;
      oldacc = newacc;
    }
  }
  return Dbest;
}

struct node* trim(int p, struct node* Ddash){
  int head = 0; int tail = 0;
  vector <struct node*> queue;
  queue.push_back(Ddash);
  struct node* temp;
  while(p > 1 && head < queue.size()){
  //visit node
  temp = queue[head++];
  p--;
  //check if not leaf
  //add children to queue
  if(temp->l_c != NULL && temp->l_c->att != -1)
    queue.push_back(temp->l_c);
  if(temp->r_c != NULL && temp->r_c->att != -1)  
    queue.push_back(temp->r_c);    
  }
  temp->l_c = NULL;
  temp->r_c = NULL;
  temp->att = -1;
  temp->val = Ddash->more;
}
