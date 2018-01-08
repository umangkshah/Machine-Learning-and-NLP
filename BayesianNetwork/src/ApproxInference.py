import BNET
from random import randint
from random import uniform

class ApproxInference:

    def __init__(self, bnet, cpt):
        self.topologicalOrder = bnet.nodes()
        self.cpt = cpt
        self.bnet = bnet

    def genSample(self):
        # map to record value of parent
        parVal = {}

        for c in self.topologicalOrder:
            pars = self.bnet.parent(c)
            probDist = 0.0
            # calc the probability distribution of current variable
            if len(pars) < 1:
                # no parent so just get distr
                probDist = self.cpt.probOf((c, 1), [])
            else:
                post = [parVal[x] for x in pars]
                probDist = self.cpt.probOf((c, 1), post)
            r = uniform(0.0, 1.0)
            if r <= (probDist):
                parVal[c] = 1
            else:
                parVal[c] = 0
        return list(parVal.values())

    def genWeightedSample(self, e):
        w = 1  # intially weight is 1
        parVal = {}  # record the sample in top order
        for c in self.topologicalOrder:
            pars = self.bnet.parent(c)  # parents of current node
            probDist = 0.0
            #  calc the probability distribution of current variable
            if len(pars) < 1:   # no parent so just get distr
                probDist = self.cpt.probOf((c, 1), [])
            else:
                post = [parVal[x] for x in pars]
                probDist = self.cpt.probOf((c, 1), post)

            # if evidence variable update weight, sample has truth value of evidence
            if c in e.keys():
                if e[c] == 1:
                    w = w * probDist
                    parVal[c] = 1
                else:
                    w = w * (1. - probDist)
                    parVal[c] = 0
            # else randomly sample
            else:
                r = uniform(0.0, 1.0)
                if r <= (probDist):
                    parVal[c] = 1
                else:
                    parVal[c] = 0
        return w, list(parVal.values())

    def priorSampling(self, noOfSamples, evidence, query):
        return self.count(self.genNSamples(noOfSamples), evidence, query)

    def likelihoodWeighting(self, N, e, var_):
        pos = 0.0
        neg = 0.0
        ix = self.topologicalOrder.index(var_)

        for i in range(N):
            w, x = self.genWeightedSample(e)
            if x[ix] == 1:
                pos += w
            else:
                neg += w
        tot = pos+neg
        if tot > 0.0:
            # print(" < ", pos/tot, ",", neg/tot, ">")
            return pos/tot
        return 0.0

    def genNSamples(self, n):
        samples = []
        for k in range(n):
            s = self.genSample()
            samples.append(s)
            # print(s)
        return samples

    def count(self, samples, evidence, query):
        # list [(a,1), (b,0) , .. ]
        evidences = {}
        vix = self.topologicalOrder.index(query)
        pos = 0.0
        neg = 0.0
        for e_ in evidence:
            ix = self.topologicalOrder.index(e_[0])
            evidences[ix] = e_[1]
        for s in samples:
            ignore = 0
            for ix, t in evidences.items():
                if s[ix] != t:
                    ignore = 1
                    break
            if not ignore == 0:
                if s[vix] == 1:
                    pos += 1
                else:
                    neg += 1

        t = pos + neg
        if t != 0:
            # print ("<", pos/t, ",", neg/t, ">")
            return float(pos)/float(t)
        else:
            return 0.0

    def rejectionSampling(self, noOfSamples, evidence, query):
        """Calculate the probability of query using rejection sampling.

           Find the probability of query being true given the evidence
           values using rejection sampling algo and the no of Samples mentioned.

           Args:
            noOfSamples (int)   No of samples
            evidence (dict)     Evidence dictionary with nodes as key and respective
                                truthvalues as value.
            query (string)      Name of node queried
        """
        samples = []
        for k in range(noOfSamples):
            s = self.generateRejectionSample(evidence)
            samples.append(s)
        positiveQueryCount = 0
        for sample in samples:
            if sample[query]:
                positiveQueryCount = positiveQueryCount + 1
        prob = float(positiveQueryCount)/noOfSamples
        return prob

    def generateRejectionSample(self, evidence):
        """Generate a sample that agrees with the evidence.

        Args:
            evidence (dict) Evidence dictionary with nodes as key and respective
                            truthvalues as value.
        """
        sample = {}
        sampleNotFound = True
        while sampleNotFound:
            sample = {}
            for node in self.topologicalOrder:
                parents = self.bnet.parent(node)
                probabilityOfNode = 0
                # calc the probability of current variable
                if len(parents) < 1:
                    # no parent so just get distr
                    probabilityOfNode = self.cpt.probOf((node, 1), [])
                else:
                    prior = [sample[x] for x in parents]
                    probabilityOfNode = self.cpt.probOf((node, 1), prior)
                randomValue = uniform(0.0, 1.0)
                if randomValue <= (probabilityOfNode):
                    if node in evidence:
                        if evidence[node] == 0:
                            break
                    sample[node] = 1
                else:
                    if node in evidence:
                        if evidence[node] == 1:
                            break
                    sample[node] = 0
            if len(sample) == len(self.topologicalOrder):
                    sampleNotFound = False
        return sample
