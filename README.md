# JPath-Filter

JPath-Filter is a quick solution to enable [JsonPath](https://github.com/json-path/JsonPath) as a parameter for your existing JSON servlets. This allows you to specify a selector as URL parameter to return specific JSON portions.

## Usage

**For a working example check out [jpath-filter-test](jpath-filter-test).**

1. Add the dependency to your maven pom:

       <dependency>
           <groupId>dev.bodewig</groupId>
           <artifactId>jpath-filter</artifactId>
           <version>1.0</version>
       </dependency>

2. Add mappings to your web.xml:

       <filter>
           <filter-name>JPathFilter</filter-name>
           <filter-class>dev.bodewig.jpath.filter.JPathFilter</filter-class>
       </filter>
       <filter-mapping>
           <filter-name>JPathFilter</filter-name>
           <!-- this can be a specific path or servlet name too -->
           <url-pattern>/*</url-pattern>
       </filter-mapping>

3. Read up on the JsonPath syntax described in detail by [Jayway](https://github.com/json-path/JsonPath) and [Stefan Gössner](https://goessner.net/articles/JsonPath/).

And just like that you can query your JSON servlets: `/?p=$.store.book[0].title`. 


## Configuration

4. If you want to change the url parameter name (default: `p`), use this init-param:

       <init-param>
           <param-name>path-param</param-name>
           <param-value><!-- whatever name you want --></param-value>
       </init-param>

5. If you want to use a different JSON parser (default: [JsonSmart](https://github.com/netplex/json-smart-v2)), add it as a dependency to your pom and use this init-param ([list of supported providers](https://github.com/json-path/JsonPath#jsonprovider-spi)):

       <init-param>
           <param-name>provider-class</param-name>
           <!-- or any other supported provider -->
           <param-value>com.jayway.jsonpath.spi.json.GsonJsonProvider</param-value>
       </init-param>

6. If you still want to change more, feel free to subclass JPathFilter and add your favorite logging framework and error handling or send me a pull request.

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
