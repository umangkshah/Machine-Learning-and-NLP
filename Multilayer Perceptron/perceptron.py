from os import listdir
import sys
import re
from sets import Set
from sklearn.preprocessing import StandardScaler
from sklearn.neural_network import MLPClassifier
import warnings

def count(folder):
    #print folder
    global vocab
    semi = Set(['BIAS'])
    Y=[]
    for f in listdir(folder):
      s = open(folder+"/"+f,'r').read()
      words = s.split()
      X = {'BIAS' : 1}
      for w in words:
        semi.add(w)
        if X.has_key(w):
          X[w] = X[w] + 1
        else:
          X[w] = 1
      Y.append(X)
    vocab = vocab | semi
    return Y

def initweights():
  global vocab
  W = dict()
  for v in vocab:
    W[v] = 0.0
  return W

def train(n,I):
  global W
  for itr in range(0,I):
    for y in range(0,2):
      for d in features[y]:
        error = delta(d,y)
        for i in d:
          W[i] = W[i] + (n * error * d[i])

def delta(d,y):
  sumn = 0.0
  error = 0.0
  for i in d:
    if i in W:  sumn = sumn + (W[i]*d[i])
  if sumn > 0: 
    o = 1
  else:    o = -1
  if y == 0:#spam
    error = -1 - o
  else:    error = 1 - o
  return error

def countForTest(folder):
  #print folder
  Y = []
  for f in listdir(folder):
      s = open(folder+"/"+f,'r').read()
      words = s.split()
      X = {'BIAS' : 1}
      for w in words:
        if X.has_key(w):
          X[w] = X[w] + 1
        else: X[w] = 1
      Y.append(X)
  return Y

def test():
  correct = 0.0
  total = 0.0
  for y in range (2,4):
    for d in features[y]:
      total = total + 1
      error = delta(d,y-2)
      if error == 0:
        correct = correct + 1
  return correct/total

def convert(a, b):
  X=[]
  Y=[]
  for y in range(a,b):
    for d in features[y]:
      Xi = []
      for v in vocab:
        if v in d:  Xi.append(float(d[v]))
        else: Xi.append(0.)
      X.append(Xi)     
      if y == 0 or y == 2:   Y.append(0)
      else: Y.append(1)
  return X, Y
  
  
#Parameters
eta = 0.01
Itr = 500
alpha = 0.8
hidden = 32
#Begin Single

vocab = Set(['BIAS'])
features = [] 
f1 = "./"+sys.argv[1]
f2 = "./"+sys.argv[2]
trainfiles = listdir(f1)
testfiles = listdir(f2)
for f in trainfiles:
  Y = count(f1+"/"+f)
  features.append(Y) #Y = 0 Spam & Y = 1 Ham
W = initweights()
train(0.02,50)
for f in testfiles:
  Y = countForTest(f2+"/"+f)
  features.append(Y) #Y = 2 Spam & Y = 3 Ham
print "Simple Perceptron Accuracy: ", test()
#End

#Begin Multilayer
print "MultiLayer Perceptron:\nlearning rate: ", eta, " Iterations: ", Itr, " Momentum: ", alpha, " Hidden Layers for MLP: ", hidden
Xtrain, Ytrain = convert(0,2)
Xtest, Ytest = convert(2,4)
scaler = StandardScaler() #Scale Vectors
scaler.fit(Xtrain)
Xtrain = scaler.transform(Xtrain)
Xtest = scaler.transform(Xtest)
clf = MLPClassifier(solver='sgd', max_iter=Itr, learning_rate_init=eta, \
                  #verbose='true', \
                  momentum=alpha, hidden_layer_sizes=(hidden,), activation='logistic')
clf.fit(Xtrain,Ytrain)
Yop = clf.predict(Xtest)
correct = 0.0
for y in range (0,len(Ytest)):
  if Ytest[y] == Yop[y]: correct=correct + 1
print "\nAccuracy: ", correct/len(Yop)
#End




