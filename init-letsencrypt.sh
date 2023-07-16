#!/bin/bash

# Certbot 더미 인증서 생성
sudo mkdir -p /etc/letsencrypt/live/www.jaetteoli.shop/
sudo openssl req -x509 -nodes -newkey rsa:1024 -days 1\
    -keyout '/etc/letsencrypt/live/www.jaetteoli.shop/privkey.pem' \
    -out '/etc/letsencrypt/live/www.jaetteoli.shop/fullchain.pem' \
    -subj '/CN=localhost'

# Docker Compose 시작
sudo docker-compose up --force-recreate -d nginx

# 잠시 대기
sleep 10

# 더미 인증서 제거
sudo rm -rf /etc/letsencrypt/live/www.jaetteoli.shop/ && sudo rm -rf /etc/letsencrypt/archive/www.jaetteoli.shop/

# 실제 인증서 요청
sudo docker-compose run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    --email jskim2x@naver.com \
    --agree-tos --no-eff-email \
    --force-renewal \
    -d www.jaetteoli.shop" certbot

# Nginx 컨테이너 재시작
sudo docker-compose exec nginx nginx -s reload