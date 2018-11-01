#!/usr/bin/env bash
docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=trips -e POSTGRES_USER=trips -e POSTGRES_DB=trips -d postgres:10.4