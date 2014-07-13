meter
=====

Meter - is a simple micro-benchmarking tool for Android (not only for Android, its actually for Java). 
It was designed for Android. Meter try to make measurement aNn easy task, with minimal impact on total project performance.

Design objectives
===================

* minimalistic code
* deployment in one file
* no support of scenarious that has more than 256 steps
* measurement of loops/iterations:
* loops with known number of iterations
* loops with unknown number of iterations (round array in use, 10000 steps of loop used for average calculations)
* loop min/max/average cost of iteration
* no sub-steps inside the loops, but allowed sub-measurment
* measurement limiteed by one thread (partly limited)
* minimalistic allocations during benchmarking, all calculations and allocations are done only on Meter.stats() call
* logcat as standard output
* simple configuration of output. Boolean flags mostly.
* nanos timestamps used for time calculations, millis for time output
