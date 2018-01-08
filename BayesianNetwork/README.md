# BayesianNetwork
This is programming assignment 3 for the Artifical Intelligence course at UT Dallas Fall 2017 submitted by Umang K Shah and  Harsha Kokel.


The goal of this programming assignment is to understand the relative properties of the inference algorithm. In order to do so, you are required to design the alarm Bayes net presented in the class and shown in Figure 14:2 from the Russell and Norvig book.

![alt text](https://github.com/harshakokel/BayesianNetwork/blob/master/bayes-net.png "BayesianNetwork")


Use the parameters of the CPTs from the Figure. Implement exact inference by enumeration, prior sampling, rejection sampling and likelihood weighting.

###### Input Format:  
Program takes a set of strings as input. They will appear as the following: [< A, t >< B, f >][M, A]. The first set of strings is the evidence set and the next is the query.  

For simplicity use the node indexes as A, B, E, J, M corresponding to alarm, burglary,
earthquake, John and Mary calling. An example queries for John calling given that alarm is true and burglary is false will [< A, t >< B, f >][J]. Similarly, [< E, t >< J, t >][M, A] which queries for Mary calling and Alarm given that earthquake and John calling are true.

###### Output:  
The output is set of strings [< M, 0:08 >< A, 0:96 >] with corresponding probability

#### Instruction to run the code.

1. This code needs python3 version

2. Run the Inference.py file from the src folder.
     ```python  
     $ python3 Inference.property     
     ```

3. You will be prompted for sample count for sampling Inference.
    ```python
    Enter sample count:
    ```
    Enter the sample size in the command prompt.  

4. You will be prompted for the input query.
    ```python
    Enter input query (of the form '[<E,t> <J,t>][M, A]' ):
    ```
    Few sample queries are listed below:  
        [<M,t> <J,f>][B, E]   
        [<J,t> <E,f>][B, M]  
        [<A,f>][B, J]  
5. Code will return the inferred probability by 4 methods:
        Inference by Enumeration
        Prior sampling
        Rejection Sampling
        likelihood Weighting

Sample run:


```python
$ python3 Inference.py
Enter sample count:
100
Enter input query (of the form '[<E,t> <J,t>][M, A]' ):
[<E,t> <J,t>][M, A]
Inference by Enumeration:
['<M, 0.6176188392283065>', '<A, 0.8806070133743574>']
Inference by prior sampling:
['<M, 0.0>', '<A, 0.0>']
Inference by Rejection sampling:
['<M, 0.63>', '<A, 0.85>']
Inference by likelihood sampling:
['<M, 0.6573033707865171>', '<A, 0.8571428571428571>']
```
