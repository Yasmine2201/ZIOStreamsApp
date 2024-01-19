providing use case presentation, a functional schema, instructions on how to run and test your application and the decisions made (libraries, data structure(s), algorithm, performance, ...).

# Energy Analysis Tool

## How to run & test

### Prerequisites

- [Scala 3](https://docs.scala-lang.org/scala3/getting-started.html) (Coursier install is recommended)
- [SBT](https://www.scala-sbt.org) (should come with the coursier install)

### Run

- Clone the repository
- Run `sbt run` in the root directory of the project

### Test

- Run `sbt test` in the root directory of the project

## Introduction

This tool was made for the ALTN73 Functional Programming course at Efrei. It parses the different CSVs provided by:
- [Electricity Maps](https://electricitymaps.com) for the carbon intensity of electricity production in France per hour in 2021 and 2022.
- [ODRE (OpenData Réseaux-Energies)](https://odre.opendatasoft.com) for:
  - the monthly raw & corrected consumption of electricity in France per month.
  - the amount of electricity produced by each type of energy in France and its consumption per hour.
  - the power needed to cover the daily peak of power and temperature in France per day since 2012.

The tool allows you to show statistics about specific days, for a given period but also a case study on Temperature vs Power peaks.
Those are accessible through a menu in the console.

For the given periods, the tool will show various tables in the console, here is an example:
```
+---------------------------------------------------------------------------------------------------------+
|                                     Production (MW) by supply chain                                     |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Field                                            | Min      | Max      | Average  | Std Dev  | Nb pts   |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Fuel                                             |   161,00 |  1507,00 |   305,65 |   257,75 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Coal                                             |    14,00 |  1777,00 |   382,98 |   455,24 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Gas                                              |  2337,00 |  9636,00 |  6541,60 |  2096,69 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Nuclear                                          | 27060,00 | 45894,00 | 40671,14 |  3931,92 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Wind                                             |   550,00 | 16644,00 |  5743,92 |  3870,51 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Solar                                            |     0,00 |  9969,00 |  1630,67 |  2508,52 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Hydro                                            |  1576,00 | 17628,00 |  6479,04 |  3325,86 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
| Bio                                              |   637,00 |   971,00 |   760,06 |    68,11 |     6240 |
+--------------------------------------------------+----------+----------+----------+----------+----------+
 Data between 2023-01-10 and 2023-03-15
```
For instance, the above data comes from one of the ODRE datasets and shows the minimum, maximum, average and standard deviation of the production of each type of energy in France between 2023-01-10 and 2023-03-15.

## How it was made

### Libraries used

This leverages the following libraries:
- [ZIO](https://zio.dev) (+ ZIO-Streams) mainly for following the functional paradigm and for the streams.
- [Scala-CSV](https://github.com/tototoshi/scala-csv) for parsing CSV files.
- [Scala-Test](https://www.scalatest.org) for unit testing.

### Project-wide

#### Decisions

We decided to structure the project in a functional way, trying to avoid at all costs the mix of OO and FP paradigms, which is not always easy in Scala since it is based on the JVM and thus has a lot of OO/Java features.
In a functional programming fashion, we followed the classic rules such as immutability & no side effects as much as possible and where possible (IO/File are exceptions since they are inherently side effects).

ZIO was required for the project, so we mainly used it for streams, notably to parse the CSVs and to do the different computations on the data, which allowed us to have a very clean and concise code that also had proper error handling.

We also used Scala-CSV to parse the CSVs, the library is very straightforward, but we still had a few struggles with it, notably on the File IO part, but we still figured it out to work in the tests and in the main program.

We decided to use Scala-Test for unit testing; we wrote a few unit tests for the different functions we wrote. Those non-exhaustive tests cover the main functions of the program, such as data parsing, mathematical computations, etc. For a potential project continuation, we could write a bit more tests, such as formatting tests.

#### Tooling

We used VSCode as our IDE, with the Metals extension for Scala.
We also were provided with online self-hosted [VSCode containers](https://coder.com) to make sure we all had the same working environment.

In addition, we used GitHub for project management and version control. We leveraged the GitHub Actions to run the tests on each push and pull request.

#### Structure

The project is structured as follows:
- `src/main/scala` contains the main program, which is split into multiple files:
  - `Main.scala` for the ZIO entry point of the program, which required us to scrupulously follow the functional paradigm and return ZIO effects for the for the function results and thus in the for comprehensions generators, which meant that we had to use for instance, the `printLine` and `readLine` functions included in the zio.Console namespace. Main loads the data and launches the main UI recursive loop.
  - `UI.scala` for the user interface. It shows the menu and handles the user input with the appropriate error handling. It also calls the different functions to show the data. It is of note that the UI is the only part of the program that is not purely functional, since it has side effects (printing to the console).
  - `Types.scala` for the different types used in the program, such as energy units, their conversion into each other and also the orderings. This allowed us to clearly isolate the business types from the primitive types, which is a good practice in functional programming.
  - `DataModels.scala` for the different data models used in the program. They are all case classes, which are immutable by default in Scala. Those models mainly represent the different datasets we used.
  - `DataLoader.scala` for the different functions used to load the data from the CSVs, which are then parsed into the data models using mostly the Option monad to make sure we have the data we need to build the models.
  - `ChunkMath.scala` is an extension to the ZIO Chunk class, which allows us to do mathematical operations on the data, such as computing the average, the standard deviation, variance etc.
  - `formatters` contains the different functions used to format the data into tables, which are then printed to the console.
  - `analysis` contains the different functions used to analyze the data, with functions useful for the case study (application of the Pearson correlation coefficient) and also for the different statistics (min, max, average, etc).

### Performance

We did not run any performance tests on the program, but we did not notice any performance issues during the development of the program. The CSV parsing is pretty fast for our rather large datasets, thanks to our use of the ZIO streams.