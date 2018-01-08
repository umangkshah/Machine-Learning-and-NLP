"""This module is created to build the report."""
import Inference as Ifr

inputQueries = []
inputQueries.append("[<A,f>][B]")
inputQueries.append("[<A,f>][J]")
inputQueries.append("[<J,t> <E,f>][B]")
inputQueries.append("[<J,t> <E,f>][M]")
inputQueries.append("[<M,t> <J,f>][B]")
inputQueries.append("[<M,t> <J,f>][E]")

sizes = [10, 50, 100, 200, 500, 1000, 10000]
types = [1, 2, 3]
for iq in inputQueries:
    for s in sizes:
        print("size: ", s)
        for t in types:
            ans = 0.0
            print ("Query: ", iq, " type: ", t, " : ")
            for i in range(10):
                str, prob = Ifr.driver(s, iq, t)
                ans += prob
            print(ans/10.)
