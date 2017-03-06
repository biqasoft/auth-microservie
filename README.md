# Auth microservice

 - [Hazelcast](https://hazelcast.org/) for failed auth limit for request IP address
 
## Property config
  - `biqa.auth.limits.interval.fail.enable=TRUE` - Enable fail limit
  - `biqa.auth.limits.interval.fail.times=10` - max times
  - `biqa.auth.limits.interval=0 * * * * *` - cron expression for clear ban (1 minute default)
  - `biqa.auth.password.reset.default.ttl=3600000` - # one hour; 0 - disable expired function
 
## REST

With swagger documentation
 
## Grpc

 files `src/main/proto/`

#### C++
  `protoc --grpc_out=. --plugin=protoc-gen-grpc=f:/development/grpc_cpp_plugin.exe *.proto`
  `protoc --cpp_out=. *.proto`
  