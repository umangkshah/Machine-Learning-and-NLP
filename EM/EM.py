from collections import namedtuple
from os import sys
import math
import random 
import numpy as np
from sklearn.cluster import KMeans
from copy import deepcopy

def randomInit(K, fixVar):
  global data
  GM = list()
  for k in range(0,K):
    #randomize betw min max
    b = 1.0
    a = random.uniform(min(data), max(data))
    if not fixVar == 1:
      b = random.uniform(min(data), max(data))
    c = random.uniform(min(data), max(data))
    GM.append(mixture(a, b, c))
  return GM

def sortedCluster(K, fixVar):
  global data
  d2 = sorted(data)
  n = len(d2)/K;
  a = list()
  a.append(d2[:n])
  a.append(d2[n+1:2*n])
  a.append(d2[(2*n)+1:])
  GM = list()
  for i in range(0,K):
    U = np.mean(a[i])
    if fixVar == 1: V = 1.0
    else: V = np.var(a[i])
    GM.append(mixture(U, V, 0.1))
  return GM
  
def kmeans(K, fixVar):
  GM = list()
  global data
  km = KMeans(n_clusters = K).fit(np.reshape(data,(-1,1)))
  a = list()
  for i in range(0,K):
    a.append(list())
    for j in range(0,len(data)):
      if km.labels_[j] == i:  a[i].append(data[j]) 
    U = np.mean(a[i])
    if fixVar == 1: V = 1.0
    else: V = np.var(a[i])
    GM.append(mixture(U, V, 0.1))  
  return GM
  
def load(filename):
  f = open(filename,"r").read()
  data = [float(x) for x in f.split()]
  return data

def px(x, M):
  den = (math.pow((2*math.pi),0.5) * math.pow(abs(M[1]),0.5))
  expo = math.exp(((-0.5)*(x - M[0]))*math.pow(M[1],-1)*(x-M[0]))
  ans = (1/den)*expo
  return ans

def E(data, GM, K):
  global W
  for i in range(0,N):
    for k in range(0,K):
      num = (GM[k][2]*px(data[i],GM[k]))
      den = 0.0   
      for m in range(0,K):
        den += (GM[m][2]*px(data[i],GM[m]))
      wik = num/den
      W[i][k] = wik

def M(data, W, K, fixVar):
  global GM
  N = list()
  for k in range(0,K):
    N.append(0.0)
    muaux = 0.0
    sigmaaux = 0.0
    for i in range(0,len(data)):
      N[k] += W[i][k]
      muaux += (W[i][k]*data[i])
    alphak = N[k] / len(data)
    muk = ((1/N[k]) * muaux)
    if fixVar == 1: sigmak = 1.0
    else:
      for i in range(0,len(data)):
        sigmaaux += (W[i][k] * math.pow((data[i] - muk),2))
      sigmak = (1/N[k]) * sigmaaux
    GM[k] = mixture(muk, sigmak, alphak)  

def hasConverged(GM1, GM2):
  diff = 0.0
  for k in  range(0,len(GM1)):
    diff += abs((GM1[k][0] - GM2[k][0]))
    diff += abs((GM1[k][1] - GM2[k][1]))
    diff += abs((GM1[k][2] - GM2[k][2]))
  if diff < 0.01: return True
  return False

#main
data = load("em_data.txt")
K = 3
initMode = int(sys.argv[1])
fixVar = int(sys.argv[2])
N = len(data)
#print data, N
mixture = namedtuple("mixture","mean variance alpha")
if initMode == 1:
  GM = randomInit(K, fixVar)
if initMode == 2:
  GM = sortedCluster(K, fixVar)
if initMode == 3:
  GM = kmeans(K, fixVar)
#init Wik
W = list()
for i in range(0,N):
  W.append(list())
  for k in range(0,K):
    W[i].append(0.0)   
#till convergence
while True:
  GMold = deepcopy(GM)
  E(data, GM, K)
  M(data, W, K, fixVar)
  if hasConverged(GMold,GM): break
#print mixture components
for k in range(0,K):
  print GM[k]
