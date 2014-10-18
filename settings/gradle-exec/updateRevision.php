<?php

echo "Start BUILD revision \n";
date_default_timezone_set('Europe/Berlin');

$langEn = file_get_contents('./res/values/strings.xml');
$langCS = file_get_contents('./res/values-cs/strings.xml');

file_put_contents('./res/values/strings.xml',  preg_replace('/build: [0-9]{2}\.[0-9]{2}\.[0-9]{4}/', 'build: ' .date('d.m.Y'), $langEn));
file_put_contents('./res/values-cs/strings.xml',  preg_replace('/sestavení: [0-9]{2}\.[0-9]{2}\.[0-9]{4}/', 'sestavení: ' .date('d.m.Y'), $langCS));

echo "stop BUILD revision \n";