# scalatrader [![Build Status](https://travis-ci.org/rysh/scalatrader.svg?branch=master)](https://travis-ci.org/rysh/scalatrader)[!["THE SUSHI-WARE LICENSE"](https://img.shields.io/badge/license-SUSHI--WARE%F0%9F%8D%A3-blue.svg)](https://github.com/MakeNowJust/sushi-ware)
I want to make a trading system that can be operated by an individual.  
Current target is bitflyer's bitcoin.

## Available Feature 
### scalatrader
- System trading with bitFlyer
- Back test with local or S3 data
- Moniter and alert Positions

### store/ticker
It saves Bitcoin ticker information on S3 using bitflyer real-time API  

## How to use

If you just try it you can  
$> sbt run

You can create docker image.  
$> sbt docker:publishLocal

Runnable docker image is here.  
https://hub.docker.com/r/scalatrader/

### settings
set environment variables  
>\#example  
>APPLICATION_SECRET=QCYtAnfkaZiwrNwnxIlR6CTfG3gf90Latabg5241ABR5W1uDFNIkn  
>DB_URL=jdbc:mysql://x.x.x.x:6603/scalatrader  
>DB_USER_NAME=xxxxxx  
>DB_PASSWORD=xxxxxx  
>AWS_SQS_REQUEST_QUEUE_NAME=scalatrader-order  
>AWS_S3_BTCFX_BUCKET_NAME=btcfx-ticker-scala  
>AWS_S3_BTC_BUCKET_NAME=btc-ticker  


## Release Note
### scalatrader
#### v0.0.8
improve multiple strategies
- show recent performance for each strategy
- can trade with minimum fixed value(0.01)
- skip old ticker after retrying
- parallel backtesting of multiple strategies
#### v0.0.7
Support for SFD
#### v0.0.6
multiple strategies