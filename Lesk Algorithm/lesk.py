from nltk.corpus import wordnet as wn
from nltk import word_tokenize
from os import sys

def overlap_count(x,y):
  count = 0
  for a in x:
    if a in y:
      count += 1
  return count
  
def lesk(word,sen):
  S = wn.synsets(word,pos='n')
  
  context = set(word_tokenize(sen.lower())) - set([word])
  max_overlap = 0
  if not len(S) > 0:
    print "No noun Senses for",word
    exit()
  best_sense = S[0]
  for sense in S:
    a = sense.definition()
    signature = set(word_tokenize(a.encode('utf-8').lower()))
    b = sense.examples()
    print a,b
    for c in b: 
      signature.union(set(word_tokenize(c.encode('utf-8').lower())))
    overlap = overlap_count(context,signature)
    if overlap > max_overlap:
      max_overlap = overlap
      best_sense = sense
  
  return best_sense
  
#main
if len(sys.argv) < 3:
  print "wrong usage.. "
else:
  w = sys.argv[1]
  s = sys.argv[2]
  best_sense = lesk(w,s)
  print "Sense->",best_sense,best_sense.definition()    
  

