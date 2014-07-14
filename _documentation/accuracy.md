# Accuracy of Measurements

Accuracy of measurement depends from several factors:
* `meter().getConfig().UseSystemNanos` value. By default used `System.nanoTime()` but you can change it to 
  `SystemClock.elapsedRealtimeNanos()` that is more accurate.
* Accuracy depends on how good is your benchmark written: 
- Do you eliminate all the 'warm-up' places? 
- Do you leave only places that you want to benchmark?
- Do you set a good number of iterations? Bigger number is always better, but slower.
- Do you try to reduce number of steps? Try to keep benchmark short. Best results are always when you have up to 10 steps.
- Small numbers are always hard to treat. This is not a profiler. OS time allocation for execution, load of system and 
  many other aspects may influence on numbers. If the difference between the steps is only in several nanos, than your 
  benchmark needs better defining. Just try another test.  
- Try to run tests on real devices, emulators, different OS versions. Numbers during benchmarking are always relative to 
  those factors.

Full output:
```
D/meter﹕ SetUp - test_01_TryCatchVsPlain
V/meter﹕ | 1439351591964 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |  0.00% |    0.000 ms |    0.000 ms | --> test_01_TryCatchVsPlain
W/meter﹕ | 0 | 1439351621006 | 0 | 0 | 0 | 0 | 0 | 0 |  0.01% |    0.029 ms |    0.029 ms | warm up classes
W/meter﹕ | 0 | 0 | 1439499275964 | 0 | 0 | 0 | 0 | 0 | 46.79% |  147.655 ms |  147.684 ms | preparations
V/meter﹕ | 0 | 0 | 0 | 1439499351631 | 0 | 0 | 0 | 0 |  0.02% |    0.076 ms |  147.760 ms | try/catch after warm up
V/meter﹕ | 0 | 0 | 0 | 0 | 1439585447589 | 0 | 0 | 0 | 27.28% |   86.096 ms |  233.856 ms | avg/min/max/total: 0.008/0.007/1.226/86.051 ms - calls:10000 / done.
V/meter﹕ | 0 | 0 | 0 | 0 | 0 | 1439585492798 | 0 | 0 |  0.01% |    0.045 ms |  233.901 ms | No try/catch
V/meter﹕ | 0 | 0 | 0 | 0 | 0 | 0 | 1439667073048 | 0 | 25.85% |   81.580 ms |  315.481 ms | avg/min/max/total: 0.008/0.008/0.366/81.566 ms - calls:10000 / done.
V/meter﹕ | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1439667169923 |  0.03% |    0.097 ms |  315.578 ms | <-- test_01_TryCatchVsPlain
V/meter﹕ --------------------------------------------------------------------------------
I/meter﹕ final: 167.894 ms (-147.684 ms), steps: 8
V/meter﹕ --------------------------------------------------------------------------------
I/meter﹕ top-1: | 0 | 0 | 0 | 0 | 1439585447589 | 0 | 0 | 0 | 27.28% |   86.096 ms |  233.856 ms | avg/min/max/total: 0.008/0.007/1.226/86.051 ms - calls:10000 / done.
I/meter﹕ top-2: | 0 | 0 | 0 | 0 | 0 | 0 | 1439667073048 | 0 | 25.85% |   81.580 ms |  315.481 ms | avg/min/max/total: 0.008/0.008/0.366/81.566 ms - calls:10000 / done.
V/meter﹕ --------------------------------------------------------------------------------
D/meter﹕ TearDown - test_01_TryCatchVsPlain
```