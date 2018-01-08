"""Module represents Inference for Bays Net."""
import BNET
from ApproxInference import ApproxInference


class Inference:
    """This is inference class."""

    cpt = BNET.CPT()
    bnet = BNET.Network()

    def __init__(self, t, noOfSamples):
        """Type 0: enum, 1: prior samp, 2: rej samp, 3: liklihood weighting."""
        self.type_ = t
        self.noOfSamples = noOfSamples

    def infer(self, query):
        """Return the inference of query.

        Args:
            query (string) String with List of evidence and the query nodes
                           Example: [<E,t> <J,t>][M, A]
        """
        strings = query.strip("[").strip("]").split("][")
        process = self.processStr(strings[0], strings[1])
        prior = process[0]
        postList = process[1]
        inferred_prob = []
        for posterior in postList:
            prob = self.doInference[self.type_](self, posterior, prior)
            inferred_prob.append("<" + posterior + ", " + str(prob) + ">")
        return inferred_prob, prob

    def processStr(self, strE, strQ):
        """Return the evidence list and query list for given strings."""
        eList = {}
        e = strE.replace("<", "").replace(">", "").split(" ")
        for x_ in e:
            x = x_.split(",")
            truthValue = 0
            if(x[1] == "t"):
                truthValue = 1
            eList[x[0]] = truthValue
        qList = [y.strip() for y in strQ.split(",")]
        return(eList, qList)

    def byEnumeration(self, query, evidence):
        """Infer the exact probability of the query by enumeration."""
        variables = self.bnet.nodes()
        evidence_ = evidence.copy()
        joint_distribution = []
        # for post = True and False
        for p_truth_value in [1, 0]:
            evidence_[query] = p_truth_value
            joint_distribution.append(self.enumerateAll(variables, evidence_))
        return self.normalize(joint_distribution)[0]

    def normalize(self, joint_distribution):
        """Return normalized probability."""
        return [x/sum(joint_distribution) for x in joint_distribution]

    def enumerateAll(self, variables, evidence):
        """Enumerate full joint_distribution for the given evidence."""
        if not variables:
            return 1
        sum_ = 0
        if variables[0] in evidence.keys():
            prior_ = [evidence[parent] for parent in self.bnet.parent(variables[0])]
            prob_ = self.cpt.probOf((variables[0], evidence[variables[0]]), prior_)
            sum_ = prob_ * self.enumerateAll(variables[1:], evidence)
        else:
            for val_ in [1, 0]:
                new_evidence = evidence.copy()
                new_evidence[variables[0]] = val_
                prior__ = [new_evidence[parent] for parent in self.bnet.parent(variables[0])]
                prob_ = self.cpt.probOf((variables[0], val_), prior__)
                sum_ = sum_ + (prob_ * self.enumerateAll(variables[1:], new_evidence))
        return sum_

    def priorSample(self, query, evidence):
        """Wrapper method for prior sampling."""
        approxInference = ApproxInference(self.bnet, self.cpt)
        evidence_ = [(k, v) for k, v in evidence.items()]
        return approxInference.priorSampling(self.noOfSamples, evidence_, query)

    def rejectionSample(self, query, evidence):
        """Wrapper method for rejection sampling."""
        approxInference = ApproxInference(self.bnet, self.cpt)
        return approxInference.rejectionSampling(self.noOfSamples, evidence, query)

    def likelihood(self, query, evidence):
        """Wrapper method for likelihood weightage sampling."""
        approxInference = ApproxInference(self.bnet, self.cpt)
        # evidence_ = [ (k,v) for k,v in evidence.items()]
        return approxInference.likelihoodWeighting(self.noOfSamples, evidence, query)

    # map the inference to the function blocks
    doInference = {0: byEnumeration,
                   1: priorSample,
                   2: rejectionSample,
                   3: likelihood
                  }  # replace 2 with rejection sampling

def main():
    """
    Sample 1
    Input: "[<J,t> <M,t>][B]"
    Output: ['<B, 0.284171835364>']

    Sample 2
    Input: "[<E,t> <J,t>][M, A]"
    Output: ['<M, 0.617618839228>', '<A, 0.690886819526>']

    Sample 3
    Input: "[<E,f> <B,t>][A]"
    Output: ['<A, 0.94>']
    """
    print ("Enter sample count: ")
    s = input()
    NoOfSamples = int(s)
    print ("Enter input query (of the form '[<E,t> <J,t>][M, A]' ): ")
    input_query = input()
    print ('Inference by Enumeration: ')
    exactInfer = Inference(0, NoOfSamples)
    print ( exactInfer.infer(input_query)[0])
    print ('Inference by prior sampling: ')
    priorSamplingInfer = Inference(1, NoOfSamples)
    print (priorSamplingInfer.infer(input_query)[0])
    print ('Inference by Rejection sampling: ')
    rejectionSamplingInfer = Inference(2, NoOfSamples)
    print (rejectionSamplingInfer.infer(input_query)[0])
    print ('Inference by likelihood sampling: ')
    likelihoodInfer = Inference(3, NoOfSamples)
    print (likelihoodInfer.infer(input_query)[0])


main()
