#!/usr/bin/env sh

ps aux | grep alena
echo ""

test_run(){
  service $1
  cat /run/$1
  service $1 status
  service $1 start
  cat /run/$1
  service $1 status
  service $1 stop
  cat /run/$1
  service $1 status
  echo ""
}

test_run media-controller
test_run client-handler
test_run webcall-app

ps aux | grep alena
