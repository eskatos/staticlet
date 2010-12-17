
staticlet
=========

What staticlet is
-----------------

Staticlet is a servlet for serving files restfully according to HTTP/1.0 & HTTP/1.1
supporting only the GET & HEAD methods. In other words, it expose a filesystem tree as
read only resources. It is a quite small and has only one dependency.



What staticlet do ...
---------------------

... good

- HTTP/1.0 (strict) and HTTP/1.1 conditional GET & HEAD support
- HTTP/1.1 ranges support (mostly as in the BalusC FileServlet)
- Pluggable ETag generation, several implementations provided

... not so good

- Hide files starting with a '.'
- Simple directory listing support (in memory & not conditional & no html cache : naive & ineficient ... IOW useless for now)



Quickstart
----------

WARNING! No released version yet, you'll have to clone and build it locally.

You'll need maven or an ide supporting it to easily build staticlet.

staticlet is split in modules:

- staticlet-core            Most of the code is here
- staticlet-testsupport     Test code & resources
- staticlet-classic         Depend on this one to use the Staticlet servlet configured via init-params

staticlet has only slf4j-api as dependency for logging, choose your implementation.



Configuration
-------------

Initial parameters:

    docRoot             String      Path to the staticlet servlet docroot
                                    Mandatory
    directoryListing    Boolean     Is directory listing enabled ?
                                    Optionnal, defaults to false
    bufferSize          Integer     Size of the buffer used when streaming data from filesystem to clients Defaults
                                    Optionnal, defaults to false
    expireTime          Long        HttpHeaders.EXPIRES, System.currentTimeMillis() + configuration.getExpireTime()
                                    Optionnal, defaults to one week



Qi4j
----

A ServiceComposite with the staticlet servlet as a Mixin is available in the qi4j-lib-staticlet project.
You'll need to configure it à là Qi4j using the StaticletConfiguration composite.
No assembly facility yet but it's really simple, look at the unit tests.




Hacking
-------

Here are the links to the two HTTP RFCs:

- HTTP/1.1 http://www.ietf.org/rfc/rfc2616
- HTTP/1.0 http://www.ietf.org/rfc/rfc1945

If you have any question, remark, whatever or found a flaw in the implementation or are having
issues using staticlet, don't hesitate to create a github issue here.




TODO
----

Unit tests

 - factor test code and ressources in staticlet-testsupport so staticlet-classic and qi4j-lib-staticlet can be tested automagically
 - implements tests for HTTP/1.0 & HTTP/1.1
 - rebuild staticlet-docroot.zip with random data so we can make byte level comparisons in client for ranges

Various

 - cleanup logging: no info level logging with debug level meaning HTTP protocol level and trace meaning code level
 - add support for path overlays (ie. / goes to /storage/root, /mirror goes to /storage/mirrors and /public goes to /storage/var/public)
   hint: need a FileFactory that create java.io.File instances resolving the real path, need to define interface & configuration
 - refactor FileRepresenter.processRangeHeaders() to lower code complexity, need unit tests before
 - send a 401 when a .nolist file is present in directory (not activated by default)
 - send a 404 when a .hidden file is present in path (not activated by default)

Caching

 - configuration: write on disk cache data in the live filesystem or in an "overlay" stored elsewhere
 - implement cache using the jsr107 api with ehcache
 - add a way to bybass gzip compression (path patterns, mime-types) [reminder: mime-types are defined by the container and then in web.xml mime-mapping elements]
 - cache gzipped content on disk (path patterns, size limit)
 - cache node that hides listing in a Map<TimeStamp,Path>
    - keep shorter paths only
    - configurable TTL of cache entries
 - cache directory representation
    - in memory: store in a Map<TimeStamp,Path>, ttl
    - on disk: path, ttl, emptyOnStart
 - provide a way to empty cache at runtime (file presence and lastModified polling ?)

Qi4j

 - see why Configuration defaults values at assembly don't work
 - provide two ways to configure: web.xml or qi4j configuration


