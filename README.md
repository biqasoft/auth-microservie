# Auth microservice

 - [Hazelcast](https://hazelcast.org/) for failed auth limit for request IP address
 
# Property config
  - `biqa.auth.limits.interval.fail.enable=TRUE` - Enable fail limit
  - `biqa.auth.limits.interval.fail.times=10` - max times
  - `biqa.auth.limits.interval=0 * * * * *` - cron expression for clear ban (1 minute default)
  - `biqa.auth.password.reset.default.ttl=3600000` - # one hour; 0 - disable expired function
  
  