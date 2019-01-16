this is just a sample project used for troubleshooting.  It is not a complete application and has a few things that still need to be fixed such as

* Properly forward client headers instaed of just the router services headers
* Need to handle request body for post request.  Currently body is dropped when forwarding the post request.



## build and deploy

```
mvn package && cf push router-service -p target/viper-0.0.1-SNAPSHOT.jar
```
