# Spark Module

This module receives the tweets and runs our opinion mining algorithm to generate the various aspects of the queried product. Then the opinions expressed on each aspect of that product is extracted. This data is then pushed onto kafka queue for the backed to consume it.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.
### Dependencies to be installed

```
Core NLP
Kafka
Spark
```

#### Building the app

```
./gradlew clean build
```

#### Running the app

```
./gradlew run
```

