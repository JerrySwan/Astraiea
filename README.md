# Astraiea
Java library for fair comparison of experimental results

================= ASTRAIEA 2016 ==========================

By Geoffrey Neumann, Jerry Swan, Mark Harman and John A. Clark

Astraiea is a tool kit for the fair comparison of experimental results. While applicable to the analysis of any experimental treatments, it contains additional functionality for assessing search based software engineering techniques.

If you use Astraiea, please be kind enough to cite the associated papers:

@inproceedings{Neumann:2014:EET:2598394.2609850, author = {Neumann, Geoffrey and Swan, Jerry and Harman, Mark and Clark, John A.}, title = {The Executable Experimental Template Pattern for the Systematic Comparison of Metaheuristics}, booktitle = {Proceedings of the Companion Publication of the 2014 Annual Conference on Genetic and Evolutionary Computation}, series = {GECCO Comp '14}, year = {2014}, isbn = {978-1-4503-2881-4}, location = {Vancouver, BC, Canada}, pages = {1427--1430}, numpages = {4}, url = {http://doi.acm.org/10.1145/2598394.2609850}, doi = {10.1145/2598394.2609850}, acmid = {2609850}, publisher = {ACM}, address = {New York, NY, USA} }

@inproceedings{DBLP:conf/ssbse/NeumannHP15, author = {Geoffrey Neumann and Mark Harman and Simon M. Poulding}, title = {Transformed Vargha-Delaney Effect Size}, booktitle = {Search-Based Software Engineering - 7th International Symposium, {SSBSE} 2015, Bergamo, Italy, September 5-7, 2015, Proceedings}, pages = {318--324}, year = {2015}, doi = {10.1007/978-3-319-22183-0_29}, }

Astraiea is designed to comply with the seminal paper on statistical methods for software engineering by Andrea Arcuri and Lional Briand: "A Hitchhiker's guide to statistical tests for assessing randomized algorithms in software engineering" (Arcuri2014).

Astraiea takes as input either:

Two sets of results
A collection of programs for producing results and performs a detailed statistical analysis.
It will print a LaTeX file giving the results of these comparisons which can then be added to research papers. The location of this LaTeX file can be set by calling astraiea.Layer1.setupLaTeXLoggers(String filename).

Below is explained how Astraiea may be used to carry out this comparison and how it may be configured to carry out various different tests depending on the nature of the data.

Astraiea may be accessed through one of the two layers of which it is comprised: 1) Running through Layer 1: Comparing two sets of results already obtained. 2) Running through Layer 2: Providing Astraiea with two or more result generators which will then be invoked. The output of these will be input in pairs into layer 1 which will then compare them.

==============Layer 1==================================

1.1 - DEFAULT:

By default, according to the instructions in (Arcuri2014), two datasets will be compared using: 1) The Wilcoxon/Mann-Whitney U test to test statistical significance. 2) The Vargha Delaney test to test effect size. 3) Confidence intervals are obtained on the effect size through bootstrapping. The intervals are the same size as the significance threshold i.e. with a threshold of 0.05 confidence intervals at 45% and 55% are shown.

Below is an example of calling layer1 with this default set up. For an explanation of these parameters see the documentation for astraiea.Layer1

Layer1.compare( dataA, dataB, double significanceThreshold, boolean brunnerMunzel, new Random() );

"dataA" and "dataB" may be either arrays of lists of doubles.

1.2 OTHER COMPARISONS

In addition the following comparisons may be performed using Layer1:

The Brunner Munzel test- Although the Wilcoxon P Value test is the default as it is recommended in Lionel and Briand's paper, this test has the disadvantage that it assumes that the data is not heteroscedastic. The Brunner Munzel test does not make this assumption and so it is more robust.

Comparing an array with a single data point- A set of results can be compared with a single result i.e. when comparing the results of a stochastic algorithm with a deterministic one. For this the mann-whitney one sample test is used. There is currently no effect size test implemented for this situation.

Comparing paired data- This is for situations in which each sample in dataA is paired to a sample in dataB. This may be because the comparison is between two treatments carried out on one entity. For this the Wilcoxon Signed rank test of significance followed by the a paired version of the Vargha Delaney effect size test.

