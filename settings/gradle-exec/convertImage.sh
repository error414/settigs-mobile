#!/bin/sh
cd res;
/usr/bin/find . -type f -name "*.png" -exec /opt/local/bin/convert {} -strip {} \;