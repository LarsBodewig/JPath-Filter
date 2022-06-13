# JPath-Filter-Test

This is a test module to showcase the JPath-Filter. For more configuration check the parent README.

## Run

Run `mvn tomcat7:run` to start a local Apache Tomcat server on http://localhost:8080/. The local server can be configured as described by the [tomcat-maven-plugin docs](https://tomcat.apache.org/maven-plugin-trunk/run-mojo-features.html). 


## Mappings

The module contains a test servlet with two mappings to show how to apply the filter to specific paths. However you can specify the filter mapping in any possible way to match all/only the servlets you want.

| Url | Result |
| - | - |
| `/` | `{"hello":{"world":"!"}}` |
| `/?p=$.hello` | `{"world":"!"}` |
| `/no_filter?p=$.hello` | `{"hello":{"world":"!"}}` |