Censored data- This is for dichotomous tests where results can be cateogorized either as a pass or a fail at the point where the test finished. The fisher p value test followed by the odds ratio effect size test is used.

Modified Vargha Delaney- This allows the application of a modified version of the Vargha Delaney effect size test which is customised to the problem so that only differences which can't be regarded as trivial or "noise" are taken into account (Neumann2015). To use this create a new class extending astraiea.layer1.effectsize.ModifiedVarghaDelaney Objects of this type can then be passed in as a parameter in Layer1.compare(...) or astraiea.layer2.Layer2.run(...) methods.

==============Layer 2==================================

2.1 - DEFAULT:

By default 2 or more generators (e.g. algorithms) are each invoked for a fixed number of times and the results compared through layer 1. The method call is as follows: Layer2.run(gens, double significanceThreshold, boolean brunnerMunzel, boolean paired, IncrementingStrategy incr, Random random)

"gens" is of type SetOfComparisons. This contains a set of SetOfExperiments objects, each of which is responsible for running one generator. See the Javadoc for more information. The SetOfComparisons object is where options for how the comparison are to be carried out are applied (e.g. whether an all against all or an all against one comparison is needed and whether any adjustment for multiple comparisons, such as BonFerroni, is to be applied).

2.2 OTHER COMPARISONS

Censored data strategies: Censored data means that a result is classed as true or false based on an observation taken at whichever point the test finished. This is, in some sense, an arbitrary judgement. For this reason, Arcuri and Briand's paper suggests various alternative tests to be carried out after the initial censored test. Instructions for how censoring is to be carried out are passed as a parameter to Layer2 using an object of type astraiea.layer2.strategies.CensoringStrategy. Call method runCensored(...) unless using timeseries generators, in which case a CensoringStrategy parameter is included in the default run(...) method. The reason for this is because when censoring is used for non timeseries generators only dichotomous tests will be carried out and this places limits on which options can be set (e.g. Brunner Munzel is no longer possible). When a timeseries is used there is an option for carrying out non dichotomous tests on the length of time that each algorithm took to reach a passing value and so all of these options are available. See the documentation for Layer2 for more details. Whatever strategy is used, all intermediate p values are printed out.

Incrementing data: It is possible to carry out additional invocations of each generator if the initial n invocations proved to be insufficient to demonstrate significance. This process must be carried out in a fair way and the data must be adjusted accordingly. Various alternative methods for doing this can be plugged into Astraiea. How Incrementing should be performed is encapsulated into an object of class "IncrementingStrategy" passed to Layer2. All intermediate p values are output if this is used on the recommendations of Arcuri and Briand.

Multiple artefacts: This is when one generator is invoked on multiple instances of a problem. This is dealt with in Astraiea in the manner recommended by Arcuri and Briand. For each set of runs on one artefact the median is taken, thus producing one value for each artefact. It is these values which are then used in statistical testing in the normal manner. To ensure that this process is carried out, any generator featuring multiple artefacts should be of a type implementing astraiea.layer2.generators.artefact.ArtefactGenerator or at least of type astraiea.layer2.generators.Generator. How the repeats of an artefact actually take place should be implemented in ArtefactGenerator's subclass.

Comparisons of more than 2 generators: This is possible through an object of type astraiea.layer2.experiments.SetOfComparisons. The recommendation of Arcuri and Briand is that all p values should be printed instead of using adjustments such as Hochberg and Bonferroni. All p values from every comparison are output whether or not an adjustment is used but an adjustment can be used through a parameter of type astraiea.layer2.MultiTestAdjustment passed to the constructor of SetOfComparisons.

====================References====================

Arcuri, Andrea, and Lionel Briand. "A hitchhiker's guide to statistical tests for assessing randomized algorithms in software engineering." Software Testing, Verification and Reliability 24.3, 2014. 219-250.

Neumann, Geoffrey, Mark Harman, and Simon Poulding. "Transformed Vargha-Delaney Effect Size." Search-Based Software Engineering. Springer International Publishing, 2015. 318-324. APA
