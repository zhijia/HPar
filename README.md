# jsoupp
**jsoupp** is a prototype of a data-level parallel parser. It parses a single HTML file in parallel.

### How To Use:

    ParallelParser pparser = new ParallelParser(html, numThreads);
    doc = pparser.parse();

Reference:

    Zhao, Z., Bebenita, M., Herman, D., Sun, J., & Shen, X. (2013). 
    HPar: A practical parallel parser for HTML--taming HTML complexities for parallel parsing. 
    ACM Transactions on Architecture and Code Optimization (TACO), 10(4), 44.
