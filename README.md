
staticlet
=========

Staticlet is a servlet implementing GET & HEAD methods for HTTP/1.0 & HTTP/1.1 on top of a filesystem.




Features
--------

- Full HTTP/1.0 (strict) and HTTP/1.1 conditional GET & HEAD support
- Full HTTP/1.1 ranges support (mostly as in the BalusC FileServlet, thanks dude)
- Only one dependency: slf4j
- The code is easy to read
- Practical logging: 99% is debug/trace with debug level meaning HTTP protocol level and trace meaning code level
- Pluggable ETag generation, one cheap and one hex md5 implementations are provided

- Hide files starting with a '.'
- Simple directory listing support (in memory & not conditional & no html cache : naive & ineficient ... IOW useless for now)




Quickstart
----------

WARNING! No released version yet, you'll have to clone and build it locally

### HIDE START

staticlet is available on maven central, add the following to your pom:

        <dependency>
            <groupId>org.codeartisans.staticlet</groupId>
            <artifactId>staticlet-classic</artifactId>
            <version>1.0</version>
        </dependency>

### HIDE STOP



Configuration
-------------

Initial parameters:
    docRoot             String      Path to the staticlet servlet docroot
    directoryListing    Boolean     Is directory listing enabled ?




Qi4j
----

A Qi4j ServiceComposite with the staticlet servlet as a Mixin is available in the Qi4j sandbox.
Configuration is done using a ConfigurationComposite to include in your application configuration.




Hacking
-------

The servlet is the *org.codeartisans.staticlet.Staticlet* class.
It's *initStaticlet* method is called for configuration.
It's *processRequest* methos is called for each request.
The unit test class, yes only one for now, is here: *org.qi4j.library.http.staticlet.StaticletServiceTest*.
This unit test is a Qi4j test but don't worry if you don't know Qi4j it's pretty clear.

Here are the links to the two HTTP RFCs:
- HTTP/1.1 http://www.ietf.org/rfc/rfc2616
- HTTP/1.0 http://www.ietf.org/rfc/rfc1945

If you have any question, remark, whatever or found a flaw in the code or are having
issues using staticlet, don't hesitate to create a github issue here.





TODO
----

 * Unit tests
 * - factor test code and ressources in staticlet-testsupport si staticlet-classic and qi4j-lib-staticlet can be tested automagically
 * - implements tests for HTTP/1.0 & HTTP/1.1
 * - rebuild staticlet-docroot.zip with random data so we can make byte level comparisons in client for ranges
 * Various
 * - add support for path overlays (ie. / goes to /storage/root, /mirror goes to /storage/mirrors and /public goes to /storage/var/public)
 *   hint: need a FileFactory that create java.io.File instances resolving the real path, need to define interface & configuration
 * - refactor FileRepresenter.processRangeHeaders() to lower code complexity, need unit tests before
 * - send a 401 when a .nolist file is present in directory (not activated by default)
 * - send a 404 when a .hidden file is present in path (not activated by default)
 * Caching
 * - configuration: write on disk cache data in the live filesystem or in an "overlay" stored elsewhere
 * - implement cache using the jsr107 api with ehcache
 * - add a way to bybass gzip compression (path patterns, mime-types) [reminder: mime-types are defined by the container and then in web.xml mime-mapping elements]
 * - cache gzipped content on disk (path patterns, size limit)
 * - cache node that hides listing in a Map<TimeStamp,Path>
 *    - keep shorter paths only
 *    - configurable TTL of cache entries
 * - cache directory representation
 *    - in memory: store in a Map<TimeStamp,Path>, ttl
 *    - on disk: path, ttl, emptyOnStart
 * - provide a way to empty cache at runtime (file presence and lastModified polling ?)
 * Qi4j
 * - see why Configuration defaults values at assembly don't work
 * - provide two ways to configure: web.xml or qi4j configuration

