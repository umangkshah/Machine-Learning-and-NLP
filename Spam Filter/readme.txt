//Spam Filter - Umang Shah

1. Compile > javac SpamFilter.java
Following classes should be created: Helper, SpamFilter, NaiveBayesFilter, LogisRegr

2. Run > java SpamFilter <removeStopWords(yes/no)>  [<eta> <lambda> <noOfIter>]

eg 1. java SpamFilter no
This will run Both NB and LR without removing stop words. And will use default values of eta, lamba,noofIter.
eg 2. java SpamFilter yes 0.05 6 81
This will run both NB and LR removing stop words. It will use eta(Learning Rate) = 0.05, lambda(Normalization factor) = 6 and NoOfIter = 81.

