# meter

Meter - is a simple micro-benchmarking tool for Android (not only for Android, its actually for any Java project). 

It was designed for Android. Meter try to make measurement an easy task, with minimal impact on total project performance.

# State [![Build Status](https://secure.travis-ci.org/OleksandrKucherenko/meter.png?branch=master)](https://travis-ci.org/OleksandrKucherenko/meter)

Active development, started at: 2014-07-13

# Design objectives

* minimalistic code
* deployment in one file
* no support of scenarios that has more than 256 steps
* nested measurements
* measurement of loops/iterations:
* loops with known number of iterations
* loops with unknown number of iterations (round array in use, 10000 steps of loop used for average calculations)
* loop Min/Max/Average cost of iteration
* no sub-steps inside the loops, but allowed sub-measurement
* measurement limited by one thread (partly limited by class design, its just a recommendation not an actual limit)
* minimalistic allocations during benchmarking, all calculations and allocations are done only on Meter.stats() call
* logcat as standard output.  Developer can change output class instance to any required. Simple interface to inherit/implement. 
* simple configuration of output formats. Boolean flags mostly.
* nanos timestamps used for time calculations, millis for time output
