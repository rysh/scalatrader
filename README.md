# scalatrader [![Build Status](https://travis-ci.org/rysh/scalatrader.svg?branch=master)](https://travis-ci.org/rysh/scalatrader)[!["THE SUSHI-WARE LICENSE"](https://img.shields.io/badge/license-SUSHI--WARE%F0%9F%8D%A3-blue.svg)](https://github.com/MakeNowJust/sushi-ware)
I want to make a trading system that can be operated by an individual.  
Current target is bitflyer's bitcoin.

## Available Feature 
### store/ticker
It saves Bitcoin ticker information on S3 using bitflyer real-time API  

How to use  

If you just try it you can  
$> sbt run

You can create docker image.  
$> sbt docker:publishLocal

Runnable docker image is here.  
https://hub.docker.com/r/scalatrader/store-ticker/

## Planed Feature  
Position Monitoring  
Strategy Execution  
Back testing  