## Processing procedure
- Get list of tokens under consideration, here adjectives (by lemma or token)
- Run smor analysis on them
- Update dictionary of (non-)verb-derived adjectives by going over the smor analysis (SmorAnalyzer.java)
- For verb-derived adjectives, get base verbs
- Get frequency distributions
    - How often does which adjective occur? are there differences between attributive and predicative adjectives?
    - How often is which adjective modified by which preposition?
    - Where does the preposition(al phrase) occur? in front or after the adjective?
    - For verb-derived adjectives, what are the differences in distribution between the adjective and its base verb?
