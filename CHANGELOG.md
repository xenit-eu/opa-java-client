# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.4.0 - (Unreleased)

Changed:
* [#7] - Moved maven group coordinates to `com.contentgrid.opa-java-client`

Housekeeping:
* [#6] - Updated Gradle to 7.5.1 
* [#6] - Updated dependencies to Spring Boot 2.6.12 baseline

[#6]: https://github.com/xenit-eu/opa-java-client/pull/6
[#7]: https://github.com/xenit-eu/opa-java-client/pull/7/files
## 0.3.0 - 2021-12-09

Fixed:
* [#4] - OPA client can now serialize `java.time` classes as `input` 

[#4]: https://github.com/xenit-eu/opa-java-client/pull/4

## 0.2.0 - 2021-07-20

Changed:
* Moved maven group coordinates to `eu.xenit.contentcloud`
* Change OPA term `Numeric` parameterized type from `Number` to `BigDecimal`
* Make http-log-spec configurable from `OpaClient.Builder`

Fixed:
* Fixes OPA terms equality checks

## 0.1.0 - 2021-05-26

Added:
- Initial release
