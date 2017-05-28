#!/bin/sh
echo y | fly -t home sp -p blog-blog-webhook -c pipeline.yml -l ../../credentials.yml
