#README
Processing procedure:
- get list of tokens under consideration, here adjectives (by lemma or token)
- run smor analysis on them (cf. usefulCommans.txt for run command)
- update dictionary of (non-)verb-derived adjectives by going over the smor analysis (SmorAnalyzer.java)
- for verb-derived adjectives, get base verbs
- get frequency distributions
    - how often does which adjective occur? are there differences between attributive and predicative adjectives?
    - how often is which adjective modified by which preposition?
    - where does the preposition(al phrase) occur? in front or after the adjective?
    - for verb-derived adjectives, what are the differences in distribution between the adjective and its base verb?
