# Tiles for JakartaEE

[![Maven Central](https://img.shields.io/maven-central/v/io.github.laurentschoelens.tiles/tiles-parent)](https://mvnrepository.com/artifact/io.github.laurentschoelens.tiles/tiles-parent)
[![GitHub Actions Status](https://github.com/laurentschoelens/tiles/workflows/Tiles/badge.svg)](https://github.com/laurentschoelens/tiles/actions)
[![Tiles Spring Compat](https://github.com/laurentschoelens/tiles/actions/workflows/maven-spring.yml/badge.svg)](https://github.com/laurentschoelens/tiles/actions/workflows/maven-spring.yml)
[![CodeQL](https://github.com/laurentschoelens/tiles/workflows/CodeQL/badge.svg)](https://github.com/laurentschoelens/tiles/actions/workflows/codeql-analysis.yml?query=workflow%3ACodeQL)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## About & Purpose

This project is an independent, community-maintained fork of **Apache Tiles**, which was retired to the [Apache Attic](https://apache.org) by The Apache Software Foundation (ASF).

Its primary goal is to **maintain and modernize the codebase to ensure full compatibility with Jakarta EE** (migrating away from legacy `javax.*` namespaces to `jakarta.*`) with JDK17 baseline.

* **Equivalent original artifact:** `org.apache.tiles:tiles-parent`
* **New artifact coordinates:** `io.github.laurentschoelens.tiles:tiles-parent`

This project also provides a `tiles-spring-extension` which takes back previous support of Spring 5.x of Tiles but adapted for this version of Tiles and is tested against the following Spring version (at build) : 6.0.23, 6.1.21, 6.2.19, 7.0.10.

## Origin & Acknowledgements

* **Original project:** Apache Tiles (Copyright 1999–2012 The Apache Software Foundation)
* **Disclaimer:** This fork is maintained independently by Laurent Schoelens and is **not** affiliated with, endorsed by, or supported by The Apache Software Foundation. *Apache* and *Apache Tiles* are trademarks or registered trademarks of The Apache Software Foundation. *Jakarta EE* is a trademark of the Eclipse Foundation.

## License

This project is licensed under the **Apache License, Version 2.0** — see the [LICENSE](LICENSE) and [NOTICE](NOTICE) files for details.
