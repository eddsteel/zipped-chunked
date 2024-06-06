# Example Webapp

This application demonstrates a possible connection leak bug around chunked compressed responses with dropwizard 3.

## POST /

This endpoint produces a chunked, optionally compressed response containing a single chunk of some JSON. It looks like this with curl:

```sh
$ nix-shell -p maven

[nix-shell:~/src/zipped-chunked]$ mvn clean package >/dev/null

[nix-shell:~/src/zipped-chunked]$ java -Ddw.client.compress=false -jar target/webapp-1.0-SNAPSHOT.jar server config.yml >/dev/null &

[nix-shell:~/src/zipped-chunked]$ curl -v http://localhost:8080 -d {}
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> Content-Length: 2
> Content-Type: application/x-www-form-urlencoded
>
< HTTP/1.1 200 OK
< Date: Thu, 06 Jun 2024 16:44:07 GMT
< Content-Type: application/json
< Vary: Accept-Encoding
< Transfer-Encoding: chunked
<
{"label":"test","integers":[1,2,3],"integerCount":3}
* Connection #0 to host localhost left intact

[nix-shell:~/src/zipped-chunked]$ curl --compressed -v http://localhost:8080 -d {}
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> Accept-Encoding: deflate, gzip
> Content-Length: 2
> Content-Type: application/x-www-form-urlencoded
>
< HTTP/1.1 200 OK
< Date: Thu, 06 Jun 2024 16:44:52 GMT
< Content-Type: application/json
< Vary: Accept-Encoding
< Content-Encoding: gzip
< Transfer-Encoding: chunked
<
{"label":"test","integers":[1,2,3],"integerCount":3}
* Connection #0 to host localhost left intact

```

## GET /check

This endpoint calls the "server" endpoint using the dropwizard default HTTP client. If the application is configured to request a compressed response, the client connection is not released. This is demonstrated by this endpoint calling the server endpoint 1025 times, one more than the default pool size.

```
[nix-shell:~/src/zipped-chunked]$ java -Ddw.client.compress=false -jar target/webapp-1.0-SNAPSHOT.jar server config.yml >/dev/null &
[1] 28790

[nix-shell:~/src/zipped-chunked]$ curl http://localhost:8080/check
done
[nix-shell:~/src/zipped-chunked]$ kill 28790

[nix-shell:~/src/zipped-chunked]$ java -Ddw.client.compress=true -jar target/webapp-1.0-SNAPSHOT.jar server config.yml >/dev/null &

[nix-shell:~/src/zipped-chunked]$ curl http://localhost:8080/check
{"code":500,"message":"Connections exhausted"}
```

# Why?

Not sure yet, but it looks like in the uncompressed case, the connection is released by the EOFSensorInputStream, which is triggered by a -1 on the stream. If the stream is compressed, this never occurs.

## Trace dump/ received bytes in compressed case

```
"pool-3-thread-7 - GET /check@4758" prio=5 tid=0x1b nid=NA runnable
  java.lang.Thread.State: RUNNABLE
      at org.apache.hc.core5.http.io.EofSensorInputStream.checkEOF(EofSensorInputStream.java:195)
      at org.apache.hc.core5.http.io.EofSensorInputStream.read(EofSensorInputStream.java:119)
      at java.util.zip.CheckedInputStream.read(CheckedInputStream.java:59)
      at java.util.zip.GZIPInputStream.readUByte(GZIPInputStream.java:266)
      at java.util.zip.GZIPInputStream.readUShort(GZIPInputStream.java:258)
      at java.util.zip.GZIPInputStream.readHeader(GZIPInputStream.java:164)
      at java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:79)
      at java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:91)
      at org.apache.hc.client5.http.entity.GZIPInputStreamFactory.create(GZIPInputStreamFactory.java:61)
      at org.apache.hc.client5.http.entity.LazyDecompressingInputStream.initWrapper(LazyDecompressingInputStream.java:51)
      at org.apache.hc.client5.http.entity.LazyDecompressingInputStream.read(LazyDecompressingInputStream.java:57)
      at org.glassfish.jersey.message.internal.EntityInputStream.read(EntityInputStream.java:69)
      at org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream.read(ReaderInterceptorExecutor.java:263)
      at org.glassfish.jersey.client.ChunkedInput$AbstractBoundaryParser.readChunk(ChunkedInput.java:112)
      at org.glassfish.jersey.client.ChunkedInput.read(ChunkedInput.java:471)
      at com.eddsteel.resources.ClientResource.callServer(ClientResource.java:30)
```

This gets the following bytes: `31, 16, 139, 8, 0, 6, 57`, which seems to only be the GZIP header from `GZIPInputStream.readHeader` in the constructor.

## Trace dump/ received bytes in uncompressed case

```
"pool-3-thread-7 - GET /check@4741" prio=5 tid=0x1b nid=NA runnable
  java.lang.Thread.State: RUNNABLE
      at org.apache.hc.core5.http.io.EofSensorInputStream.checkEOF(EofSensorInputStream.java:197)
      at org.apache.hc.core5.http.io.EofSensorInputStream.read(EofSensorInputStream.java:119)
      at org.glassfish.jersey.message.internal.EntityInputStream.read(EntityInputStream.java:69)
      at org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream.read(ReaderInterceptorExecutor.java:263)
      at org.glassfish.jersey.client.ChunkedInput$AbstractBoundaryParser.readChunk(ChunkedInput.java:112)
      at org.glassfish.jersey.client.ChunkedInput.read(ChunkedInput.java:471)
      at com.eddsteel.resources.ClientResource.callServer(ClientResource.java:30)
```

Bytes received: (the full expected JSON + -1): `123, 34, 108, 97, 98, 101, 108, 34, 58, 34, 116, 101,
115, 116, 34, 44, 34, 105, 110, 116, 101, 103, 101, 114, 115, 34, 58, 91, 49, 44, 50, 44, 51, 93, 44,
34, 105, 110, 116, 101, 103, 101, 114, 67, 111, 117, 110, 116, 34, 58, 51, 125, 13, 10, -1`.

```sh
$ kotlinc
>>> listOf(123, 34, 108, 97, 98, 101, 108, 34, 58, 34, 116, 101, 115, 116, 34, 44, 34, 105, 110, 116, 101, 103, 101, 114, 115, 34, 58, 91, 49, 44, 50, 44, 51, 93, 44, 34, 105, 110, 116, 101, 103, 101, 114, 67, 111, 117, 110, 116, 34, 58, 51, 125, 13, 10, -1).map{it.toChar()}.joinToString("")
res3: kotlin.String = {"label":"test","integers":[1,2,3],"integerCount":3}
￿

```

The final `-1` triggers [the connection to be released](https://github.com/apache/httpcomponents-client/blob/rel/v5.3.1/httpclient5/src/main/java/org/apache/hc/client5/http/impl/classic/ResponseEntityProxy.java#L98).
