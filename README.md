# Transfer Pattern Generator 

This project uses a modified version of the Connection Scan Algorithm to generate a set of transfer patterns for every stop in a GTFS dataset.

It works by iteratively creating a Minimum Spanning Tree at every minute of the day for every stop in the network. 

The code itself is quite a mess for a number of reasons:

- The connection scan algorithm is inherently imperative and does not port well to Scala
- My Scala knowledge is poor
- It is heavily optimized

## Test
```
sbt test
```

## Run
```
sbt run
```

## Build
```
sbt assembly
```
## Contributing

Issues and PRs are very welcome. 

## License

This software is licensed under [GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).

Copyright 2017 Linus Norton.

